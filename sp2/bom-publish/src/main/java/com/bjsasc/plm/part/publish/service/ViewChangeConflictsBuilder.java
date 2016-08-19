package com.bjsasc.plm.part.publish.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bjsasc.plm.Helper;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartHelper;
import com.bjsasc.plm.core.part.Quantity;
import com.bjsasc.plm.core.part.link.PartDecribeLink;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartModifyChangeItem;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.PersistUtil;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.type.ATObject;
import com.bjsasc.plm.core.type.TypeHelper;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.type.attr.Attr;
import com.bjsasc.plm.type.attr.AttrUtil;

public class ViewChangeConflictsBuilder {

	public void build(ATPublishPackage publishPackage){
		//删除之前的冲突
		removeViewChangeConflict(publishPackage);
		//重新生成冲突
		createViewChangeConflict(publishPackage);
	}
	private void removeViewChangeConflict(ATPublishPackage publishPackage){
		//移除以前的冲突
		StringBuilder hql = null;
		List<Object> paramList = null;
		
		//移除属性冲突
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" delete from PartAttrChangeConflict conflict");
		hql.append(" where conflict.pointRef.innerId in (");
		hql.append("	select changepoint.innerId from PartAttrChangePoint changepoint,PartModifyChangeItem changeitem");
		hql.append("	where changepoint.viewChangeItemRef.innerId = changeitem.innerId");
		hql.append("	and changeitem.publishPackageRef.innerId = ? ");
		paramList.add(publishPackage.getInnerId());
		hql.append(" )");
		Helper.getPersistService().bulkUpdate(hql.toString(), paramList.toArray());
		
		//移除关系冲突
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" delete from PartLinkChangeConflict conflict");
		hql.append(" where conflict.pointRef.innerId in (");
		hql.append("	select changepoint.innerId from PartLinkChangePoint changepoint,PartModifyChangeItem changeitem");
		hql.append("	where changepoint.viewChangeItemRef.innerId = changeitem.innerId");
		hql.append("	and changeitem.publishPackageRef.innerId = ? ");
		paramList.add(publishPackage.getInnerId());
		hql.append(" )");
		Helper.getPersistService().bulkUpdate(hql.toString(), paramList.toArray());
	}
	private void createViewChangeConflict(ATPublishPackage publishPackage){
		//Map<属性更改点,目标部件>
		Map<PartAttrChangePoint,Part> viewChangePointTargetViewMap_attr = getPartAttrChangePoint4Target(publishPackage);
		//Map<描述关系更改点,目标部件>
		Map<PartLinkChangePoint,Part> viewChangePointTargetViewMap_link = getPartLinkChangePoint4Target(publishPackage);
		
		for(Entry<PartAttrChangePoint,Part> tempEntry:viewChangePointTargetViewMap_attr.entrySet()){
			createViewChangeConflict4Attr(tempEntry.getKey(),tempEntry.getValue());
		}
		for(Entry<PartLinkChangePoint,Part> tempEntry:viewChangePointTargetViewMap_link.entrySet()){
			createViewChangeConflict4Link(tempEntry.getKey(),tempEntry.getValue());
		}
	}
	private void createViewChangeConflict4Attr(PartAttrChangePoint partAttrChangePoint,Part targetPart){
		//属性修改冲突
		boolean isToConflicts = false;
		String attrId = partAttrChangePoint.getAttrId();
		Attr attr = null;
		if(attrId.equals("QUANTITY")){
			List<Attr> attrs1 = Helper.getTypeManager().getTypeAttrs("PartUsageLink", "view_publish");
			for(Attr attr2:attrs1){
				if(attr2.getId().equals("QUANTITY")){
					attr=attr2;
					break;
				}
			}
		}
		else{
			attr = AttrUtil.getAttrsMap().get(attrId);
		}
			
		if(partAttrChangePoint.getType().equals(PartAttrChangePoint.MASTER_ATTR_CHANGE)||partAttrChangePoint.getType().equals(PartAttrChangePoint.PART_ATTR_CHANGE)){
			
			String currentContent = getAttributeValueInclueAll(attr,attrId, targetPart)+"";
			
			String s = "";
			if(partAttrChangePoint.getBeforeContent() != null){
				s = partAttrChangePoint.getBeforeContent().toString();
			}
			if(!currentContent.equals(s.toString())){
				PartAttrChangeConflict partAttrChangeConflict = new PartAttrChangeConflict();
				partAttrChangeConflict.setViewChangePoint(partAttrChangePoint);
				partAttrChangeConflict.setDownStream(targetPart);							
				partAttrChangeConflict.setCurrentContent(currentContent);
				PersistHelper.getService().save(partAttrChangeConflict);
				isToConflicts = true;
			}
		}
		if(partAttrChangePoint.getType().equals(PartAttrChangePoint.USAGELINK_ATTR_CHANGE)){
			Part part = (Part)targetPart;
			List<PartUsageLink> partUsageLinks = PartHelper.getService().getPartUsageLinkByFrom(part);
			for(PartUsageLink partUsageLink:partUsageLinks){
				if(partAttrChangePoint.getAffectedLink().getToObjectRef().getInnerId().equals(partUsageLink.getToObjectRef().getInnerId())){
					Serializable currentContent;
					Object value=getAttributeValueInclueAll(attr,(null==attr?null:attr.getSources().get(0)),partUsageLink);
					if(null!=value)
					{
						currentContent=(Serializable)value;
						Serializable beforeContent = partAttrChangePoint.getBeforeContent();
						if(currentContent instanceof Quantity){
							Quantity currentQuantity  = (Quantity)currentContent;
							Quantity beforeQuantity = (Quantity)beforeContent;
							double currentAmount = currentQuantity.getAmount();
							double beforeAmount = beforeQuantity.getAmount();
							if(currentQuantity.getQuantityUnit().equals(beforeQuantity.getQuantityUnit())&&currentAmount==beforeAmount){
									//
							}
							else{
								PartAttrChangeConflict partAttrChangeConflict = new PartAttrChangeConflict();
								partAttrChangeConflict.setViewChangePoint(partAttrChangePoint);
								partAttrChangeConflict.setDownStream(targetPart);							
								partAttrChangeConflict.setCurrentContent(currentContent);
								PersistHelper.getService().save(partAttrChangeConflict);
								isToConflicts = true;
							}
						}
					}
				}
			}
		}
		if(isToConflicts==true){
			partAttrChangePoint.setIsConflict(true);
			PersistUtil.getService().update(partAttrChangePoint);
		}
	}
	private void createViewChangeConflict4Link(PartLinkChangePoint changePoint,Part targetPart){
		//创建关系冲突
		boolean isToConflicts = false;
		if(changePoint.getType().equals(PartLinkChangePoint.PART_DESCRIBE_CHANGE)){
			PartDecribeLink removedLink = (PartDecribeLink) changePoint.getRemovedLink();
			if(removedLink!=null){
				List<PartDecribeLink> partDecribeLinks = PartHelper.getService().getPartDecribeLinkByFrom(targetPart);
				for(PartDecribeLink partDecribeLink:partDecribeLinks){
					if(partDecribeLink.getDescribesObject().getMasterRef().getInnerId().equals(removedLink.getDescribesObject().getMasterRef().getInnerId())){
						if(partDecribeLink.getDescribesObject().getInnerId().equals(removedLink.getDescribesObject().getInnerId())){
							
						}
						else{
							PartLinkChangeConflict partLinkChangeConflict = new PartLinkChangeConflict();
							partLinkChangeConflict.setCurrentLink(partDecribeLink);
							partLinkChangeConflict.setDownStream(targetPart);
							partLinkChangeConflict.setViewChangePoint(changePoint);
							PersistHelper.getService().save(partLinkChangeConflict);
							isToConflicts = true;
						}
					}
				}
			}
			
		}
		if(isToConflicts==true){
			changePoint.setIsConflict(true);
			PersistUtil.getService().update(changePoint);
		}
	}
	private Map<PartAttrChangePoint,Part> getPartAttrChangePoint4Target(ATPublishPackage publishPackage){
		
		Map<PartAttrChangePoint,Part> viewChangePointTargetViewMap = new HashMap<PartAttrChangePoint,Part>();
		//查找属性目标视图部件存在的属性更改点，
		StringBuilder hql = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		hql.append(" select changeitem,changepoint,part,targetpart from PartModifyChangeItem changeitem,PartAttrChangePoint changepoint,Part part,Part targetpart,com.bjsasc.platform.objectmodel.business.version.ControlBranch controlbranch");
		hql.append(" where changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getInnerId());
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		hql.append(" and changeitem.affectedViewManageableRef.innerId = part.innerId");
		hql.append(" and part.masterRef.innerId = targetpart.masterRef.innerId");
		hql.append(" and targetpart.viewRef.innerId = ?");
		paramList.add(publishPackage.getTargetViewRef().getInnerId());
		hql.append(" and targetpart.iterationInfo.latestInBranch = '1'");
		hql.append(" and targetpart.iterationInfo.controlBranchRef.innerId = controlbranch.innerId");
		hql.append(" and controlbranch.latestBranch = '1'");
		
		List result = PersistHelper.getService().find(hql.toString(), paramList.toArray());
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			PartModifyChangeItem changeItem = (PartModifyChangeItem)tempArray[0];
			PartAttrChangePoint changepoint = (PartAttrChangePoint)tempArray[1];
			changepoint.setViewChangeItem(changeItem);
			Part part = (Part)tempArray[2];
			changeItem.setAffectedViewManageable(part);
			Part targetPart = (Part)tempArray[3];
			viewChangePointTargetViewMap.put(changepoint,targetPart);
		}
		return viewChangePointTargetViewMap;
	}
	private Map<PartLinkChangePoint,Part> getPartLinkChangePoint4Target(ATPublishPackage publishPackage){
		
		Map<PartLinkChangePoint,Part> viewChangePointTargetViewMap = new HashMap<PartLinkChangePoint,Part>();
		//查找属性目标视图部件存在的属性更改点，
		StringBuilder hql = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		hql.append(" select changeitem,changepoint,part,targetpart from PartModifyChangeItem changeitem,PartLinkChangePoint changepoint,Part part,Part targetpart,com.bjsasc.platform.objectmodel.business.version.ControlBranch controlbranch");
		hql.append(" where changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getInnerId());
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		hql.append(" and changeitem.affectedViewManageableRef.innerId = part.innerId");
		hql.append(" and part.masterRef.innerId = targetpart.masterRef.innerId");
		hql.append(" and targetpart.viewRef.innerId = ?");
		paramList.add(publishPackage.getTargetViewRef().getInnerId());
		hql.append(" and targetpart.iterationInfo.latestInBranch = '1'");
		hql.append(" and targetpart.iterationInfo.controlBranchRef.innerId = controlbranch.innerId");
		hql.append(" and controlbranch.latestBranch = '1'");
		hql.append(" and changepoint.type = ?");
		paramList.add(PartLinkChangePoint.PART_DESCRIBE_CHANGE);
		
		List result = PersistHelper.getService().find(hql.toString(), paramList.toArray());
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			PartModifyChangeItem changeItem = (PartModifyChangeItem)tempArray[0];
			PartLinkChangePoint changepoint = (PartLinkChangePoint)tempArray[1];
			changepoint.setViewChangeItem(changeItem);
			Part part = (Part)tempArray[2];
			changeItem.setAffectedViewManageable(part);
			Part targetPart = (Part)tempArray[3];
			viewChangePointTargetViewMap.put(changepoint,targetPart);
		}
		return viewChangePointTargetViewMap;
	}
	
	private Object getAttributeValueInclueAll(Attr attr,String attrid,Persistable obj)
	{
		if((null==attr)||((null!=attr)&&(attr.getHard().equalsIgnoreCase("false"))))
		{
			return ((com.bjsasc.platform.objectmodel.business.persist.ContainerIncluded)obj).getExtAttr(attrid);
		}
		else
		{
			return TypeHelper.getService().getAttrValue(attrid, obj);
		}
	}

}
