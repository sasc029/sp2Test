package com.bjsasc.plm.part.publish.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import com.bjsasc.platform.objectmodel.business.version.VersionControlUtil;
import com.bjsasc.plm.Helper;
import com.bjsasc.plm.core.doc.Document;
import com.bjsasc.plm.core.doc.DocumentMaster;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartHelper;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.PartStandardConfigSpec;
import com.bjsasc.plm.core.part.link.PartDecribeLink;
import com.bjsasc.plm.core.part.link.PartReferenceLink;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.part.publish.conflict.PartChangeSolution;
import com.bjsasc.plm.core.part.publish.model.PartAddChangeItem;
import com.bjsasc.plm.core.part.publish.model.PartAddChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartModifyChangeItem;
import com.bjsasc.plm.core.part.publish.util.ConflictUtils;
import com.bjsasc.plm.core.part.publish.util.PartChangeUtils;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.PersistUtil;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.query.QueryHelper;
import com.bjsasc.plm.core.vc.VersionControlHelper;
import com.bjsasc.plm.core.vc.model.Workable;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewHelper;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.ViewConflictHelper;
import com.bjsasc.plm.core.view.publish.model.ATPackageMemberLink;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATViewChangeLog;
import com.bjsasc.plm.core.view.publish.model.ViewChangeType;

/**��������ִ����
 * @author sunzhengzhu
 *
 */
public class PartReceiveExecutor {
	
	private ATPublishPackage publishPackage = null;
	private List<String> changePointInnerIdList = new ArrayList<String>();
	
	public PartReceiveExecutor(String packageOid,List<String> changePointOIDList){
		this.publishPackage = (ATPublishPackage) PersistHelper.getService().getObject(packageOid);
		for(String oid:changePointOIDList){
			this.changePointInnerIdList.add(Helper.getInnerId(oid));
		}
	}
	
	public void execute(){
		//��ʼ��
		init();
		//�����������ĵ�
		executePartAddChangePoint();
		//����������ϵ���ĵ㡢�޸����Ը��ĵ㡢�޸Ĺ�ϵ���ĵ�
		executePartModifyChangePoint();
	}
	
	/**
	 * ��ʼ���������־��Ϣ
	 */
	private void init(){
		StringBuilder hql = null;
		//�����־
		hql = new StringBuilder();
		hql.append(" delete from ATViewChangeLog t ");
		hql.append(" where t.changeItemRef.innerId in (");
		hql.append(" 	select changepoint.viewChangeItemRef.innerId from PartAddChangePoint changepoint");
		hql.append("	where ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "changepoint.innerId", true));
		hql.append(" )");
		Helper.getPersistService().bulkUpdate(hql.toString(),changePointInnerIdList.toArray());
		
		hql = new StringBuilder();
		hql.append(" delete from ATViewChangeLog t ");
		hql.append(" where t.changeItemRef.innerId in (");
		hql.append(" 	select changepoint.viewChangeItemRef.innerId from PartAttrChangePoint changepoint");
		hql.append("	where ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "changepoint.innerId", true));
		hql.append(" )");
		Helper.getPersistService().bulkUpdate(hql.toString(),changePointInnerIdList.toArray());
		
		hql = new StringBuilder();
		hql.append(" delete from ATViewChangeLog t ");
		hql.append(" where t.changeItemRef.innerId in (");
		hql.append(" 	select changepoint.viewChangeItemRef.innerId from PartLinkChangePoint changepoint");
		hql.append("	where ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "changepoint.innerId", true));
		hql.append(" )");
		Helper.getPersistService().bulkUpdate(hql.toString(),changePointInnerIdList.toArray());
	}
	/**
	 * ���������������ĵ㣬�½�Ŀ����ͼ����
	 */
	private void executePartAddChangePoint(){
		StringBuilder hql = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		hql.append(" select part,changepoint from Part part,PartAddChangePoint changepoint");
		hql.append(" where part.innerId = changepoint.createdPartRef.innerId");
		hql.append(" and ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "changepoint.innerId", true));
		paramList.addAll(changePointInnerIdList);
		hql.append(" and 0 =(");
		hql.append(" 	select count(*) from Part part1");
		hql.append("	where part1.masterRef.innerId = part.masterRef.innerId");
		hql.append("	and part1.viewRef.innerId = ?");
		paramList.add(publishPackage.getTargetViewRef().getInnerId());
		hql.append(" )");
		List result = Helper.getPersistService().find(hql.toString(),paramList.toArray());
		
		//�½���ͼ�汾�����б�
		List<Part> successPartList = new ArrayList<Part>();
		//���ĵ�Ӧ�óɹ����ĵ�innerid�б�
		List<String> successChangePointInnerIdList = new ArrayList<String>();
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			Part part = (Part)tempArray[0];
			PartAddChangePoint changePoint = (PartAddChangePoint)tempArray[1];
			successPartList.add(part);
			successChangePointInnerIdList.add(changePoint.getInnerId());
		}
		//����ת����ͼ
		List<Part> newSuccessPartList = newBranchForView(successPartList,publishPackage.getTargetView());
		//�������������Ա
		addMemberToPackage(newSuccessPartList, publishPackage, ViewChangeType.ViewChangeType_Add);
		//���´���ɹ����
		updateChangePointResult(PartAddChangePoint.class,successChangePointInnerIdList, true);
		
		//�����Ѿ�����������ͼ�ĸ��ĵ㴦����Ϊ ���Ѿ��������β�����
		StringBuilder hql_exist = new StringBuilder();
		List<Object> paramList_exist = new ArrayList<Object>();
		hql_exist.append(" update PartAddChangePoint changepoint set changepoint.result = '�Ѵ������β���'");
		hql_exist.append(" where ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "changepoint.innerId", true));
		paramList_exist.addAll(changePointInnerIdList);
		hql_exist.append(" and 0 <(");
		hql_exist.append(" 	select count(*) from Part part1,Part part");
		hql_exist.append("	where part1.masterRef.innerId = part.masterRef.innerId");
		hql_exist.append("	and part1.viewRef.innerId = ?");
		hql_exist.append("	and part.innerId = changepoint.createdPartRef.innerId ");
		paramList_exist.add(publishPackage.getTargetViewRef().getInnerId());
		hql_exist.append(" )");
		Helper.getPersistService().bulkUpdate(hql_exist.toString(),paramList_exist.toArray());
	}
	/**
	 * ����Ŀ����ͼ������Ӧ�ò������Լ���ϵ���ĵ�
	 */
	private void executePartModifyChangePoint(){
		//����Ŀ�겿��
		List<PartChangeItem> changeItemList = buildPartChangeItem();
		
		List<String> successViewChangePointList_attr = new ArrayList<String>();
		List<String> successViewChangePointList_link = new ArrayList<String>();
		
		for(PartChangeItem temp:changeItemList){
			if(temp.target!=null){
				for(ViewChangePoint tempChangePoint:temp.changePointList){
					if(tempChangePoint instanceof PartAttrChangePoint){
						if(applyAttrChange(temp.target, (PartAttrChangePoint)tempChangePoint)){
							successViewChangePointList_attr.add(tempChangePoint.getInnerId());
						}
					}else if(tempChangePoint instanceof PartLinkChangePoint){
						if(applyLinkChange(temp.target, (PartLinkChangePoint)tempChangePoint)){
							successViewChangePointList_link.add(tempChangePoint.getInnerId());
						}
					}
				}
			}else{
				createViewChangeLog(temp.changeItem, temp.changeItem.getAffectedViewManageable(), "���ζԵȲ���������");
			}
		}
		updateChangePointResult(PartAttrChangePoint.class, successViewChangePointList_attr, true);
		updateChangePointResult(PartLinkChangePoint.class, successViewChangePointList_link, true);
	}
	private void createViewChangeLog(ViewChangeItem item, ViewManageable toChange, String message) {
		ATViewChangeLog changeLog = new ATViewChangeLog();
		changeLog.setChangeItem(item);
		changeLog.setToChange(toChange);
		changeLog.setNote(message);
		PersistHelper.getService().save(changeLog);
	}
	/**
	 * �����������޸ĵĶ��������������
	 * @param viewManageable
	 * @param publishPackage
	 * @param changeType
	 */
	private void addMemberToPackage(List<? extends ViewManageable> viewManageableList, ATPublishPackage publishPackage, ViewChangeType changeType) {
		//TODO ��ͼ������Ҫ�ṩ����ת����ͼ
		for(ViewManageable viewManageable:viewManageableList ){
			ATPackageMemberLink memberLink = new ATPackageMemberLink();
			memberLink.setViewManageable(viewManageable);
			memberLink.setPublishPackage(publishPackage);
			memberLink.setChangeType(changeType);
			PersistUtil.getService().save(memberLink);
		}
	}
	private List<Part> newBranchForView(List<Part> ViewManageableList,View targetView){
		List<Part> returnList = new ArrayList<Part>();
		returnList.addAll(Helper.getViewService().newBranchForView(ViewManageableList,publishPackage.getTargetView()).values()) ;
		return returnList;
	}
	private void updateChangePointResult(Class clazz,List<String> changePointInnerIdList,boolean isSuccess){
		if(changePointInnerIdList!=null&&changePointInnerIdList.size()>0){
			StringBuilder hql = new StringBuilder();
			hql.append(" update ").append(clazz.getName()).append(" t set t.result = '�ɹ�'");
			hql.append(" where ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "t.innerId", true));
			Helper.getPersistService().bulkUpdate(hql.toString(), changePointInnerIdList.toArray());
		}
	}
	private List<PartChangeItem> buildPartChangeItem(){
		//TODO
		List<PartChangeItem> returnList = new ArrayList<PartReceiveExecutor.PartChangeItem>();
		StringBuilder hql = null;
		List<Object> paramList = null;
		//Map<��Ӱ�첿�������ʾ��������>
		Map<String,PartChangeItem> changeItemMap = new HashMap<String,PartChangeItem>();
		List result = null;
		
		//���Ҹ�������ĵ�
		//���������Ĺ�ϵ���ĵ�
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" select part,changeitem,changepoint from Part part, PartAddChangeItem changeitem,PartLinkChangePoint changepoint");
		hql.append(" where part.innerId = changeitem.affectedViewManageableRef.innerId");
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		hql.append(" and ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "changepoint.innerId", true));
		paramList.addAll(changePointInnerIdList);
		
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			Part part = (Part)tempArray[0];
			PartAddChangeItem changeitem = (PartAddChangeItem)tempArray[1];
			PartLinkChangePoint changepoint = (PartLinkChangePoint)tempArray[2];
			PartChangeItem changeItem = changeItemMap.get(changeitem.getAffectedViewManageableRef().getInnerId());
			if(changeItem==null){
				changeItem = new PartChangeItem();
				changeItem.changeItem = changeitem;
				changeitem.setAffectedViewManageable(part);
				
				changeItemMap.put(changeitem.getAffectedViewManageableRef().getInnerId(),changeItem);
			}
			changeItem.changePointList.add(changepoint);
		}
		
		//�޸Ĳ����Ĺ�ϵ���ĵ�
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" select part,changeitem,changepoint from Part part, PartModifyChangeItem changeitem,PartLinkChangePoint changepoint");
		hql.append(" where part.innerId = changeitem.affectedViewManageableRef.innerId");
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		hql.append(" and ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "changepoint.innerId", true));
		paramList.addAll(changePointInnerIdList);
		
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			Part part = (Part)tempArray[0];
			PartModifyChangeItem changeitem = (PartModifyChangeItem)tempArray[1];
			PartLinkChangePoint changepoint = (PartLinkChangePoint)tempArray[2];
			PartChangeItem changeItem = changeItemMap.get(changeitem.getAffectedViewManageableRef().getInnerId());
			if(changeItem==null){
				changeItem = new PartChangeItem();
				changeItem.changeItem = changeitem;
				changeitem.setAffectedViewManageable(part);
				
				changeItemMap.put(changeitem.getAffectedViewManageableRef().getInnerId(),changeItem);
			}
			changeItem.changePointList.add(changepoint);
		}
		
		//�޸Ĳ��������Ը��ĵ�
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" select part,changeitem,changepoint from Part part, PartModifyChangeItem changeitem,PartAttrChangePoint changepoint");
		hql.append(" where part.innerId = changeitem.affectedViewManageableRef.innerId");
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		hql.append(" and ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "changepoint.innerId", true));
		paramList.addAll(changePointInnerIdList);
		/*hql.append(" and 0= (");
		hql.append("	select count(*) from Part targetpart");
		hql.append("	where targetpart.masterRef.innerId = part.masterRef.innerId");
		hql.append("	and targetpart.viewRef.innerId = ?");
		paramList.add(publishPackage.getTargetViewRef().getInnerId());
		hql.append(" )");*/
		
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			Part part = (Part)tempArray[0];
			PartModifyChangeItem changeitem = (PartModifyChangeItem)tempArray[1];
			PartAttrChangePoint changepoint = (PartAttrChangePoint)tempArray[2];
			PartChangeItem changeItem = changeItemMap.get(changeitem.getAffectedViewManageableRef().getInnerId());
			if(changeItem==null){
				changeItem = new PartChangeItem();
				changeItem.changeItem = changeitem;
				changeitem.setAffectedViewManageable(part);
				
				changeItemMap.put(changeitem.getAffectedViewManageableRef().getInnerId(),changeItem);
			}
			changeItem.changePointList.add(changepoint);
		}
		
		//����Ŀ�겿��
		Map<String,Part> sourceInnerIdTargetMap = new HashMap<String,Part>();
		Map<String,Part> sourceInnerIdTargetMap_checkout4Other = new HashMap<String,Part>();
		List<String> sourceInnerIdList = new ArrayList<String>();
		sourceInnerIdList.addAll(changeItemMap.keySet());
		
		//��Ҫ����Ĳ���
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		
		hql.append(" select part.innerId,targetpart from Part part,Part targetpart,com.bjsasc.platform.objectmodel.business.version.ControlBranch controlbranch");
		hql.append(" where part.masterRef.innerId = targetpart.masterRef.innerId");
		hql.append(" and targetpart.viewRef.innerId = ?");
		paramList.add(publishPackage.getTargetViewRef().getInnerId());
		hql.append(" and targetpart.iterationInfo.latestInBranch = '1'");
		hql.append(" and targetpart.iterationInfo.checkoutState = '"+VersionControlUtil.CHECKOUTSTATE_IN+"'");
		hql.append(" and targetpart.iterationInfo.controlBranchRef.innerId = controlbranch.innerId");
		hql.append(" and controlbranch.latestBranch = '1'");
		hql.append(" and ").append(Helper.getQueryService().buildInCondition(sourceInnerIdList, "part.innerId", true));
		paramList.addAll(sourceInnerIdList);
		
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		
		List<Workable> checkoutList = new ArrayList<Workable>();
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			String sourceInnerId = (String)tempArray[0];
			Part targetPart = (Part)tempArray[1];
			sourceInnerIdTargetMap.put(sourceInnerId,targetPart);
			if(!checkoutList.contains(targetPart)){
				checkoutList.add(targetPart);
			}
		}
		
		Map<Workable, Workable> checkoutMap =  VersionControlHelper.getService().checkout(checkoutList);
		
		List<Part> workingList = new ArrayList<Part>();
		//�滻�����Ĳ�����ΪĿ�겿��
		for(Entry<String, Part> tempEntry:sourceInnerIdTargetMap.entrySet()){
			Part workingPart = (Part)(Part)checkoutMap.get(tempEntry.getValue());
			workingList.add(workingPart);
			tempEntry.setValue(workingPart);
		}
		
		addMemberToPackage(workingList, publishPackage, ViewChangeType.ViewChangeType_Modify);
		//�Լ������
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		
		hql.append(" select part.innerId,targetpart from Part part,Part targetpart");
		hql.append(" where part.masterRef.innerId = targetpart.masterRef.innerId");
		hql.append(" and targetpart.viewRef.innerId = ?");
		paramList.add(publishPackage.getTargetViewRef().getInnerId());
		hql.append(" and targetpart.iterationInfo.checkoutState = '"+VersionControlUtil.CHECKOUTSTATE_WORK+"'");
		hql.append(" and targetpart.iterationInfo.modifierRef.innerId = ?");
		paramList.add(Helper.getSessionService().getUser().getAaUserInnerId());
		hql.append(" and ").append(Helper.getQueryService().buildInCondition(sourceInnerIdList, "part.innerId", true));
		paramList.addAll(sourceInnerIdList);
		
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			String sourceInnerId = (String)tempArray[0];
			Part targetPart = (Part)tempArray[1];
			sourceInnerIdTargetMap.put(sourceInnerId,targetPart);
		}
		
		//���˼����
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		
		hql.append(" select part.innerId,targetpart from Part part,Part targetpart");
		hql.append(" where part.masterRef.innerId = targetpart.masterRef.innerId");
		hql.append(" and targetpart.viewRef.innerId = ?");
		paramList.add(publishPackage.getTargetViewRef().getInnerId());
		hql.append(" and targetpart.iterationInfo.checkoutState = '"+VersionControlUtil.CHECKOUTSTATE_OUT+"'");
		hql.append(" and targetpart.iterationInfo.lockerRef.innerId <> ?");
		paramList.add(Helper.getSessionService().getUser().getAaUserInnerId());
		hql.append(" and ").append(Helper.getQueryService().buildInCondition(sourceInnerIdList, "part.innerId", true));
		paramList.addAll(sourceInnerIdList);
		
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			String sourceInnerId = (String)tempArray[0];
			Part targetPart = (Part)tempArray[1];
			sourceInnerIdTargetMap_checkout4Other.put(sourceInnerId,targetPart);
		}
		
		//����Ŀ�겿��
		for(Entry<String,PartChangeItem> tempEntry:changeItemMap.entrySet()){
			Part targetPart = sourceInnerIdTargetMap.get(tempEntry.getKey());
			if(targetPart!=null){
				tempEntry.getValue().target = targetPart;
			}else{
				targetPart = sourceInnerIdTargetMap_checkout4Other.get(tempEntry.getKey());
				if(targetPart!=null){
					tempEntry.getValue().target = targetPart;
					tempEntry.getValue().isCheckoutByOther = true;
				}
			}
			//���Ŀ�깤�ղ�����Ϊ�գ�����¹�ϵ
			if(targetPart!=null){
				ViewHelper.getEquivalentService().useLatestUpstreamVersion(targetPart);
			}
		}
		returnList.addAll(changeItemMap.values());
		return  returnList;
	}
	/**
	 * ִ�в������Ը��ĵ�
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private boolean applyAttrChange(Part downStream,PartAttrChangePoint attrChangePoint) {
		String type = attrChangePoint.getType();
		if (type.equals(PartAttrChangePoint.PART_ATTR_CHANGE)) { // �������Ը��ĵ�
			return applyPartAttrChange(downStream, attrChangePoint);
		} else if (type.equals(PartAttrChangePoint.USAGELINK_ATTR_CHANGE)) { // ʹ�ù�ϵ���Ը��ĵ�
			return applyPartUsageAttrChange(downStream, attrChangePoint);
		}
		return true;
	}
	/**
	 * ִ�в�����ϵ���ĵ�
	 * 
	 * @param downStream
	 * @param linkChangePoint
	 */
	private boolean applyLinkChange(Part downStream,PartLinkChangePoint linkChangePoint) {
		String type = linkChangePoint.getType();
		if (type.equals(PartLinkChangePoint.PART_USAGE_CHANGE)) { // ʹ�ù�ϵ���ĵ�
			return applyPartUsageChange(downStream, linkChangePoint);
		} else if (type.equals(PartLinkChangePoint.PART_DESCRIBE_CHANGE)) { // ˵����ϵ���ĵ�
			return applyPartDescribeChange(downStream, linkChangePoint);
		} else if (type.equals(PartLinkChangePoint.PART_REFERENCE_CHANGE)) { // �ο���ϵ���ĵ�
			return applyPartReferenceChange(downStream, linkChangePoint);
		}
		return true;
	}
	/**
	 * Ӧ�ñ������Ը��ĵ�
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private boolean applyPartAttrChange(Part downStream,PartAttrChangePoint attrChangePoint) {
		// ��ȡ��ͻ
		PartAttrChangeConflict conflict = ViewConflictHelper.getService().getConflictByDownstream(attrChangePoint, downStream,PartAttrChangeConflict.class);
		if (conflict != null) { // ���ڳ�ͻ
			PartChangeSolution solution = ConflictUtils.buildSolution(conflict.getSolution(), downStream);
			// ִ�н������
			
			//��ʱ���802�ֳ����⡣
			if(solution == null){
				attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
				PartChangeUtils.changePartAttr(downStream, attrChangePoint);
			}else{
				solution.solve(conflict);
			}
			
		} else { // �����ڳ�ͻ
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changePartAttr(downStream, attrChangePoint);
		}
		return true;
	}

	/**
	 * Ӧ��ʹ�ù�ϵ���Ը��ĵ�
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private boolean applyPartUsageAttrChange(Part downStream,PartAttrChangePoint attrChangePoint) {
		PartMaster toPartMaster = attrChangePoint.getAffectedLink().getUsesObject();
		
		PartStandardConfigSpec configspec = new PartStandardConfigSpec();
		configspec.setView(attrChangePoint.getTargetView());
		
		Persistable downStreamUsesPart =  Helper.getConfigSpecService().filteredIterationsOf(toPartMaster, configspec);
		if (downStreamUsesPart == null || downStreamUsesPart instanceof PartMaster) {
			updatePointResult(attrChangePoint, "δ�ҵ�ʹ�ò���");
			return false;
		}
		// �޸�ʹ�ù�ϵ
		Part uses = (Part) downStreamUsesPart;
		List<PartUsageLink> toModifyLinks = Helper.getPartService().getPartUsageLinksByFromAndTo(downStream,uses.getPartMaster());
		if (toModifyLinks == null || toModifyLinks.size()==0) {
			updatePointResult(attrChangePoint, "δ�ҵ�ʹ�ù�ϵ");
			return false;
		}
		// ��ȡ��ͻ
		PartAttrChangeConflict conflict = ViewConflictHelper.getService().getConflictByDownstream(attrChangePoint, uses,PartAttrChangeConflict.class);
		if (conflict != null) {
			for (PartUsageLink link : toModifyLinks) {
				Object uniqueid=attrChangePoint.getAffectedLink().getExtAttr("uniqueid");
				if((null!=uniqueid)&&(uniqueid.equals(link.getExtAttr("prelink"))))
				{
					PartChangeSolution solution = ConflictUtils.buildSolution(conflict.getSolution(), link);
					solution.solve(conflict);
				}
			}
		} else {
			boolean changed=false;
			String changelinkid="";
			Object uniqueid=attrChangePoint.getAffectedLink().getExtAttr("uniqueid");
			if(null!=uniqueid)
			{
				for (PartUsageLink link : toModifyLinks) {
					
					System.out.println("----------------");
					System.out.println(uniqueid);
					System.out.println(link.getExtAttr("prelink"));
					if((null!=uniqueid)&&(uniqueid.equals(link.getExtAttr("prelink"))))
					{
						attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
						PartChangeUtils.changeUsageLinkAttr(link, attrChangePoint);
						changelinkid=link.getInnerId();
						changed=true;
						break;
					}
				}
				//���ʣ��Ķ����Ƿ���û�з���ģ�����о����ʹ�����е�һ��
				if(!changed)
				{
					for (PartUsageLink link : toModifyLinks) {
						if(!link.getInnerId().equalsIgnoreCase(changelinkid))
						{
							if(null==link.getExtAttr("prelink"))
							{
								link.setExtAttr("prelink", uniqueid);
								com.bjsasc.plm.core.persist.PersistUtil.getService().update(link);
								attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
								PartChangeUtils.changeUsageLinkAttr(link, attrChangePoint);
								break;
							}
						}
					}
				}
				
			}
			
		}
		return true;
	}

	/**
	 * Ӧ��ʹ�ù�ϵ����
	 * 
	 * @param downStream
	 *            �����ĵ����β���
	 * @param linkChangePoint
	 *            ���ĵ�
	 */
	private boolean applyPartUsageChange(Part downStream,PartLinkChangePoint linkChangePoint) {
		// �����Ҫ�����Ĳ�����Ϊ�գ���Ϊ����ʹ�ù�ϵ
		if (linkChangePoint.getCreatedLinkRef() != null) {
			Object uniqueid=linkChangePoint.getCreatedLink().getExtAttr("uniqueid");
			//�����û�����еĹ�ϵ�����оȸ��¸ù�ϵ��prelink��������½�
			List<PartUsageLink> links=PartHelper.getService().getPartUsageLinksByFromAndTo(downStream, (PartMaster)linkChangePoint.getCreatedLink().getTo());
			boolean foundempty=false;
			for (PartUsageLink link : links) {
				if(null==link.getExtAttr("prelink"))
				{
					System.out.println("�ҵ�һ��δ֪��Դ�Ĺ�ϵ�����뵱ǰ�����Ĺ�ϵ��������"+uniqueid);
					link.setExtAttr("prelink", uniqueid);
					com.bjsasc.plm.core.persist.PersistUtil.getService().update(link);
					foundempty=true;
					break;
				}
			}
			// ����ʹ�ù�ϵ
			if(!foundempty)
			{
				boolean foundExist=false;
				for (PartUsageLink link : links) {
					if(null!=link.getExtAttr("prelink"))
					{
						foundExist=true;
					}
				}
				if(foundExist)
				{
					//TODO����ʹ�ù�ϵ
					PartChangeUtils.deleteUsageLink(downStream, (PartMaster)linkChangePoint.getCreatedLink().getTo(), (PartUsageLink)linkChangePoint.getCreatedLinkRef().getObject());
					PartChangeUtils.createUsageLink(downStream, (PartMaster)linkChangePoint.getCreatedLink().getTo(), (PartUsageLink)linkChangePoint.getCreatedLinkRef().getObject());
				}
				else
				{
					PartChangeUtils.createUsageLink(downStream, (PartMaster)linkChangePoint.getCreatedLink().getTo(), (PartUsageLink)linkChangePoint.getCreatedLinkRef().getObject());
				}
			}
		}else if (linkChangePoint.getRemovedLinkRef() != null) {// �����Ҫɾ���Ĳ�����Ϊ�գ���Ϊɾ��ʹ�ù�ϵ
			// ɾ��ʹ�ù�ϵ
			PartChangeUtils.deleteUsageLink(downStream, (PartMaster)linkChangePoint.getRemovedLink().getTo(), (PartUsageLink)linkChangePoint.getRemovedLinkRef().getObject());
		}
		return true;
	}

	/**
	 * Ӧ��˵����ϵ����
	 * 
	 * @param downStream
	 *            �����ĵ����β���
	 * @param linkChangePoint
	 *            ���ĵ�
	 */
	private boolean applyPartDescribeChange(Part downStream,PartLinkChangePoint linkChangePoint) {
		// ���Ϊ����˵����ϵ
		if (linkChangePoint.getCreatedLinkRef() != null) {
			// ���Ҹ����β����ĳ�ͻ��
			PartLinkChangeConflict conflict = ViewConflictHelper.getService()
					.getConflictByDownstream(linkChangePoint, downStream,
							PartLinkChangeConflict.class);
			// ��ͻ����
			if (conflict != null) {
				PartChangeSolution solution = ConflictUtils.buildSolution(conflict.getSolution(), downStream);
				solution.solve(conflict);
			} else { // ��ͻ������
				// ��Ҫ������˵���ĵ�
				Document decribeDoc = (Document) ((PartDecribeLink) linkChangePoint
						.getCreatedLink()).getDescribesObject();
				PartChangeUtils.createDescribeLink(downStream, decribeDoc);
			}
		}
		// ���Ϊɾ��˵����ϵ
		if (linkChangePoint.getRemovedLinkRef() != null) {
			// ��Ҫɾ����˵���ĵ�
			Document decribeDoc = (Document) ((PartDecribeLink) linkChangePoint
					.getRemovedLink()).getDescribesObject();
			PartChangeUtils.deleteDescribeLink(downStream, decribeDoc);
		}
		return true;
	}

	/**
	 * Ӧ�òο���ϵ����
	 * 
	 * @param downStream
	 *            �����ĵ����β���
	 * @param linkChangePoint
	 *            ���ĵ�
	 */
	private boolean applyPartReferenceChange(Part downStream,PartLinkChangePoint linkChangePoint) {
		// ���Ϊ�����ο���ϵ
		if (linkChangePoint.getCreatedLinkRef() != null) {
			// ��Ҫ�����Ĳο��ĵ�������
			DocumentMaster referenceDocMaster = (DocumentMaster) ((PartReferenceLink) linkChangePoint
					.getCreatedLink()).getReferencesObject();
			PartChangeUtils.createReferenceLink(downStream, referenceDocMaster);
		}
		// ���Ϊɾ���ο���ϵ
		if (linkChangePoint.getRemovedLinkRef() != null) {
			DocumentMaster referenceDocMaster = (DocumentMaster) ((PartReferenceLink) linkChangePoint
					.getRemovedLink()).getReferencesObject();
			PartChangeUtils.deleteReferenceLink(downStream, referenceDocMaster);
		}
		return true;
	}
	private void updatePointResult(ViewChangePoint point, String result) {
		point.setResult(result);
    	PersistHelper.getService().update(point);
	}

	class PartChangeItem{
		public ViewChangeItem changeItem;
		public Part target;
		public List<ViewChangePoint> changePointList = new ArrayList<ViewChangePoint>();
		public boolean isCheckoutByOther = false;
	}
}
