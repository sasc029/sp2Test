package com.bjsasc.plm.part.publish.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.bjsasc.plm.core.Helper;
import com.bjsasc.plm.core.baseline.BaselineHelper;
import com.bjsasc.plm.core.baseline.model.Baselined;
import com.bjsasc.plm.core.context.model.ContextInfo;
import com.bjsasc.plm.core.context.model.ProductContext;
import com.bjsasc.plm.core.context.rule.RuleHelper;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.publish.PartPublishHelper;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.PersistUtil;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.system.principal.User;
import com.bjsasc.plm.core.system.principal.UserHelper;
import com.bjsasc.plm.core.system.task.TaskHelper;
import com.bjsasc.plm.core.system.task.TaskInfo;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewPublishHelper;
import com.bjsasc.plm.core.part.publish.util.ConflictUtils;
import com.bjsasc.plm.core.view.publish.model.ATLinkSnapshot;
import com.bjsasc.plm.core.view.publish.model.ATPublishBaseline;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATPublishTask;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;
import com.bjsasc.plm.core.view.publish.model.ATSummaryBaseline;
import com.bjsasc.plm.part.publish.PartPublishService;
import com.bjsasc.ui.json.DataUtil;

public class PartPublishServiceImpl implements PartPublishService {

	@SuppressWarnings("unchecked")
	public void savePublishTask(String convertNodeInfo, String partOid,
			String targetViewOid, String number, String aaUserInnerId) {

		//获取首选项，是否强制重新发布关系
		boolean recreatelink=ConflictUtils.CanReCreateLink;
		
		ATPublishBaseline publishBaseline = new ATPublishBaseline();// 发布基线
		ATSummaryBaseline summaryBaseline = null;// 汇总基线
		ATSummaryBaseline tempSummaryBaseline = null;// 临时汇总基线
		ATPublishPackage publishPackage = new ATPublishPackage();// 发布包

		View targetView = (View) Helper.getPersistService().getObject(
				targetViewOid);
		Part rootPart = (Part) Helper.getPersistService().getObject(partOid);
		View sourceView = rootPart.getView();
		ContextInfo contextInfo = rootPart.getContextInfo();// 产品上下文
		PersistHelper.getService().save(publishBaseline);// 持久化发布基线

		// 1、初始化发布包
		publishPackage.setContextInfo(contextInfo);
		publishPackage.setSourceView(sourceView);
		publishPackage.setTargetView(targetView);
		publishPackage.setNumber(number);// 发布编码
		publishPackage.setName(number);
		publishPackage.setPublishPart(rootPart);// 发布部件
		publishPackage.setPublishBaseline(publishBaseline);// 发布基线
		RuleHelper.getService().init(publishPackage, contextInfo.getContext());

		// 2 、分解bom 封装快照 打入发布基线
		List<Map<String, Object>> nodelist = DataUtil
				.JsonToList(convertNodeInfo);
		// 收集数据
		List<Baselined> baselinedList_publish = new ArrayList<Baselined>();
		
		for (Map<String, Object> map : nodelist) {
			if (!partOid.equals(map.get("OID"))) {

				Map<String, Object> linkMap = DataUtil.JsonToMap(map
						.get("link") + "");
				Persistable link = Helper.getPersistService().getObject(
						linkMap.get("OID") + "");
				if (link instanceof ATLink) {
					boolean found=false;
					for(Baselined linksnapshot : baselinedList_publish)
					{
						if(linksnapshot instanceof ATLinkSnapshot){
							if(((ATLinkSnapshot)linksnapshot).getLinkRef().getInnerId().equals(link.getInnerId())){
								found=true;
								break;
							}
						}
					}
					if(found==false)
					{
						ATLinkSnapshot linkSnapshot = PartPublishHelper
								.getPublishService().newATLinkSnapshot(
										(ATLink) link);
						PersistUtil.getService().save(linkSnapshot);
						baselinedList_publish.add(linkSnapshot);
					}
					// 获取链接打入基线
				}
			}
			if ("no".equals(map.get("CONVERTSTATUSID").toString())
					|| "no".equals(map.get("ISEXISTCONFIGID").toString())) {
				continue;
			}
			Persistable obj = Helper.getPersistService().getObject(map.get("OID") + "");
			if (obj instanceof ViewManageable) {
				ATSnapshot snapshot = PartPublishHelper.getPublishService()
						.newATSnapshot((ViewManageable) obj);
				PersistUtil.getService().save(snapshot);
				baselinedList_publish.add(snapshot);
			}
		}
		if(baselinedList_publish!=null&&baselinedList_publish.size()>0){
			BaselineHelper.getService().addToBaseline(baselinedList_publish,publishBaseline,false);
		}
		
		// 3、查询前置可用发布包列表
		ATPublishPackage predPackage = getPrePackages(contextInfo,sourceView,targetView);
		if (null == predPackage) {
			// 无前置包新建汇总基线
			summaryBaseline = new ATSummaryBaseline();
			summaryBaseline.setProdContext((ProductContext) contextInfo
					.getContext());
			summaryBaseline.setSourceView(sourceView);
			summaryBaseline.setTargetView(targetView);
			PersistHelper.getService().save(summaryBaseline);
			publishPackage.setSummaryBaseline(summaryBaseline);
			/*
			tempSummaryBaseline = new ATSummaryBaseline();
			tempSummaryBaseline.setProdContext((ProductContext) contextInfo
					.getContext());
			tempSummaryBaseline.setSourceView(sourceView);
			tempSummaryBaseline.setTargetView(targetView);
			PersistHelper.getService().save(tempSummaryBaseline);
			publishPackage.setTempSummaryBaseline(tempSummaryBaseline);
			*/
		} else {
			//所有任务共享同一个SummaryBaseline，方便从任务上获取当前总的发布清单（用户接收后形成的总的发布清单）
			//summarybaseline会越来越大，因为它保存了所有发布任务的汇总结果
			publishPackage.setPredecessor(predPackage);
			publishPackage.setSummaryBaseline(predPackage.getSummaryBaseline());

			//每个任务都持有一个独立的临时基线，方便后续从任务上获取任务发布时的汇总情况（用户当时接收后的发布清单）
			//临时汇总任务则是越往后的任务其持有的临时汇总基线越大，应为它会继承之前发布任务中的临时汇总基线内容
			/*
			tempSummaryBaseline = new ATSummaryBaseline();
			tempSummaryBaseline.setProdContext((ProductContext) contextInfo
					.getContext());
			tempSummaryBaseline.setSourceView(sourceView);
			tempSummaryBaseline.setTargetView(targetView);
			PersistHelper.getService().save(tempSummaryBaseline);
			publishPackage.setTempSummaryBaseline(tempSummaryBaseline);
			*/
			//tempSummaryBaseline=predPackage.getTempSummaryBaseline();
			//清除关系
			//BaselineHelper.getService().clearBaseline(tempSummaryBaseline);
			//获取所有未拒绝任务的发布基线
			
			//获取发布包的发布基线
			//将当前所有发布包共享的发布汇总基线内容（此上下文下所有任务（相同转换视图）的发布内容）作为当前临时汇总基线的基础
			List<Baselined> snapshots=BaselineHelper.getService().getBaselineItems(predPackage.getSummaryBaseline());
			for (Baselined snapshot:snapshots) {
					//判断是否重新生成链接
				if(recreatelink)
				{	
					//如果是则不把链接对象添加到临时汇总基线，这样将导致对比时产生新建关系更改点
					if(!(snapshot instanceof ATLinkSnapshot))
					{
						//BaselineHelper.getService().addToBaseline(snapshot,tempSummaryBaseline);
					}
				}
				else
				{
					BaselineHelper.getService().addToBaseline(snapshot,tempSummaryBaseline);
				}
			}
			//publishPackage.setTempSummaryBaseline(predPackage.getSummaryBaseline());
		}
		PersistHelper.getService().save(publishPackage);

		// 4、生成更改项
		new ViewChangeItemBuilder().build(publishPackage,predPackage);
		
		// 5 更新发布汇总基线，将需要发布的对象合并到汇总基线中
		//PartPublishHelper.getReceiveService().updateSummaryBaseline(publishPackage);
		// 6、创建下达任务
		ATPublishTask task = (ATPublishTask) TaskHelper.getService().newTask(
				"ATPublishTask");
		task.setName(number);
		task.setPublishPackage(publishPackage);
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.setCanDecomposed(false);// 是否允许分解
		taskInfo.setCanSubmitAuto(true);// 能否自动提交
		User executor = (User) UserHelper.getService().findUser(aaUserInnerId);
		taskInfo.setExecutor(executor);// 执行人
		task.setTaskInfo(taskInfo);
		task.setContextInfo(contextInfo);
		RuleHelper.getService().init(task, contextInfo.getContext());
		PersistHelper.getService().save(task);
		//下达任务
		TaskHelper.getService().assignTask(task);
		//启动任务
		TaskHelper.getService().startTask(task);

	}
	
	//获取指定上下文下属于目标视图的前置可用发布包列表
	public static ATPublishPackage getPrePackages(ContextInfo contextInfo,View sourceView,View targetView)
	{
		DetachedCriteria criteria = DetachedCriteria
				.forClass(ATPublishPackage.class);
		criteria.add(Restrictions.eq("contextInfo.contextRef.innerId",
				contextInfo.getContext().getInnerId()));
		criteria.add(Restrictions.eq("sourceViewRef.innerId",
				sourceView.getInnerId()));
		criteria.add(Restrictions.eq("targetViewRef.innerId",
				targetView.getInnerId()));
		criteria.addOrder(Order.desc("manageInfo.createTime"));
		List<ATPublishPackage> predPackageList = (List<ATPublishPackage>) PersistHelper
				.getService().findByCriteria(criteria);
		for (ATPublishPackage pubPackage : predPackageList) {
			ATPublishTask preTask = ViewPublishHelper.getService().getPublishTask(pubPackage);
			if(preTask != null && !"被拒绝".equals(preTask.getLifeCycleInfo().getStateName()) && !"已终止".equals(preTask.getLifeCycleInfo().getStateName())){
				{
					return pubPackage;
				}
			}
		}
		return null;
	}
}
