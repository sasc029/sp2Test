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
 * ��ͼ���շ�����
 * @author caorang
 * @since 2014-6-18
 */
public abstract class ViewReceiveServiceImpl implements ViewReceiveService {
	
	private Logger logger = Logger.getLogger(ViewReceiveServiceImpl.class);
	
	@Override
	public void executeAddChangeItem(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected) {
		// ��ոø�������־��¼
		clearViewChangeLogs(item);
		// ��ȡ��Ҫ��������ͼ�ܹ������
		applyAdd(targetView, item, selected);
	}
	
	@Override
	public void executeModifyChangeItem(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected) {
		// ��ոø�������־��¼
		clearViewChangeLogs(item);
		// ��ȡ��Ҫ�޸ĵ���ͼ�ܹ������
		ViewManageable upStreamViewManageable = item.getAffectedViewManageable();
		// ���˳���Ŀ����ͼ�е����µ�������ͼ����
		List<ViewManageable> viewManageables = ViewHelper
				.getEquivalentService().getDownstreamObjects(
						upStreamViewManageable);
		List<ViewManageable> result = ViewUtils.filterViewManageablesInView(
				viewManageables, targetView);
		List<ViewManageable> downStreamViewManageables = StructHelper
				.getService().findLastestIteration(result);
		// ����Ҳ���������ͼ������д����־
		if (downStreamViewManageables == null
				|| downStreamViewManageables.size() == 0) {
			createViewChangeLog(item, upStreamViewManageable, "���ζԵȲ���������");
			return;
		}
		// ������Ҫ�޸ĵ�������ͼ�ܹ������
		for (ViewManageable downStreamViewManageable : downStreamViewManageables) {
			// ���ʵ�����������ڹ�����ö����ܴ����ܿ�״̬
			if (downStreamViewManageable instanceof LifeCycleManaged) {
				LifeCycleService lifeCycleService = LifeCycleHelper.getService();
				if (lifeCycleService.isInFinalPhase((LifeCycleManaged) downStreamViewManageable)) {
					//д�������־
					createViewChangeLog(item, downStreamViewManageable, "���ܸ����ܿ��еĲ���");
					continue;
				}
			}
			VersionControlService versionControlService = VersionControlHelper.getService();
			if (downStreamViewManageable instanceof Workable) {
				// ����ö���Ϊ��������,������
				if (downStreamViewManageable.getIterationInfo()
						.getCheckoutState()
						.equals(VersionControlUtil.CHECKOUTSTATE_WORK)) {
					AAUserData modifier =  downStreamViewManageable.getIterationInfo().getModifier();
					AAUserData currentUser = ObjectModelUtil.getCurrentUser();
					// �ж��ǲ����ɱ��˼��
					if (!modifier.getInnerId().equals(currentUser.getInnerId())) {
						createViewChangeLog(item, downStreamViewManageable, "�����������˼��");
						continue;
					}
				} else {
					// �ж��Ƿ��ܼ��
					StringBuilder checkoutResult = new StringBuilder();
					boolean canCheckout = versionControlService.isCheckoutAllowed((Workable)downStreamViewManageable, checkoutResult);
					if (!canCheckout) {
						createViewChangeLog(item, downStreamViewManageable, checkoutResult.toString());
						continue;
					}
					downStreamViewManageable = (ViewManageable) versionControlService.checkout((Workable) downStreamViewManageable);
					// ����ǰ�޸ĵĲ����������������Ա�У�����ͳһ�����ͳһȡ�����
					addMemberToPackage(downStreamViewManageable, item.getPublishPackage(), ViewChangeType.ViewChangeType_Modify);
				}
			}
			applyModify(downStreamViewManageable, item, selected);
		}
	}
	
	/**
	 * ��ո�����־
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
	 * ����������־
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
     * Ӧ��Add������
     * @param toModifyViewManageable
     * @param item
     * @param selected 
     */
	protected abstract void applyAdd(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected);
	
	/**
     * Ӧ��Modify������
     * @param toModifyViewManageable
     * @param item
     * @param selected 
     */
	protected abstract void applyModify(ViewManageable downStream, ViewChangeItem item, Collection<ViewChangePoint> selected);
	
	/**
	 * �����������޸ĵĶ��������������
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
	 * �������ͻ�ȡ��������Ա�б�
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
	 * ����������Ա�б����
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
		// ����������Ѿ���ֹ�������ύ
		if (LifeCycleHelper.getService().isInFinalPhase(publishPackage)) {
			throw new PlmException(
					"plm.part.receive.commitChange.publishPackage.terminate",
					publishPackage.getNumber());
		}
		// ����Ϊ�ս�״̬
		setFinalPhasePhase(publishPackage);
		// ��ȡ������Ҫ������Ķ���
		List<ViewManageable> modifyMembers = getMembersFromPackage(publishPackage,
						ViewChangeType.ViewChangeType_Modify);
		VersionControlHelper.getService().checkin(ListUtil.format(modifyMembers,
				Workable.class));
		// ���·������� 
		updateSummaryBaseline(publishPackage);
	}

	@Override
	public void rollbackChange(ATPublishPackage publishPackage) {
		// ����������Ѿ���ֹ�����ٻع�
		if (LifeCycleHelper.getService().isInFinalPhase(publishPackage)) {
			throw new PlmException(
					"plm.part.receive.rollbackChange.publishPackage.terminate",
					publishPackage.getNumber());
		}
		// ����״̬Ϊ��ʼ״̬
		setInitialPhase(publishPackage);
		// ��ո�����־
		clearChangeResult(publishPackage);
		// ɾ���޸ĵķ�֧�汾
		List<ViewManageable> modifyMembers = getMembersFromPackage(publishPackage, ViewChangeType.ViewChangeType_Modify);
		for (ViewManageable vm : modifyMembers) {
			if (vm instanceof Workable) {
				VersionControlHelper.getService().undoCheckout((Workable) vm);
			}
		}
		/*// ɾ�������ķ�֧�汾
		List<ViewManageable> addMembers = getMembersFromPackage(publishPackage, ViewChangeType.ViewChangeType_Add);	
		for (ViewManageable vm : addMembers) {
			if (vm instanceof Workable) {
				undoRevise((Workable) vm);
			}
		}*/
		// ��ճ�Ա�б�
		clearPackageMembers(publishPackage);
	}

	/**
	 * ���÷�����Ϊ��ʼ̬
	 * 
	 * @param publishPackage
	 */
	private void setInitialPhase(ATPublishPackage publishPackage) {
		while (!LifeCycleHelper.getService().isInInitialPhase(publishPackage)) {
			LifeCycleHelper.getService().demote(publishPackage);
		}
	}

	/**
	 * ���÷�����Ϊ��ֹ̬
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
//			// TODO �Ƿ���֤�׳�����ʱ�쳣
//			return;
//		}
		List<Baselined> summaryArray;//���ܻ���Ƭ��
		
		
		List<BaselineMemberLink> summaryList = PartPublishHelper.getPublishService().getPartSnapshotInPublishBaseline(summaryBaseline);
		List<BaselineMemberLink> publishList = PartPublishHelper.getPublishService().getPartSnapshotInPublishBaseline(publishBaseline);
		
		
		//���������µ�����Ԫ��
		List<Baselined> publishBaselineds = new ArrayList<Baselined>();			
		for(BaselineMemberLink memberLink : publishList){
			publishBaselineds.add(memberLink.getBaselineItem());
		}
		boolean isCheckWorking=false;
		//�ж��״η���
		if(null == summaryList || summaryList.size() == 0){
			BaselineHelper.getService().addToBaseline(publishBaselineds, summaryBaseline, isCheckWorking);
		}else{
			List<Baselined> summaryBaselineds = new ArrayList<Baselined>();			
			for(BaselineMemberLink memberLink : summaryList){
				summaryBaselineds.add(memberLink.getBaselineItem());
			}
			summaryArray=getCompareList(summaryBaselineds,publishBaselineds);
			//�������
			Baselined existBaseline;
			for(Baselined summaryBaselined : summaryArray){
				//�������  ɾ�������£��粻���� ɾ����
				if ((existBaseline = getExistBaselined(summaryBaselined, publishBaselineds)) != null){
					BaselineHelper.getService().removeFromBaseline(summaryBaselined, summaryBaseline);
					BaselineHelper.getService().addToBaseline(existBaseline, summaryBaseline);
				}else{
					BaselineHelper.getService().removeFromBaseline(summaryBaselined, summaryBaseline);
				}
			}
			for(Baselined publishBaselined : publishBaselineds){
				//��������ھ����������ܻ��ߣ�
				if((existBaseline = getExistBaselined(publishBaselined, summaryArray)) == null){
					BaselineHelper.getService().addToBaseline(publishBaselined, summaryBaseline);
				}
			}
		}
	}
	
	
	//�ڼ������Ƿ���ڴ�Ԫ�أ����ղ��������link�� ���ش��ڵ�Ԫ��
	protected abstract Baselined getExistBaselined(Baselined one, List<Baselined> list);

	//��ȡ�ȶԼ��� summaryList ���ܻ�����Ԫ�ؼ���  publishBaseline ����������Ԫ�ؼ���
	protected abstract List<Baselined> getCompareList(List<Baselined> summaryList, 
			List<Baselined> publishList);

}
