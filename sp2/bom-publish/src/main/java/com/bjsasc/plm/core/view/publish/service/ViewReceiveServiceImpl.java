package com.bjsasc.plm.core.view.publish.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.bjsasc.platform.objectmodel.business.version.VersionControlUtil;
import com.bjsasc.platform.objectmodel.util.ObjectModelUtil;
import com.bjsasc.plm.core.baseline.BaselineHelper;
import com.bjsasc.plm.core.baseline.model.BaselineMemberLink;
import com.bjsasc.plm.core.baseline.model.Baselined;
import com.bjsasc.plm.core.lifecycle.LifeCycleHelper;
import com.bjsasc.plm.core.lifecycle.LifeCycleManaged;
import com.bjsasc.plm.core.lifecycle.LifeCycleService;
import com.bjsasc.plm.core.part.publish.PartPublishHelper;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.util.ListUtil;
import com.bjsasc.plm.core.util.LogUtil;
import com.bjsasc.plm.core.util.PlmException;
import com.bjsasc.plm.core.vc.VersionControlHelper;
import com.bjsasc.plm.core.vc.VersionControlService;
import com.bjsasc.plm.core.vc.model.Workable;
import com.bjsasc.plm.core.vc.struct.StructHelper;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewHelper;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.ViewReceiveService;
import com.bjsasc.plm.core.view.publish.model.ATPackageMemberLink;
import com.bjsasc.plm.core.view.publish.model.ATPublishBaseline;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATSummaryBaseline;
import com.bjsasc.plm.core.view.publish.model.ATViewChangeLog;
import com.bjsasc.plm.core.view.publish.model.ViewChangeType;
import com.bjsasc.plm.core.view.publish.util.ViewUtils;
import com.cascc.platform.aa.api.data.AAUserData;

/**
 * 视图接收服务类
 * @author caorang
 * @since 2014-6-18
 */
public abstract class ViewReceiveServiceImpl implements ViewReceiveService {
	
	private Logger logger = Logger.getLogger(ViewReceiveServiceImpl.class);
	
	@Override
	public void executeAddChangeItem(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected) {
		// 清空该更改项日志记录
		clearViewChangeLogs(item);
		// 获取需要新增的视图受管理对象
		applyAdd(targetView, item, selected);
	}
	
	@Override
	public void executeModifyChangeItem(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected) {
		// 清空该更改项日志记录
		clearViewChangeLogs(item);
		// 获取需要修改的视图受管理对象
		ViewManageable upStreamViewManageable = item.getAffectedViewManageable();
		// 过滤出在目标视图中的最新的下游视图部件
		List<ViewManageable> viewManageables = ViewHelper
				.getEquivalentService().getDownstreamObjects(
						upStreamViewManageable);
		List<ViewManageable> result = ViewUtils.filterViewManageablesInView(
				viewManageables, targetView);
		List<ViewManageable> downStreamViewManageables = StructHelper
				.getService().findLastestIteration(result);
		// 如果找不到下游视图对象，则写入日志
		if (downStreamViewManageables == null
				|| downStreamViewManageables.size() == 0) {
			createViewChangeLog(item, upStreamViewManageable, "下游对等部件不存在");
			return;
		}
		// 遍历需要修改的下游视图受管理对象
		for (ViewManageable downStreamViewManageable : downStreamViewManageables) {
			// 如果实现了生命周期管理，则该对象不能处于受控状态
			if (downStreamViewManageable instanceof LifeCycleManaged) {
				LifeCycleService lifeCycleService = LifeCycleHelper.getService();
				if (lifeCycleService.isInFinalPhase((LifeCycleManaged) downStreamViewManageable)) {
					//写入更改日志
					createViewChangeLog(item, downStreamViewManageable, "不能更改受控中的部件");
					continue;
				}
			}
			VersionControlService versionControlService = VersionControlHelper.getService();
			if (downStreamViewManageable instanceof Workable) {
				// 如果该对象为工作副本,则不需检出
				if (downStreamViewManageable.getIterationInfo()
						.getCheckoutState()
						.equals(VersionControlUtil.CHECKOUTSTATE_WORK)) {
					AAUserData modifier =  downStreamViewManageable.getIterationInfo().getModifier();
					AAUserData currentUser = ObjectModelUtil.getCurrentUser();
					// 判断是不是由本人检出
					if (!modifier.getInnerId().equals(currentUser.getInnerId())) {
						createViewChangeLog(item, downStreamViewManageable, "部件已由他人检出");
						continue;
					}
				} else {
					// 判断是否能检出
					StringBuilder checkoutResult = new StringBuilder();
					boolean canCheckout = versionControlService.isCheckoutAllowed((Workable)downStreamViewManageable, checkoutResult);
					if (!canCheckout) {
						createViewChangeLog(item, downStreamViewManageable, checkoutResult.toString());
						continue;
					}
					downStreamViewManageable = (ViewManageable) versionControlService.checkout((Workable) downStreamViewManageable);
					// 将当前修改的部件添加至发布包成员中，用来统一检入和统一取消检出
					addMemberToPackage(downStreamViewManageable, item.getPublishPackage(), ViewChangeType.ViewChangeType_Modify);
				}
			}
			applyModify(downStreamViewManageable, item, selected);
		}
	}
	
	/**
	 * 清空更改日志
	 * @param item
	 * @param message 
	 */
	private void clearViewChangeLogs(ViewChangeItem item) {
		logger.debug(LogUtil.beginMethod(item));
		if (item == null) {
			return;
		}
		String hql = "delete from ATViewChangeLog t where t.changeItemRef.classId =? and t.changeItemRef.innerId = ?";
		PersistHelper.getService().bulkUpdate(hql, item.getClassId(), item.getInnerId());
	}
	
	/**
	 * 创建更改日志
	 * @param item
	 * @param message 
	 */
	private void createViewChangeLog(ViewChangeItem item, ViewManageable toChange, String message) {
		ATViewChangeLog changeLog = new ATViewChangeLog();
		changeLog.setChangeItem(item);
		changeLog.setToChange(toChange);
		changeLog.setNote(message);
		PersistHelper.getService().save(changeLog);
	}
	
    protected void updatePointResult(ViewChangePoint point, String result) {
		point.setResult(result);
    	PersistHelper.getService().update(point);
	}
	
    protected abstract void clearChangeResult(ATPublishPackage publishPackage);
    
    /**
     * 应用Add更改项
     * @param toModifyViewManageable
     * @param item
     * @param selected 
     */
	protected abstract void applyAdd(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected);
	
	/**
     * 应用Modify更改项
     * @param toModifyViewManageable
     * @param item
     * @param selected 
     */
	protected abstract void applyModify(ViewManageable downStream, ViewChangeItem item, Collection<ViewChangePoint> selected);
	
	/**
	 * 将新增或者修改的对象添加至发布包
	 * @param viewManageable
	 * @param publishPackage
	 * @param changeType
	 */
	public void addMemberToPackage(ViewManageable viewManageable, ATPublishPackage publishPackage, ViewChangeType changeType) {
		ATPackageMemberLink memberLink = new ATPackageMemberLink();
		memberLink.setViewManageable(viewManageable);
		memberLink.setPublishPackage(publishPackage);
		memberLink.setChangeType(changeType);
		PersistHelper.getService().save(memberLink);
	}
	
	/**
	 * 根据类型获取发布包成员列表
	 * @param publishPackage
	 * @param changeType
	 */
	public List<ViewManageable> getMembersFromPackage(ATPublishPackage publishPackage, ViewChangeType changeType) {
        logger.debug(LogUtil.beginMethod(publishPackage));
		if (publishPackage == null || publishPackage == null) {
			return null;
		}
		return ATPackageMemberLink.getTo(publishPackage, changeType);
	}
	
	/**
	 * 将发布包成员列表清空
	 * @param publishPackage
	 */
	public void clearPackageMembers(ATPublishPackage publishPackage) {
		logger.debug(LogUtil.beginMethod(publishPackage));
		if(publishPackage == null){
			return;
		}
		PersistHelper.getService().bulkUpdate("delete from ATPackageMemberLink t where t.fromObjectRef.innerId=?", publishPackage.getInnerId());
	}
	
	@Override
	public void commitChange(ATPublishPackage publishPackage) {
		// 如果发布包已经终止则不能再提交
		if (LifeCycleHelper.getService().isInFinalPhase(publishPackage)) {
			throw new PlmException(
					"plm.part.receive.commitChange.publishPackage.terminate",
					publishPackage.getNumber());
		}
		// 设置为终结状态
		setFinalPhasePhase(publishPackage);
		// 获取所有需要被检入的对象
		List<ViewManageable> modifyMembers = getMembersFromPackage(publishPackage,
						ViewChangeType.ViewChangeType_Modify);
		VersionControlHelper.getService().checkin(ListUtil.format(modifyMembers,
				Workable.class));
		// 更新发布基线 
		updateSummaryBaseline(publishPackage);
	}

	@Override
	public void rollbackChange(ATPublishPackage publishPackage) {
		// 如果发布包已经终止则不能再回滚
		if (LifeCycleHelper.getService().isInFinalPhase(publishPackage)) {
			throw new PlmException(
					"plm.part.receive.rollbackChange.publishPackage.terminate",
					publishPackage.getNumber());
		}
		// 设置状态为初始状态
		setInitialPhase(publishPackage);
		// 清空更改日志
		clearChangeResult(publishPackage);
		// 删除修改的分支版本
		List<ViewManageable> modifyMembers = getMembersFromPackage(publishPackage, ViewChangeType.ViewChangeType_Modify);
		for (ViewManageable vm : modifyMembers) {
			if (vm instanceof Workable) {
				VersionControlHelper.getService().undoCheckout((Workable) vm);
			}
		}
		/*// 删除新增的分支版本
		List<ViewManageable> addMembers = getMembersFromPackage(publishPackage, ViewChangeType.ViewChangeType_Add);	
		for (ViewManageable vm : addMembers) {
			if (vm instanceof Workable) {
				undoRevise((Workable) vm);
			}
		}*/
		// 清空成员列表
		clearPackageMembers(publishPackage);
	}

	/**
	 * 设置发布包为初始态
	 * 
	 * @param publishPackage
	 */
	private void setInitialPhase(ATPublishPackage publishPackage) {
		while (!LifeCycleHelper.getService().isInInitialPhase(publishPackage)) {
			LifeCycleHelper.getService().demote(publishPackage);
		}
	}

	/**
	 * 设置发布包为终止态
	 * 
	 * @param publishPackage
	 */
	private void setFinalPhasePhase(ATPublishPackage publishPackage) {
		while (!LifeCycleHelper.getService().isInFinalPhase(publishPackage)) {
			LifeCycleHelper.getService().promote(publishPackage);
		}
	}
	
	@Override
	public void updateSummaryBaseline(ATPublishPackage publishPackage) {
		
		ATPublishBaseline publishBaseline=publishPackage.getPublishBaseline();
		ATSummaryBaseline summaryBaseline = publishPackage.getSummaryBaseline();
		
//		if(null == publishBaseline || null == summaryBaseline){
//			// TODO 非法验证抛出运行时异常
//			return;
//		}
		List<Baselined> summaryArray;//汇总基线片段
		
		
		List<BaselineMemberLink> summaryList = PartPublishHelper.getPublishService().getPartSnapshotInPublishBaseline(summaryBaseline);
		List<BaselineMemberLink> publishList = PartPublishHelper.getPublishService().getPartSnapshotInPublishBaseline(publishBaseline);
		
		
		//发布基线下的所有元素
		List<Baselined> publishBaselineds = new ArrayList<Baselined>();			
		for(BaselineMemberLink memberLink : publishList){
			publishBaselineds.add(memberLink.getBaselineItem());
		}
		boolean isCheckWorking=false;
		//判断首次发布
		if(null == summaryList || summaryList.size() == 0){
			BaselineHelper.getService().addToBaseline(publishBaselineds, summaryBaseline, isCheckWorking);
		}else{
			List<Baselined> summaryBaselineds = new ArrayList<Baselined>();			
			for(BaselineMemberLink memberLink : summaryList){
				summaryBaselineds.add(memberLink.getBaselineItem());
			}
			summaryArray=getCompareList(summaryBaselineds,publishBaselineds);
			//正反向比
			Baselined existBaseline;
			for(Baselined summaryBaselined : summaryArray){
				//如果存在  删除、更新；如不存在 删除；
				if ((existBaseline = getExistBaselined(summaryBaselined, publishBaselineds)) != null){
					BaselineHelper.getService().removeFromBaseline(summaryBaselined, summaryBaseline);
					BaselineHelper.getService().addToBaseline(existBaseline, summaryBaseline);
				}else{
					BaselineHelper.getService().removeFromBaseline(summaryBaselined, summaryBaseline);
				}
			}
			for(Baselined publishBaselined : publishBaselineds){
				//如果不存在就新增到汇总基线；
				if((existBaseline = getExistBaselined(publishBaselined, summaryArray)) == null){
					BaselineHelper.getService().addToBaseline(publishBaselined, summaryBaseline);
				}
			}
		}
	}
	
	
	//在集合中是否存在此元素（快照部件或快照link） 返回存在的元素
	protected abstract Baselined getExistBaselined(Baselined one, List<Baselined> list);

	//获取比对集合 summaryList 汇总基线中元素集合  publishBaseline 发布基线中元素集合
	protected abstract List<Baselined> getCompareList(List<Baselined> summaryList, 
			List<Baselined> publishList);

}
