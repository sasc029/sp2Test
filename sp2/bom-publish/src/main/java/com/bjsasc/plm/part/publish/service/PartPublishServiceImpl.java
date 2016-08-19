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

		//��ȡ��ѡ��Ƿ�ǿ�����·�����ϵ
		boolean recreatelink=ConflictUtils.CanReCreateLink;
		
		ATPublishBaseline publishBaseline = new ATPublishBaseline();// ��������
		ATSummaryBaseline summaryBaseline = null;// ���ܻ���
		ATSummaryBaseline tempSummaryBaseline = null;// ��ʱ���ܻ���
		ATPublishPackage publishPackage = new ATPublishPackage();// ������

		View targetView = (View) Helper.getPersistService().getObject(
				targetViewOid);
		Part rootPart = (Part) Helper.getPersistService().getObject(partOid);
		View sourceView = rootPart.getView();
		ContextInfo contextInfo = rootPart.getContextInfo();// ��Ʒ������
		PersistHelper.getService().save(publishBaseline);// �־û���������

		// 1����ʼ��������
		publishPackage.setContextInfo(contextInfo);
		publishPackage.setSourceView(sourceView);
		publishPackage.setTargetView(targetView);
		publishPackage.setNumber(number);// ��������
		publishPackage.setName(number);
		publishPackage.setPublishPart(rootPart);// ��������
		publishPackage.setPublishBaseline(publishBaseline);// ��������
		RuleHelper.getService().init(publishPackage, contextInfo.getContext());

		// 2 ���ֽ�bom ��װ���� ���뷢������
		List<Map<String, Object>> nodelist = DataUtil
				.JsonToList(convertNodeInfo);
		// �ռ�����
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
					// ��ȡ���Ӵ������
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
		
		// 3����ѯǰ�ÿ��÷������б�
		ATPublishPackage predPackage = getPrePackages(contextInfo,sourceView,targetView);
		if (null == predPackage) {
			// ��ǰ�ð��½����ܻ���
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
			//����������ͬһ��SummaryBaseline������������ϻ�ȡ��ǰ�ܵķ����嵥���û����պ��γɵ��ܵķ����嵥��
			//summarybaseline��Խ��Խ����Ϊ�����������з�������Ļ��ܽ��
			publishPackage.setPredecessor(predPackage);
			publishPackage.setSummaryBaseline(predPackage.getSummaryBaseline());

			//ÿ�����񶼳���һ����������ʱ���ߣ���������������ϻ�ȡ���񷢲�ʱ�Ļ���������û���ʱ���պ�ķ����嵥��
			//��ʱ������������Խ�������������е���ʱ���ܻ���Խ��ӦΪ����̳�֮ǰ���������е���ʱ���ܻ�������
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
			//�����ϵ
			//BaselineHelper.getService().clearBaseline(tempSummaryBaseline);
			//��ȡ����δ�ܾ�����ķ�������
			
			//��ȡ�������ķ�������
			//����ǰ���з���������ķ������ܻ������ݣ���������������������ͬת����ͼ���ķ������ݣ���Ϊ��ǰ��ʱ���ܻ��ߵĻ���
			List<Baselined> snapshots=BaselineHelper.getService().getBaselineItems(predPackage.getSummaryBaseline());
			for (Baselined snapshot:snapshots) {
					//�ж��Ƿ�������������
				if(recreatelink)
				{	
					//������򲻰����Ӷ�����ӵ���ʱ���ܻ��ߣ����������¶Ա�ʱ�����½���ϵ���ĵ�
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

		// 4�����ɸ�����
		new ViewChangeItemBuilder().build(publishPackage,predPackage);
		
		// 5 ���·������ܻ��ߣ�����Ҫ�����Ķ���ϲ������ܻ�����
		//PartPublishHelper.getReceiveService().updateSummaryBaseline(publishPackage);
		// 6�������´�����
		ATPublishTask task = (ATPublishTask) TaskHelper.getService().newTask(
				"ATPublishTask");
		task.setName(number);
		task.setPublishPackage(publishPackage);
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.setCanDecomposed(false);// �Ƿ�����ֽ�
		taskInfo.setCanSubmitAuto(true);// �ܷ��Զ��ύ
		User executor = (User) UserHelper.getService().findUser(aaUserInnerId);
		taskInfo.setExecutor(executor);// ִ����
		task.setTaskInfo(taskInfo);
		task.setContextInfo(contextInfo);
		RuleHelper.getService().init(task, contextInfo.getContext());
		PersistHelper.getService().save(task);
		//�´�����
		TaskHelper.getService().assignTask(task);
		//��������
		TaskHelper.getService().startTask(task);

	}
	
	//��ȡָ��������������Ŀ����ͼ��ǰ�ÿ��÷������б�
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
			if(preTask != null && !"���ܾ�".equals(preTask.getLifeCycleInfo().getStateName()) && !"����ֹ".equals(preTask.getLifeCycleInfo().getStateName())){
				{
					return pubPackage;
				}
			}
		}
		return null;
	}
}
