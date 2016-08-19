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

/**部件接收执行器
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
		//初始化
		init();
		//处理新增更改点
		executePartAddChangePoint();
		//处理新增关系更改点、修改属性更改点、修改关系更改点
		executePartModifyChangePoint();
	}
	
	/**
	 * 初始化。清除日志信息
	 */
	private void init(){
		StringBuilder hql = null;
		//清除日志
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
	 * 处理新增部件更改点，新建目标视图部件
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
		
		//新建视图版本部件列表
		List<Part> successPartList = new ArrayList<Part>();
		//更改点应用成功更改点innerid列表
		List<String> successChangePointInnerIdList = new ArrayList<String>();
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			Part part = (Part)tempArray[0];
			PartAddChangePoint changePoint = (PartAddChangePoint)tempArray[1];
			successPartList.add(part);
			successChangePointInnerIdList.add(changePoint.getInnerId());
		}
		//批量转换视图
		List<Part> newSuccessPartList = newBranchForView(successPartList,publishPackage.getTargetView());
		//添加至发布包成员
		addMemberToPackage(newSuccessPartList, publishPackage, ViewChangeType.ViewChangeType_Add);
		//更新处理成功结果
		updateChangePointResult(PartAddChangePoint.class,successChangePointInnerIdList, true);
		
		//更新已经存在下游视图的更改点处理结果为 ‘已经存在下游部件’
		StringBuilder hql_exist = new StringBuilder();
		List<Object> paramList_exist = new ArrayList<Object>();
		hql_exist.append(" update PartAddChangePoint changepoint set changepoint.result = '已存在下游部件'");
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
	 * 查找目标视图部件，应用部件属性及关系更改点
	 */
	private void executePartModifyChangePoint(){
		//查找目标部件
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
				createViewChangeLog(temp.changeItem, temp.changeItem.getAffectedViewManageable(), "下游对等部件不存在");
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
	 * 将新增或者修改的对象添加至发布包
	 * @param viewManageable
	 * @param publishPackage
	 * @param changeType
	 */
	private void addMemberToPackage(List<? extends ViewManageable> viewManageableList, ATPublishPackage publishPackage, ViewChangeType changeType) {
		//TODO 视图服务需要提供批量转换视图
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
			hql.append(" update ").append(clazz.getName()).append(" t set t.result = '成功'");
			hql.append(" where ").append(QueryHelper.getService().buildInCondition(changePointInnerIdList, "t.innerId", true));
			Helper.getPersistService().bulkUpdate(hql.toString(), changePointInnerIdList.toArray());
		}
	}
	private List<PartChangeItem> buildPartChangeItem(){
		//TODO
		List<PartChangeItem> returnList = new ArrayList<PartReceiveExecutor.PartChangeItem>();
		StringBuilder hql = null;
		List<Object> paramList = null;
		//Map<受影响部件对象标示，更改项>
		Map<String,PartChangeItem> changeItemMap = new HashMap<String,PartChangeItem>();
		List result = null;
		
		//查找更改项及更改点
		//新增部件的关系更改点
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
		
		//修改部件的关系更改点
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
		
		//修改部件的属性更改点
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
		
		//查找目标部件
		Map<String,Part> sourceInnerIdTargetMap = new HashMap<String,Part>();
		Map<String,Part> sourceInnerIdTargetMap_checkout4Other = new HashMap<String,Part>();
		List<String> sourceInnerIdList = new ArrayList<String>();
		sourceInnerIdList.addAll(changeItemMap.keySet());
		
		//需要检出的部件
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
		//替换检出后的部件作为目标部件
		for(Entry<String, Part> tempEntry:sourceInnerIdTargetMap.entrySet()){
			Part workingPart = (Part)(Part)checkoutMap.get(tempEntry.getValue());
			workingList.add(workingPart);
			tempEntry.setValue(workingPart);
		}
		
		addMemberToPackage(workingList, publishPackage, ViewChangeType.ViewChangeType_Modify);
		//自己检出的
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
		
		//别人检出的
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
		
		//处理目标部件
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
			//如果目标工艺部件不为空，则更新关系
			if(targetPart!=null){
				ViewHelper.getEquivalentService().useLatestUpstreamVersion(targetPart);
			}
		}
		returnList.addAll(changeItemMap.values());
		return  returnList;
	}
	/**
	 * 执行部件属性更改点
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private boolean applyAttrChange(Part downStream,PartAttrChangePoint attrChangePoint) {
		String type = attrChangePoint.getType();
		if (type.equals(PartAttrChangePoint.PART_ATTR_CHANGE)) { // 本身属性更改点
			return applyPartAttrChange(downStream, attrChangePoint);
		} else if (type.equals(PartAttrChangePoint.USAGELINK_ATTR_CHANGE)) { // 使用关系属性更改点
			return applyPartUsageAttrChange(downStream, attrChangePoint);
		}
		return true;
	}
	/**
	 * 执行部件关系更改点
	 * 
	 * @param downStream
	 * @param linkChangePoint
	 */
	private boolean applyLinkChange(Part downStream,PartLinkChangePoint linkChangePoint) {
		String type = linkChangePoint.getType();
		if (type.equals(PartLinkChangePoint.PART_USAGE_CHANGE)) { // 使用关系更改点
			return applyPartUsageChange(downStream, linkChangePoint);
		} else if (type.equals(PartLinkChangePoint.PART_DESCRIBE_CHANGE)) { // 说明关系更改点
			return applyPartDescribeChange(downStream, linkChangePoint);
		} else if (type.equals(PartLinkChangePoint.PART_REFERENCE_CHANGE)) { // 参考关系更改点
			return applyPartReferenceChange(downStream, linkChangePoint);
		}
		return true;
	}
	/**
	 * 应用本身属性更改点
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private boolean applyPartAttrChange(Part downStream,PartAttrChangePoint attrChangePoint) {
		// 获取冲突
		PartAttrChangeConflict conflict = ViewConflictHelper.getService().getConflictByDownstream(attrChangePoint, downStream,PartAttrChangeConflict.class);
		if (conflict != null) { // 存在冲突
			PartChangeSolution solution = ConflictUtils.buildSolution(conflict.getSolution(), downStream);
			// 执行解决方案
			
			//临时解决802现场问题。
			if(solution == null){
				attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
				PartChangeUtils.changePartAttr(downStream, attrChangePoint);
			}else{
				solution.solve(conflict);
			}
			
		} else { // 不存在冲突
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changePartAttr(downStream, attrChangePoint);
		}
		return true;
	}

	/**
	 * 应用使用关系属性更改点
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
			updatePointResult(attrChangePoint, "未找到使用部件");
			return false;
		}
		// 修改使用关系
		Part uses = (Part) downStreamUsesPart;
		List<PartUsageLink> toModifyLinks = Helper.getPartService().getPartUsageLinksByFromAndTo(downStream,uses.getPartMaster());
		if (toModifyLinks == null || toModifyLinks.size()==0) {
			updatePointResult(attrChangePoint, "未找到使用关系");
			return false;
		}
		// 获取冲突
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
				//检查剩余的对象是否有没有分配的，如果有就随机使用其中的一条
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
	 * 应用使用关系更改
	 * 
	 * @param downStream
	 *            被更改的下游部件
	 * @param linkChangePoint
	 *            更改点
	 */
	private boolean applyPartUsageChange(Part downStream,PartLinkChangePoint linkChangePoint) {
		// 如果需要新增的部件不为空，则为新增使用关系
		if (linkChangePoint.getCreatedLinkRef() != null) {
			Object uniqueid=linkChangePoint.getCreatedLink().getExtAttr("uniqueid");
			//检查有没有已有的关系，如有救更新该关系的prelink，否则就新建
			List<PartUsageLink> links=PartHelper.getService().getPartUsageLinksByFromAndTo(downStream, (PartMaster)linkChangePoint.getCreatedLink().getTo());
			boolean foundempty=false;
			for (PartUsageLink link : links) {
				if(null==link.getExtAttr("prelink"))
				{
					System.out.println("找到一条未知来源的关系，将与当前创建的关系建立关联"+uniqueid);
					link.setExtAttr("prelink", uniqueid);
					com.bjsasc.plm.core.persist.PersistUtil.getService().update(link);
					foundempty=true;
					break;
				}
			}
			// 建立使用关系
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
					//TODO更新使用关系
					PartChangeUtils.deleteUsageLink(downStream, (PartMaster)linkChangePoint.getCreatedLink().getTo(), (PartUsageLink)linkChangePoint.getCreatedLinkRef().getObject());
					PartChangeUtils.createUsageLink(downStream, (PartMaster)linkChangePoint.getCreatedLink().getTo(), (PartUsageLink)linkChangePoint.getCreatedLinkRef().getObject());
				}
				else
				{
					PartChangeUtils.createUsageLink(downStream, (PartMaster)linkChangePoint.getCreatedLink().getTo(), (PartUsageLink)linkChangePoint.getCreatedLinkRef().getObject());
				}
			}
		}else if (linkChangePoint.getRemovedLinkRef() != null) {// 如果需要删除的部件不为空，则为删除使用关系
			// 删除使用关系
			PartChangeUtils.deleteUsageLink(downStream, (PartMaster)linkChangePoint.getRemovedLink().getTo(), (PartUsageLink)linkChangePoint.getRemovedLinkRef().getObject());
		}
		return true;
	}

	/**
	 * 应用说明关系更改
	 * 
	 * @param downStream
	 *            被更改的下游部件
	 * @param linkChangePoint
	 *            更改点
	 */
	private boolean applyPartDescribeChange(Part downStream,PartLinkChangePoint linkChangePoint) {
		// 如果为新增说明关系
		if (linkChangePoint.getCreatedLinkRef() != null) {
			// 查找该下游部件的冲突项
			PartLinkChangeConflict conflict = ViewConflictHelper.getService()
					.getConflictByDownstream(linkChangePoint, downStream,
							PartLinkChangeConflict.class);
			// 冲突存在
			if (conflict != null) {
				PartChangeSolution solution = ConflictUtils.buildSolution(conflict.getSolution(), downStream);
				solution.solve(conflict);
			} else { // 冲突不存在
				// 需要新增的说明文档
				Document decribeDoc = (Document) ((PartDecribeLink) linkChangePoint
						.getCreatedLink()).getDescribesObject();
				PartChangeUtils.createDescribeLink(downStream, decribeDoc);
			}
		}
		// 如果为删除说明关系
		if (linkChangePoint.getRemovedLinkRef() != null) {
			// 需要删除的说明文档
			Document decribeDoc = (Document) ((PartDecribeLink) linkChangePoint
					.getRemovedLink()).getDescribesObject();
			PartChangeUtils.deleteDescribeLink(downStream, decribeDoc);
		}
		return true;
	}

	/**
	 * 应用参考关系更改
	 * 
	 * @param downStream
	 *            被更改的下游部件
	 * @param linkChangePoint
	 *            更改点
	 */
	private boolean applyPartReferenceChange(Part downStream,PartLinkChangePoint linkChangePoint) {
		// 如果为新增参考关系
		if (linkChangePoint.getCreatedLinkRef() != null) {
			// 需要新增的参考文档主对象
			DocumentMaster referenceDocMaster = (DocumentMaster) ((PartReferenceLink) linkChangePoint
					.getCreatedLink()).getReferencesObject();
			PartChangeUtils.createReferenceLink(downStream, referenceDocMaster);
		}
		// 如果为删除参考关系
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
