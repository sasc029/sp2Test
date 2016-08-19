package com.bjsasc.plm.part.publish.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Pattern;

import com.bjsasc.plm.Helper;
import com.bjsasc.plm.core.context.model.LibraryContext;
import com.bjsasc.plm.core.doc.Document;
import com.bjsasc.plm.core.doc.DocumentMaster;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.Quantity;
import com.bjsasc.plm.core.part.link.PartDecribeLink;
import com.bjsasc.plm.core.part.link.PartReferenceLink;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.part.publish.PartPublishHelper;
import com.bjsasc.plm.core.part.publish.PartPublishService;
import com.bjsasc.plm.core.part.publish.model.PartAddChangeItem;
import com.bjsasc.plm.core.part.publish.model.PartAddChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartModifyChangeItem;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.PersistUtil;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.query.QueryHelper;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.type.TypeHelper;
import com.bjsasc.plm.core.vc.VersionControlHelper;
import com.bjsasc.plm.core.vc.model.Iterated;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;
import com.bjsasc.plm.core.view.publish.model.ViewChangePointDealType;
import com.bjsasc.plm.type.attr.Attr;

public class ViewChangeItemBuilder {
	
	private PartPublishService partPublishService = PartPublishHelper
			.getPublishService();
	
	public void build(ATPublishPackage publishPackage,ATPublishPackage predPackage) {
		//查询初次转换的部件(用当前发布基线与所属上下文【相同转换视图】下的汇总基线内容比较)
		Collection<ATSnapshot> createsnapshotList = getPartSnapShot4add(publishPackage);
		//批量创建【新增更改点】
		createViewChangePoint4Add(createsnapshotList,publishPackage);
		if(createsnapshotList.size()==0){
			//查询前置发布包中未发布的更改点
			Map<ViewChangeItem,List<ViewChangePoint>> changeItemChangePointMap = new ViewChangeItemTreeNodeBuilder().getViewChangeItem(predPackage);
			//批量创建【新增更改点】
			createViewChangePoint4PrePackageAdd(changeItemChangePointMap,publishPackage);
		}
		//查询非初次转换的部件(用当前发布基线与所属上下文【相同转换视图】下的汇总基线内容比较)
		Map<ATSnapshot,ATSnapshot> updatesnapshotList=getPartSnapShot4update(publishPackage);
		//批量创建【修改更改点】
		createViewChangePoint4Update(updatesnapshotList,publishPackage,predPackage);
	}
	

	/**查询初次转换的部件
	 * @param publishPackage
	 * @return
	 */
	private Collection<ATSnapshot> getPartSnapShot4add(ATPublishPackage publishPackage){
		StringBuilder hql = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		hql.append(" select part ,snapshot from Part part,ATSnapshot snapshot,BaselineMemberLink link");
		hql.append(" where part.innerId = snapshot.viewManageableRef.innerId");
		hql.append(" and snapshot.innerId = link.toObjectRef.innerId");
		hql.append(" and link.fromObjectRef.innerId = ?");
		paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
		hql.append(" and 0=(");
		hql.append(" 	select count(*) from Part part1,ATSnapshot snapshot1,BaselineMemberLink link1");
		hql.append(" 	where part1.innerId = snapshot1.viewManageableRef.innerId");
		hql.append(" 	and snapshot1.innerId = link1.toObjectRef.innerId");
		hql.append(" 	and link1.fromObjectRef.innerId = ?");
		hql.append("	and part.masterRef.innerId = part1.masterRef.innerId");
		paramList.add(publishPackage.getSummaryBaselineRef().getInnerId());
		hql.append(" )");
		hql.append(" and 0 = (");
		hql.append(" 	select  count(*) from PartAddChangeItem changeitem");
		hql.append(" 	where changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
		hql.append(" 	and changeitem.affectedViewManageableRef.innerId = part.innerId");
		hql.append(")");
		List result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		Map<String,ATSnapshot> snapshotList = new HashMap<String,ATSnapshot>();
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			Part part  = (Part)tempArray[0];
			if(!snapshotList.containsKey(part.getInnerId()))
			{
				ATSnapshot snapshot = (ATSnapshot)tempArray[1];
				snapshot.setViewManageable(part);
				snapshotList.put(part.getInnerId(),snapshot);
			}
		}
		return snapshotList.values();
	}
	/**批量创建更改点
	 * @param createdSnapshot
	 * @param publishBaseline
	 */
	private void createViewChangePoint4Add(Collection<ATSnapshot> createdSnapshot,ATPublishPackage publishPackage){
		if(createdSnapshot==null||createdSnapshot.size()<=0){
			return;
		}
		StringBuilder tempTable = getTempTable(createdSnapshot);
		if(tempTable.toString().trim().isEmpty()) return;
		StringBuilder sql = new StringBuilder();
		List<Object> paramList = null;
		long updateTime = System.currentTimeMillis();
		
		//部件新增更改项
		sql.append(" insert into PLM_PART_PUB_ADDITEM");
		sql.append(" (INNERID,CLASSID,AFFECTEDVIEWMANAGEABLECLASSID,AFFECTEDVIEWMANAGEABLEID,PUBLISHPACKAGECLASSID,PUBLISHPACKAGEID,UPDATETIME,UPDATECOUNT)");
		sql.append(" select t.INNERID,'PartAddChangeItem',t.VIEWCLASSID,t.VIEWID,'"+publishPackage.getClassId()+"','"+publishPackage.getInnerId()+"',"+updateTime+",0");
		sql.append(" from (");
		sql.append(tempTable);
		sql.append(" ) t");
		PersistHelper.getService().bulkUpdateBySql(sql.toString());
		
		//部件新增更改点
		sql = new StringBuilder();
		sql.append(" insert into PLM_PART_PUB_ADDPOINT");
		sql.append(" (INNERID,CLASSID,VIEWCHANGEITEMCLASSID,VIEWCHANGEITEMID,ISCONFLICT,NOTE,RESULT,CREATEDPARTCLASSID,CREATEDPARTID,UPDATETIME,UPDATECOUNT)");
		sql.append(" select sys_guid(),'PartAddChangePoint','PartAddChangeItem',t.INNERID,0,'新发布','',t.VIEWCLASSID,t.VIEWID,"+updateTime+",0");
		sql.append(" from (");
		sql.append(tempTable);
		sql.append(" ) t");
		PersistHelper.getService().bulkUpdateBySql(sql.toString());
		
		//参考关系更改点
		sql = new StringBuilder();
		paramList = new ArrayList<Object>();
		sql.append(" insert into PLM_PART_PUB_LINKPOINT");
		sql.append(" (INNERID,CLASSID,VIEWCHANGEITEMCLASSID,VIEWCHANGEITEMID,ISCONFLICT,NOTE,RESULT,CREATEDLINKCLASSID,CREATEDLINKID,LINKCHANGETYPE,UPDATETIME,UPDATECOUNT)");
		sql.append(" select sys_guid(),'PartLinkChangePoint','PartAddChangeItem',t.INNERID,0,'新增参考文档'||master.name,'',link.classid,link.innerid,'"+PartLinkChangePoint.PART_REFERENCE_CHANGE+"',"+updateTime+",0");
		sql.append(" from plm_doc_DOCUMENTMASTER master,PLM_PART_REFERENCELINK link,PLM_VIEW_PUB_LINKSNAPSHOT snapshot,PLM_BASELINE_MEMBERLINK memberlink,(");
		sql.append(tempTable);
		sql.append(" ) t");
		sql.append(" where t.viewid = link.fromobjectid ");
		sql.append(" and link.innerid = snapshot.LINKID");
		sql.append(" and snapshot.innerid = memberlink.toobjectid");
		sql.append(" and memberlink.fromobjectid = ?");
		sql.append(" and master.innerid = link.toobjectid");
		paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
		PersistHelper.getService().bulkUpdateBySql(sql.toString(),paramList.toArray());
		
		//描述关系更改点
		sql = new StringBuilder();
		paramList = new ArrayList<Object>();
		sql.append(" insert into PLM_PART_PUB_LINKPOINT");
		sql.append(" (INNERID,CLASSID,VIEWCHANGEITEMCLASSID,VIEWCHANGEITEMID,ISCONFLICT,NOTE,RESULT,CREATEDLINKCLASSID,CREATEDLINKID,LINKCHANGETYPE,UPDATETIME,UPDATECOUNT)");
		sql.append(" select sys_guid(),'PartLinkChangePoint','PartAddChangeItem',t.INNERID,0,'新增描述文档'||master.name,'',link.classid,link.innerid,'"+PartLinkChangePoint.PART_DESCRIBE_CHANGE+"',"+updateTime+",0");
		sql.append(" from plm_doc_DOCUMENTMASTER master,plm_doc_DOCUMENT doc,PLM_PART_DECRIBELINK link,PLM_VIEW_PUB_LINKSNAPSHOT snapshot,PLM_BASELINE_MEMBERLINK memberlink,(");
		sql.append(tempTable);
		sql.append(" ) t");
		sql.append(" where t.viewid = link.fromobjectid ");
		sql.append(" and link.innerid = snapshot.LINKID");
		sql.append(" and snapshot.innerid = memberlink.toobjectid");
		sql.append(" and memberlink.fromobjectid = ?");
		sql.append(" and doc.innerid = link.toobjectid");
		sql.append(" and doc.masterid = master.innerid");
		paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
		PersistHelper.getService().bulkUpdateBySql(sql.toString(),paramList.toArray());
		
		//使用关系更改点
		sql = new StringBuilder();
		paramList = new ArrayList<Object>();
		sql.append(" insert into PLM_PART_PUB_LINKPOINT");
		sql.append(" (INNERID,CLASSID,VIEWCHANGEITEMCLASSID,VIEWCHANGEITEMID,ISCONFLICT,NOTE,RESULT,CREATEDLINKCLASSID,CREATEDLINKID,LINKCHANGETYPE,UPDATETIME,UPDATECOUNT)");
		sql.append(" select sys_guid(),'PartLinkChangePoint','PartAddChangeItem',t.INNERID,0,'新增子部件' ||master.name,'',link.classid,link.innerid,'"+PartLinkChangePoint.PART_USAGE_CHANGE+"',"+updateTime+",0");
		sql.append(" from PLM_PART_PARTMASTER master,PLM_PART_USAGELINK link,PLM_VIEW_PUB_LINKSNAPSHOT snapshot,PLM_BASELINE_MEMBERLINK memberlink,(");
		sql.append(tempTable);
		sql.append(" ) t");
		sql.append(" where t.viewid = link.fromobjectid ");
		sql.append(" and link.innerid = snapshot.LINKID");
		sql.append(" and snapshot.innerid = memberlink.toobjectid");
		sql.append(" and memberlink.fromobjectid = ?");
		sql.append(" and master.innerid = link.toobjectid");
		paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
		PersistHelper.getService().bulkUpdateBySql(sql.toString(),paramList.toArray());
		
	}
	
	
	//查询父部件的修改点（新增及修改【包括自身修改及关系修改】更改点）
	private Map<ATSnapshot,ATSnapshot> getPartSnapShot4update(ATPublishPackage publishPackage){
		//查询非首次转换的部件
		//查询当前发布基线中的部件是否存在其他版本对象在以前的发布基线中
		//返回的结果中，key为当前修改的部件版本，value为以前发布的该部件的其他版本
		StringBuilder hql = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		hql.append(" select part ,snapshot,part1,snapshot1 from Part part,ATSnapshot snapshot,BaselineMemberLink link,Part part1,ATSnapshot snapshot1,BaselineMemberLink link1");
		hql.append(" where part.innerId = snapshot.viewManageableRef.innerId");
		hql.append(" and snapshot.innerId = link.toObjectRef.innerId");
		hql.append(" and link.fromObjectRef.innerId = ?");
		paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
		hql.append(" and part1.innerId = snapshot1.viewManageableRef.innerId");
		hql.append(" and snapshot1.innerId = link1.toObjectRef.innerId");
		hql.append(" and link1.fromObjectRef.innerId = ?");
		hql.append(" and part.masterRef.innerId = part1.masterRef.innerId");
		hql.append(" and part.innerId <> part1.innerId");
		paramList.add(publishPackage.getSummaryBaselineRef().getInnerId());
		//检查是否有未处理的新增更改点
		hql.append(" and 0 = (");
		hql.append(" 	select  count(*) from PartAddChangeItem changeitem");
		hql.append(" 	where changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
		hql.append(" 	and changeitem.affectedViewManageableRef.innerId = part.innerId");
		hql.append(")");
		
		//检查是否有未处理的修改更改点
		hql.append(" and 0 = (");
		hql.append(" 	select  count(*) from PartModifyChangeItem changeitem");
		hql.append(" 	where changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
		hql.append(" 	and changeitem.affectedViewManageableRef.innerId = part.innerId");
		hql.append(")");
		
		List result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		Map<ATSnapshot,ATSnapshot> snapshotList = new HashMap<ATSnapshot,ATSnapshot>();
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			Part part  = (Part)tempArray[0];
			ATSnapshot snapshot = (ATSnapshot)tempArray[1];
			snapshot.setViewManageable(part);
			
			Part part1  = (Part)tempArray[2];
			ATSnapshot snapshot1 = (ATSnapshot)tempArray[3];
			snapshot1.setViewManageable(part1);
			
			snapshotList.put(snapshot,snapshot1);
		}
		return snapshotList;
	}
	/**
	 * @param createdSnapshot
	 * @param publishPackage
	 */
	private void createViewChangePoint4Update(Map<ATSnapshot,ATSnapshot> createdSnapshot,ATPublishPackage publishPackage,ATPublishPackage predPackage){
		//部件修改更改项
		if(createdSnapshot==null||createdSnapshot.size()<=0){
			return;
		}
		//Map<部件唯一标识,部件更改项>
		Map<String,PartModifyChangeItem> changeItemMap = new HashMap<String, PartModifyChangeItem>();
		//createdSnapshot集合中，key为当前需要修改的部件版本，value为以前曾近发布过的该部件其他版本
		for(ATSnapshot temp:createdSnapshot.keySet()){
			PartModifyChangeItem partModifyChangeItem = new PartModifyChangeItem();
			String uuid = UUID.randomUUID().toString();
			partModifyChangeItem.setInnerId(uuid);
			partModifyChangeItem.setAffectedViewManageableRef(temp.getViewManageableRef());
			partModifyChangeItem.setPublishPackage(publishPackage);
			changeItemMap.put(temp.getViewManageableRef().getInnerId(),partModifyChangeItem);
		}
		
		//Map<部件唯一标识,参考关系>发布
		Map<String,List<PartReferenceLink>> referenceMap_publish = getATLinkInBaseline(createdSnapshot.keySet(), PartReferenceLink.class, publishPackage.getPublishBaselineRef().getInnerId());
		
		//Map<部件唯一标识,参考关系>汇总
		Map<String,List<PartReferenceLink>> referenceMap_temp = getATLinkInBaseline(createdSnapshot.values(),PartReferenceLink.class,publishPackage.getSummaryBaselineRef().getInnerId());
		
		//Map<部件唯一标识,描述关系>发布
		Map<String,List<PartDecribeLink>> decribeMap_publish = getATLinkInBaseline(createdSnapshot.keySet(), PartDecribeLink.class, publishPackage.getPublishBaselineRef().getInnerId());
		//Map<部件唯一标识,描述关系>汇总
		Map<String,List<PartDecribeLink>> decribeMap_temp = getATLinkInBaseline(createdSnapshot.values(), PartDecribeLink.class, publishPackage.getSummaryBaselineRef().getInnerId());
		
		//Map<部件唯一标识,使用关系>发布，获取当前发布任务需要发布的使用关系
		Map<String,List<PartUsageLink>> usageMap_publish = getATLinkInBaseline(createdSnapshot.keySet(), PartUsageLink.class, publishPackage.getPublishBaselineRef().getInnerId());
		//Map<部件唯一标识,使用关系>汇总，获取上一个发布（非拒绝）任务需要发布的使用关系
		Map<String,List<PartUsageLink>> usageMap_pre_publish;
		if(predPackage==null)
		{
			usageMap_pre_publish=new HashMap<String,List<PartUsageLink>>();
		}
		else
		{
			//注意：必须要获取上一个包含当前发布部件的可以发布任务
			usageMap_pre_publish= getATLinkInBaseline(createdSnapshot.values(), PartUsageLink.class, publishPackage.getSummaryBaselineRef().getInnerId());
		}
		//循环遍历以前发布的该部件的历史版本temp
		for(Entry<ATSnapshot,ATSnapshot> temp: createdSnapshot.entrySet()){
			ATSnapshot publishSnapshot = temp.getKey();
			PartModifyChangeItem changeItem = changeItemMap.get(publishSnapshot.getViewManageableRef().getInnerId());
			if(changeItem==null){
				continue;
			}
			ATSnapshot summarySnapshot = temp.getValue();
			List<ViewChangePoint> viewChangePoints = new ArrayList<ViewChangePoint>();
			Part publishPart = (Part)publishSnapshot.getViewManageable();
			Part summaryPart = (Part)summarySnapshot.getViewManageable();
			
			/*朱对象属性变更，无条件接收不生成更改点
			 * //主对象属性更改点
			if(!publishSnapshot.getMasterInfo().getMasterName().equals(summarySnapshot.getMasterInfo().getMasterName())){
				PartAttrChangePoint partAttrChangePoint = new PartAttrChangePoint();
				partAttrChangePoint.setType(PartAttrChangePoint.MASTER_ATTR_CHANGE);
				partAttrChangePoint.setAttrId("NAME");
				partAttrChangePoint.setAfterContent(publishSnapshot.getMasterInfo().getMasterName());
				partAttrChangePoint.setBeforeContent(summarySnapshot.getMasterInfo().getMasterName());
				String note = "名称:"+summarySnapshot.getMasterInfo().getMasterName()+"→"+publishSnapshot.getMasterInfo().getMasterName();
				partAttrChangePoint.setNote(note);
				partAttrChangePoint.setViewChangeItem(changeItem);
				viewChangePoints.add(partAttrChangePoint);
			}
			if(!publishSnapshot.getMasterInfo().getMasterNumber().equals(summarySnapshot.getMasterInfo().getMasterNumber())){
				PartAttrChangePoint partAttrChangePoint1 = new PartAttrChangePoint();
				partAttrChangePoint1.setType(PartAttrChangePoint.MASTER_ATTR_CHANGE);
				partAttrChangePoint1.setAttrId("NUMBER");
				partAttrChangePoint1.setAfterContent(publishSnapshot.getMasterInfo().getMasterNumber());
				partAttrChangePoint1.setBeforeContent(summarySnapshot.getMasterInfo().getMasterNumber());
				String note1 = "编号:"+summarySnapshot.getMasterInfo().getMasterNumber()+"→"+publishSnapshot.getMasterInfo().getMasterNumber();
				partAttrChangePoint1.setNote(note1);
				partAttrChangePoint1.setViewChangeItem(changeItem);
				viewChangePoints.add(partAttrChangePoint1);
			}*/
			//自身属性更改点
			
			List<Attr> attrs = Helper.getTypeManager().getTypeAttrs("Part", "view_publish");
			Map<String,Object> values = Helper.getTypeManager().format(publishPart, "view_publish", false);
			Map<String,Object> values1 = Helper.getTypeManager().format(summaryPart, "view_publish", false);
			List<Persistable> parts = new Vector<Persistable>();
			parts.add(summaryPart);
			parts.add(publishPart);	

			for(Attr attr:attrs){
				if(!isEqual(attr, parts)){
					PartAttrChangePoint partAttrChangePoint2 = new PartAttrChangePoint();
					partAttrChangePoint2.setType(PartAttrChangePoint.PART_ATTR_CHANGE);
					partAttrChangePoint2.setAttrId(attr.getId());
					partAttrChangePoint2.setDisplayName(attr.getName());
					Serializable value =  (Serializable)getAttributeValueInclueAll(attr, publishPart);
					partAttrChangePoint2.setAfterContent(value);
					
					Serializable value1 = (Serializable)getAttributeValueInclueAll(attr, summaryPart);
					partAttrChangePoint2.setBeforeContent(value1);
					
					Object afterValue = values.get(attr.getId());
					Object beforeValue = values1.get(attr.getId());
					String attrname = attr.getName();
					String note = attrname+":"+beforeValue+"→"+afterValue;
					
					partAttrChangePoint2.setNote(note);
					partAttrChangePoint2.setViewChangeItem(changeItem);
					viewChangePoints.add(partAttrChangePoint2);
				}
			}
			//参考关系更改点
			List<PartReferenceLink> publishReferenceLinks = referenceMap_publish.get(publishPart.getInnerId());
			List<PartReferenceLink> summaryReferenceLinks = referenceMap_temp.get(summaryPart.getInnerId());
			//临时解决
			if(publishReferenceLinks==null){
				publishReferenceLinks = new ArrayList<PartReferenceLink>();
			}
			if(summaryReferenceLinks==null){
				summaryReferenceLinks = new ArrayList<PartReferenceLink>();
			}
			
			for(PartReferenceLink partReferenceLink:summaryReferenceLinks){
				boolean isExisted = false;
				for(PartReferenceLink partReferenceLink2:publishReferenceLinks){
					if(partReferenceLink.getToObjectRef().getInnerId().equals(partReferenceLink2.getToObjectRef().getInnerId())){
						isExisted = true;
						publishReferenceLinks.remove(partReferenceLink2);
						break;
					}
				}
				if(isExisted==false){
					PartLinkChangePoint referenceLinkChangePoint = new PartLinkChangePoint();
					referenceLinkChangePoint.setType(PartLinkChangePoint.PART_REFERENCE_CHANGE);
					referenceLinkChangePoint.setRemovedLink(partReferenceLink);
					DocumentMaster documentMaster = (DocumentMaster)partReferenceLink.getReferencesObject();
					String name = documentMaster.getName();
					String note = "删除参考文档:"+name;
					referenceLinkChangePoint.setNote(note);
					referenceLinkChangePoint.setViewChangeItem(changeItem);
					viewChangePoints.add(referenceLinkChangePoint);
				}
			}
			for(PartReferenceLink partReferenceLink:publishReferenceLinks){
				PartLinkChangePoint referenceLinkChangePoint = new PartLinkChangePoint();
				referenceLinkChangePoint.setType(PartLinkChangePoint.PART_REFERENCE_CHANGE);
				referenceLinkChangePoint.setCreatedLink(partReferenceLink);
				DocumentMaster documentMaster = (DocumentMaster)partReferenceLink.getReferencesObject();
				String name = documentMaster.getName();
				String note = "新增参考文档:"+name;
				referenceLinkChangePoint.setNote(note);
				referenceLinkChangePoint.setViewChangeItem(changeItem);
				viewChangePoints.add(referenceLinkChangePoint);
			}
			//说明关系更改点
			List<PartDecribeLink> summaryPartDecribeLinks = decribeMap_temp.get(summaryPart.getInnerId());
			List<PartDecribeLink> publishPartDecribeLinks = decribeMap_publish.get(publishPart.getInnerId());
			
			//临时解决
			if(summaryPartDecribeLinks==null){
				summaryPartDecribeLinks = new ArrayList<PartDecribeLink>();
			}
			if(publishPartDecribeLinks==null){
				publishPartDecribeLinks = new ArrayList<PartDecribeLink>();
			}
			
			for(PartDecribeLink partDecribeLink:summaryPartDecribeLinks){
				int i = 0;
				for(PartDecribeLink partDecribeLink2:publishPartDecribeLinks){
					if(partDecribeLink.getDescribesObject().getMasterRef().getInnerId().equals(partDecribeLink2.getDescribesObject().getMasterRef().getInnerId())){
						break;
					}
					i = i + 1;
				}
				if(i == publishPartDecribeLinks.size()){
					PartLinkChangePoint decribeLinkChangePoint = new PartLinkChangePoint();
					decribeLinkChangePoint.setType(PartLinkChangePoint.PART_DESCRIBE_CHANGE);
					decribeLinkChangePoint.setRemovedLink(partDecribeLink);
					Document document = (Document)partDecribeLink.getTo();
					DocumentMaster documentMaster = (DocumentMaster)document.getMaster();
					String name = documentMaster.getName();
					String note = "移除描述文档:"+name;
					decribeLinkChangePoint.setNote(note);
					viewChangePoints.add(decribeLinkChangePoint);
				}
			}
			for(PartDecribeLink partDecribeLink:publishPartDecribeLinks){
				int i = 0;
				for(PartDecribeLink partDecribeLink2:summaryPartDecribeLinks){
					if(partDecribeLink.getDescribesObject().getMaster().getInnerId().equals(partDecribeLink2.getDescribesObject().getMaster().getInnerId())){
						if(partDecribeLink.getDescribesObject().getInnerId().equals(partDecribeLink2.getDescribesObject().getInnerId())){
							break;
						}
						else{
							PartLinkChangePoint decribeLinkChangePoint = new PartLinkChangePoint();
							decribeLinkChangePoint.setType(PartLinkChangePoint.PART_DESCRIBE_CHANGE);
							decribeLinkChangePoint.setRemovedLink(partDecribeLink2);
							decribeLinkChangePoint.setCreatedLink(partDecribeLink);
							Document publishDocument = (Document)partDecribeLink.getTo();
							Document summaryDocument = (Document)partDecribeLink2.getTo();
							DocumentMaster publishDocumentMaster = (DocumentMaster)publishDocument.getMaster();
							String publishName = publishDocumentMaster.getName();
							String publishVersion =  publishDocument.getIterationInfo().getFullVersionNo();
							String summaryVersion =  summaryDocument.getIterationInfo().getFullVersionNo();
							String note = "描述文档修改:"+publishName+summaryVersion+"→"+publishVersion;
							decribeLinkChangePoint.setNote(note);
							viewChangePoints.add(decribeLinkChangePoint);
							break;
						}
					}
					else{
						i = i + 1;
					}
				}
				if(i == summaryPartDecribeLinks.size()){
					PartLinkChangePoint decribeLinkChangePoint = new PartLinkChangePoint();
					decribeLinkChangePoint.setType(PartLinkChangePoint.PART_DESCRIBE_CHANGE);
					decribeLinkChangePoint.setCreatedLink(partDecribeLink);
					Document document = (Document)partDecribeLink.getTo();
					DocumentMaster documentMaster = (DocumentMaster)document.getMaster();
					String name = documentMaster.getName();
					String note = "新增描述文档:"+name;
					decribeLinkChangePoint.setNote(note);
					viewChangePoints.add(decribeLinkChangePoint);
				}
			}	
			//使用关系更改点

			//使用关系更改点
			//根据历史发布任务中包含的该部件历史版本，找到以前的发布关系
			List<PartUsageLink> prepublishPartUsageLinks = usageMap_pre_publish.get(summaryPart.getInnerId());
			//根据当前发布任务中包含的该部件版本，找到对应的发布关系
			List<PartUsageLink> publishPartUsageLinks = usageMap_publish.get(publishPart.getInnerId());
			
			//临时解决
			if(prepublishPartUsageLinks==null){
				prepublishPartUsageLinks = new ArrayList<PartUsageLink>();
			}
			if(publishPartUsageLinks==null){
				publishPartUsageLinks = new ArrayList<PartUsageLink>();
			}
			
			//以下属于核心的使用关系比较逻辑
			//会把当前需要发布的关系和上次发布时包含的关系（注意：不是用户接收发布任务时选择的关系）进行比较
			//如果有新的，说明需要新增关系
			//如果没有，说明需要删除关系
			for(PartUsageLink partUsageLink:prepublishPartUsageLinks){
				boolean isExisted = false;
				String lastuniqueid=(String) partUsageLink.getExtAttr("uniqueid");
				for(PartUsageLink partUsageLink2:publishPartUsageLinks){
					String uniqueid=(String) partUsageLink2.getExtAttr("uniqueid");
					if((lastuniqueid!=null)&&(lastuniqueid.equalsIgnoreCase(uniqueid))){
						//使用关系属性更改点
						List<Attr> attrs1 = Helper.getTypeManager().getTypeAttrs("PartUsageLink", "view_publish");
						List<Persistable> partUsagelinks = new ArrayList<Persistable>();
						partUsagelinks.add(partUsageLink);
						partUsagelinks.add(partUsageLink2);
						for(Attr attr:attrs1){
							if(!isEqual(attr, partUsagelinks)){
								PartAttrChangePoint viewChangePoint = new PartAttrChangePoint();
								viewChangePoint.setType(PartAttrChangePoint.USAGELINK_ATTR_CHANGE);
								Object summaryObj = getAttributeValueInclueAll(attr, partUsageLink);
								Object publishObj = getAttributeValueInclueAll(attr, partUsageLink2);
								
								if(publishObj instanceof Serializable){
									Serializable publishSerializable = (Serializable)publishObj;
									viewChangePoint.setAfterContent(publishSerializable);
								}
								
								if(summaryObj instanceof Serializable){
									Serializable summarySerializable = (Serializable)summaryObj;
									viewChangePoint.setBeforeContent(summarySerializable);
								}
							
								
								viewChangePoint.setAttrId(attr.getId());
								viewChangePoint.setDisplayName(attr.getName());
								PartMaster partMaster = partUsageLink.getUsesObject();
								viewChangePoint.setAffectedLink(partUsageLink2);

								Map<String,Object> summaryUsageLinkValues = Helper.getTypeManager().getAttrValues(partUsageLink);
								Map<String,Object> publishUsageLinkValues = Helper.getTypeManager().getAttrValues(partUsageLink2);
								Object afterValue = publishUsageLinkValues.get(attr.getId());
								Object beforeValue = summaryUsageLinkValues.get(attr.getId());
								String note = "";
								if(attr.getId().equals("QUANTITY")){
									afterValue = publishUsageLinkValues.get("QUANTITY_AMOUNT");
									Object afterUnit =	publishUsageLinkValues.get("QUANTITY_UNIT");
									beforeValue = summaryUsageLinkValues.get("QUANTITY_AMOUNT");
									Object beforeUnit = summaryUsageLinkValues.get("QUANTITY_UNIT");
									String str = beforeValue.toString()+"("+beforeUnit.toString()+")"+"→"+afterValue.toString()+"("+afterUnit.toString()+")";
									note = partMaster.getName()+"数量"+":"+str+"\n";
								}
								else{
									note = partMaster.getName()+attr.getName()+beforeValue+"→"+afterValue+"\n";
								}
								viewChangePoint.setNote(note);
								viewChangePoints.add(viewChangePoint);
							}
						}
						
						isExisted = true;
						publishPartUsageLinks.remove(partUsageLink2);
						break;
					}
				}
				if(isExisted==false){
					PartLinkChangePoint usageLinkChangePoint = new PartLinkChangePoint();
					usageLinkChangePoint.setType(PartLinkChangePoint.PART_USAGE_CHANGE);
					usageLinkChangePoint.setRemovedLink(partUsageLink);
					PartMaster partMaster = partUsageLink.getUsesObject();
					String note = "移除子部件:"+partMaster.getName();
					usageLinkChangePoint.setNote(note);
					viewChangePoints.add(usageLinkChangePoint);
				}
			}
			for(PartUsageLink partUsageLink:publishPartUsageLinks){
				PartLinkChangePoint usageLinkChangePoint = new PartLinkChangePoint();
				usageLinkChangePoint.setType(PartLinkChangePoint.PART_USAGE_CHANGE);
				usageLinkChangePoint.setCreatedLink(partUsageLink);
				PartMaster partMaster = partUsageLink.getUsesObject();
				String note = "新增子部件:"+partMaster.getName();
				usageLinkChangePoint.setNote(note);
				viewChangePoints.add(usageLinkChangePoint);
			}
			
			if(viewChangePoints!=null&&viewChangePoints.size()>0){
				PersistUtil.getService().merge(changeItem);
				for(ViewChangePoint tempViewChangePoint:viewChangePoints){
					tempViewChangePoint.setViewChangeItem(changeItem);
					PersistUtil.getService().save(tempViewChangePoint);
				}
			}
		}
		
	}
	private <T extends ATLink> Map<String,List<T>> getATLinkInBaseline(Collection<ATSnapshot> snapshot,Class<T> clazz,String baselineInnerId){
		Map<String,List<T>> returnMap = new HashMap<String,List<T>>();
		StringBuilder hql = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		hql.append(" select distinct link from "+clazz.getName()+" link,ATLinkSnapshot snapshot,BaselineMemberLink memberlink");
		hql.append(" where link.innerId = snapshot.linkRef.innerId");
		hql.append(" and snapshot.innerId = memberlink.toObjectRef.innerId");
		hql.append(" and memberlink.fromObjectRef.innerId = ?");
		paramList.add(baselineInnerId);
		
		List<Object> inObjectList = new ArrayList<Object>();
		for(ATSnapshot temp:snapshot){
			inObjectList.add(temp.getViewManageableRef().getInnerId());
		}
		hql.append(" and ").append(QueryHelper.getService().buildInCondition(inObjectList, "link.fromObjectRef.innerId", true));
		paramList.addAll(inObjectList);
		
		List<T> returnList = PersistHelper.getService().find(hql.toString(), paramList.toArray());
		//按所属父项分组
		for(T temp :returnList){
			List<T> returnMapList =  returnMap.get(temp.getFromObjectRef().getInnerId());
			if(returnMapList==null){
				returnMapList = new ArrayList<T>();
				returnMap.put(temp.getFromObjectRef().getInnerId(),returnMapList);
			}
			returnMapList.add(temp);
		}
		return returnMap;
	}

	private StringBuilder getTempTable(Collection<ATSnapshot> createdSnapshot){
		StringBuilder returnBuilder = new StringBuilder();
		boolean isFirst = true;
		for(ATSnapshot temp :createdSnapshot) {
			//如果被转换对象属于基础库中的对象,则不生成更改点
			if(!(temp.getViewManageable().getContextInfo().getContext() instanceof LibraryContext))
			{
				if(isFirst ) {
					returnBuilder.append("select '").append(UUID.randomUUID().toString()).append("' INNERID");
					returnBuilder.append(",'").append(temp.getViewManageableRef().getClassId()).append("' VIEWCLASSID");
					returnBuilder.append(",'").append(temp.getViewManageableRef().getInnerId()).append("' VIEWID").
					append(" from dual");
					
					isFirst = false;
				}else{
	
					returnBuilder.append(" union all ");
					returnBuilder.append("select '").append(UUID.randomUUID().toString()).append("'");
					returnBuilder.append(",'").append(temp.getViewManageableRef().getClassId()).append("'");
					returnBuilder.append(",'").append(temp.getViewManageableRef().getInnerId()).append("'");
					returnBuilder.append(" from dual");
				}
			}
		}
		return returnBuilder;
	}
	
	private Object getAttributeValueInclueAll(Attr attr,Persistable obj)
	{
		if(attr.getHard().equalsIgnoreCase("false"))
		{
			return ((com.bjsasc.platform.objectmodel.business.persist.ContainerIncluded)obj).getExtAttr(attr.getId());
		}
		else
		{
			return TypeHelper.getService().getAttrValueIncludeExtAttr(attr.getSources().get(0), obj);
		}
	}
	
	private boolean isEqual(Attr attr, List<Persistable> objs){
		if(attr.getSources().isEmpty()){
			return true;
		}
		
		if(attr.getId().equals("QUANTITY")){
			return isEqualQuantity(attr, objs);
		}
		
		List<String> values = new Vector<String>();
		for(Persistable obj:objs){
			values.add(getAttributeValueInclueAll(attr,(null==attr?null:attr.getSources().get(0)),obj)+"");
		}
		
		String valueCompared = null;
		for(String value:values){
			if(value != null){
				valueCompared = value;
				break;
			}
		}
		
		if(valueCompared == null){
			return true;
		}
		
		for(String value:values){
			
			valueCompared = Pattern.compile("<a[^>]*>").matcher(valueCompared).replaceAll("");
			value = Pattern.compile("<a[^>]*>").matcher(value).replaceAll("");
			
			if(!valueCompared.equals(value)){
				return false;
			}				
		}
		
		return true;
	}
	
	private boolean isEqualQuantity(Attr attr, List<Persistable> objs){
		List<Quantity> quantitys = new ArrayList<Quantity>();
		for(Persistable persistable:objs){
			PartUsageLink partUsageLink = (PartUsageLink)persistable;
			quantitys.add(partUsageLink.getQuantity());	
		}
		
		Quantity valueCompared = null;
		for(Quantity value:quantitys){
			if(value != null){
				valueCompared = value;
				break;
			}
		}
		
		if(valueCompared == null){
			return true;
		}
		
		for(Quantity value:quantitys){
			
			if(Math.abs(value.getAmount()-valueCompared.getAmount())>0.000001){
				return false;
			}
			if(!value.getQuantityUnit().equals(valueCompared.getQuantityUnit())){
				return false;
			}
		}
		
		
		return true;
	}
	
	private Object getAttributeValueInclueAll(Attr attr,String attrid,Persistable obj){
		if((null==attr)||((null!=attr)&&(attr.getHard().equalsIgnoreCase("false"))))
		{
			return ((com.bjsasc.platform.objectmodel.business.persist.ContainerIncluded)obj).getExtAttr(attrid);
		}
		else
		{
			return TypeHelper.getService().getAttrValue(attrid, obj);
		}
	}
	
	private void createViewChangePoint4PrePackageAdd(Map<ViewChangeItem,List<ViewChangePoint>> changeItemChangePointMap,ATPublishPackage publishPackage){
		List<PartAddChangeItem> changeItemList = new ArrayList<PartAddChangeItem>();
		List<PartAddChangePoint> changePointList = new ArrayList<PartAddChangePoint>();
		List<PartLinkChangePoint> linkChangePointList = new ArrayList<PartLinkChangePoint>();
		for (Entry<ViewChangeItem,List<ViewChangePoint>> tempEntry : changeItemChangePointMap.entrySet()) {
			for(ViewChangePoint changePoint:tempEntry.getValue()){
				if(ViewChangePointDealType.REMAINED.equals(changePoint.getDeal()) || ViewChangePointDealType.REFUSED.equals(changePoint.getDeal())){
					PartAddChangeItem changeItem = (PartAddChangeItem)changePoint.getViewChangeItem();
					if(!changeItemList.contains(changeItem)){
						changeItemList.add(changeItem);
					}
					if(changePoint instanceof PartAddChangePoint){
						PartAddChangePoint addChangePoint = (PartAddChangePoint)changePoint;
						changePointList.add(addChangePoint);
					}
					if(changePoint instanceof PartLinkChangePoint){
						PartLinkChangePoint linkChangePoint = (PartLinkChangePoint)changePoint;
						linkChangePointList.add(linkChangePoint);
					}
				}
			}
		}
		
		StringBuilder sql = new StringBuilder();
		StringBuilder hql = new StringBuilder();
		long updateTime = System.currentTimeMillis();
		List<Object> paramList = null;
	
		//部件新增更改项
		for(PartAddChangeItem changeItem:changeItemList){
			Part targetPart = (Part)changeItem.getAffectedViewManageable();
			String targetPartInnerId = "";
			//判断新增更改项是否为最新版小版本
			if(VersionControlHelper.getService().isLatestInBranch((Iterated)targetPart)){
				targetPartInnerId = targetPart.getInnerId();
			}else{
				Iterated latesdVersion = (Iterated)VersionControlHelper.getService().getLatestIteration((Iterated)targetPart);
				targetPartInnerId = latesdVersion.getInnerId();
			}
			paramList = new ArrayList<Object>();
			sql = new StringBuilder();
			sql.append(" insert into PLM_PART_PUB_ADDITEM");
			sql.append(" (INNERID,CLASSID,AFFECTEDVIEWMANAGEABLECLASSID,AFFECTEDVIEWMANAGEABLEID,PUBLISHPACKAGECLASSID,PUBLISHPACKAGEID,UPDATETIME,UPDATECOUNT,DEAL)");
			sql.append(" select sys_guid(),'PartAddChangeItem','Part','"+targetPartInnerId+"','"+publishPackage.getClassId()+"','"+publishPackage.getInnerId()+"',"+updateTime+",0,'"+changeItem.getDeal()+"'");
			sql.append(" from PLM_PART_PUB_ADDITEM t");
			sql.append(" where t.innerId = ?");
			paramList.add(changeItem.getInnerId());
			PersistHelper.getService().bulkUpdateBySql(sql.toString(),paramList.toArray());
		}
		
		
		//部件新增更改点
		for(PartAddChangePoint addchangePoint:changePointList){
			List<Object> hqlParamList = new ArrayList<Object>();
			hql = new StringBuilder();
			//根据最新包的innerId和受影响部件innerId查寻出最新的更改项
			String affectedViewManageableId = addchangePoint.getViewChangeItem().getAffectedViewManageable().getInnerId();
			hqlParamList.add(affectedViewManageableId);
			String publishPackageId = publishPackage.getInnerId();
			hqlParamList.add(publishPackageId);
			hql.append(" select p.innerId from PartAddChangeItem p");
			hql.append(" where p.affectedViewManageableRef.innerId = ? and p.publishPackageRef.innerId = ?");
			List<Object> changeItemInnerIdList = Helper.getPersistService().find(hql.toString(), hqlParamList.toArray());
			paramList = new ArrayList<Object>();
			sql = new StringBuilder();
			sql.append(" insert into PLM_PART_PUB_ADDPOINT");
			sql.append(" (INNERID,CLASSID,VIEWCHANGEITEMCLASSID,VIEWCHANGEITEMID,ISCONFLICT,NOTE,RESULT,CREATEDPARTCLASSID,CREATEDPARTID,UPDATETIME,UPDATECOUNT,DEAL)");
			sql.append(" select sys_guid(),'PartAddChangePoint','PartAddChangeItem','"+changeItemInnerIdList.get(0)+"',0,'新发布','','Part','"+addchangePoint.getCreatedPart().getInnerId()+"',"+updateTime+",0,'"+addchangePoint.getDeal()+"'");
			sql.append(" from PLM_PART_PUB_ADDPOINT t");
			sql.append(" where t.innerId = ?");
			paramList.add(addchangePoint.getInnerId());
			PersistHelper.getService().bulkUpdateBySql(sql.toString(),paramList.toArray());
		}
		
		for(PartLinkChangePoint linkChangePoint:linkChangePointList){
			/*//参考关系更改点
			sql = new StringBuilder();
			paramList = new ArrayList<Object>();
			sql.append(" insert into PLM_PART_PUB_LINKPOINT");
			sql.append(" (INNERID,CLASSID,VIEWCHANGEITEMCLASSID,VIEWCHANGEITEMID,ISCONFLICT,NOTE,RESULT,CREATEDLINKCLASSID,CREATEDLINKID,LINKCHANGETYPE,UPDATETIME,UPDATECOUNT)");
			sql.append(" select sys_guid(),'PartLinkChangePoint','PartAddChangeItem',t.INNERID,0,'新增参考文档'||master.name,'',link.classid,link.innerid,'"+PartLinkChangePoint.PART_REFERENCE_CHANGE+"',"+updateTime+",0");
			sql.append(" from plm_doc_DOCUMENTMASTER master,PLM_PART_REFERENCELINK link,PLM_VIEW_PUB_LINKSNAPSHOT snapshot,PLM_BASELINE_MEMBERLINK memberlink,(");
			//sql.append(tempTable);
			sql.append(" ) t");
			sql.append(" where t.viewid = link.fromobjectid ");
			sql.append(" and link.innerid = snapshot.LINKID");
			sql.append(" and snapshot.innerid = memberlink.toobjectid");
			sql.append(" and memberlink.fromobjectid = ?");
			sql.append(" and master.innerid = link.toobjectid");
			paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
			PersistHelper.getService().bulkUpdateBySql(sql.toString(),paramList.toArray());
			
			//描述关系更改点
			sql = new StringBuilder();
			paramList = new ArrayList<Object>();
			sql.append(" insert into PLM_PART_PUB_LINKPOINT");
			sql.append(" (INNERID,CLASSID,VIEWCHANGEITEMCLASSID,VIEWCHANGEITEMID,ISCONFLICT,NOTE,RESULT,CREATEDLINKCLASSID,CREATEDLINKID,LINKCHANGETYPE,UPDATETIME,UPDATECOUNT)");
			sql.append(" select sys_guid(),'PartLinkChangePoint','PartAddChangeItem',t.INNERID,0,'新增描述文档'||master.name,'',link.classid,link.innerid,'"+PartLinkChangePoint.PART_DESCRIBE_CHANGE+"',"+updateTime+",0");
			sql.append(" from plm_doc_DOCUMENTMASTER master,plm_doc_DOCUMENT doc,PLM_PART_DECRIBELINK link,PLM_VIEW_PUB_LINKSNAPSHOT snapshot,PLM_BASELINE_MEMBERLINK memberlink,(");
			//sql.append(tempTable);
			sql.append(" ) t");
			paramList.add(publishPackage.getPublishBaselineRef().getInnerId());
			PersistHelper.getService().bulkUpdateBySql(sql.toString(),paramList.toArray());*/
			
			//使用关系更改点
			List<Object> hqlParamList = new ArrayList<Object>();
			hql = new StringBuilder();
			//根据最新包的innerId和最新下版本部件innerId查寻出最新的更改项
			Part targetPart = (Part)linkChangePoint.getViewChangeItem().getAffectedViewManageable();
			String affectedViewManageableId = "";
			//判断新增更改项是否为最新版小版本
			if(VersionControlHelper.getService().isLatestInBranch((Iterated)targetPart)){
				affectedViewManageableId = targetPart.getInnerId();
				hqlParamList.add(affectedViewManageableId);
			}else{
				Iterated latesdVersion = (Iterated)VersionControlHelper.getService().getLatestIteration((Iterated)targetPart);
				affectedViewManageableId = latesdVersion.getInnerId();
				hqlParamList.add(affectedViewManageableId);
			}
			String publishPackageId = publishPackage.getInnerId();
			hqlParamList.add(publishPackageId);
			hql.append(" select p.innerId from PartAddChangeItem p");
			hql.append(" where p.affectedViewManageableRef.innerId = ? and p.publishPackageRef.innerId = ?");
			List<Object> changeItemInnerIdList = Helper.getPersistService().find(hql.toString(), hqlParamList.toArray());
			sql = new StringBuilder();
			paramList = new ArrayList<Object>();
			sql.append(" insert into PLM_PART_PUB_LINKPOINT");
			sql.append(" (INNERID,CLASSID,VIEWCHANGEITEMCLASSID,VIEWCHANGEITEMID,ISCONFLICT,NOTE,RESULT,CREATEDLINKCLASSID,CREATEDLINKID,LINKCHANGETYPE,UPDATETIME,UPDATECOUNT,DEAL)");
			sql.append(" select sys_guid(),'PartLinkChangePoint','PartAddChangeItem','"+changeItemInnerIdList.get(0)+"',0,'"+linkChangePoint.getNote()+"','','PartUsageLink','"+linkChangePoint.getCreatedLink().getInnerId()+"','"+PartLinkChangePoint.PART_USAGE_CHANGE+"',"+updateTime+",0,'"+linkChangePoint.getDeal()+"'");
			sql.append(" from PLM_PART_PUB_LINKPOINT t");
			sql.append(" where t.innerId = ?");
			paramList.add(linkChangePoint.getInnerId());
			PersistHelper.getService().bulkUpdateBySql(sql.toString(),paramList.toArray());
		}
		
	}
	
}
