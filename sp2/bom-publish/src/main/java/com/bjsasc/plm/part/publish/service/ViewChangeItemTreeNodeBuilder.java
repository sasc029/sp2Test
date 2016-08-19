package com.bjsasc.plm.part.publish.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bjsasc.plm.Helper;
import com.bjsasc.plm.core.identifier.Named;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.publish.model.PartAddChangeItem;
import com.bjsasc.plm.core.part.publish.model.PartAddChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartModifyChangeItem;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.vc.model.Iterated;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ViewChangePointDealType;
import com.bjsasc.plm.type.TypeManager;
import com.bjsasc.plm.url.ContextPath;
import com.bjsasc.plm.util.SortUtil;

public class ViewChangeItemTreeNodeBuilder {
	public List<Map<String,Object>> build(ATPublishPackage publishPackage){

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		// 处理新增更改项
		
		Map<ViewChangeItem,List<ViewChangePoint>> changeItemChangePointMap = getViewChangeItem(publishPackage);
		
		for (Entry<ViewChangeItem,List<ViewChangePoint>> tempEntry : changeItemChangePointMap.entrySet()) {
			Map<String, Object> map = new HashMap<String, Object>();
			Part affectedPart = (Part) tempEntry.getKey().getAffectedViewManageable();
			//获取子部件主对象
			PartMaster partMaster = affectedPart.getPartMaster();
			map.put("OID", Helper.getOid(tempEntry.getKey()));
			String tip = partMaster.getName();
			map.put("AFFECTEDNODE", "<div title =" + tip+ " style='color:green'>" + partMaster.getNumber()+ "</div>");
			map.put("AFFECTEDNODE_VERSION", affectedPart.getIterationInfo().getVersionNo()+"."+affectedPart.getIterationInfo().getIterationNo()+"("+affectedPart.getView().getName()+")");
			if(tempEntry.getKey() instanceof PartAddChangeItem){
				PartAddChangeItem changeItem = (PartAddChangeItem)tempEntry.getKey();
				if(ViewChangePointDealType.REFUSED.equals(changeItem.getDeal())){
					map.put("deleterow", true);
				}
				map.put("CHANGETYPE", "新增");
			}else{
				map.put("CHANGETYPE", "修改");
			}
			map.put("innerId", partMaster.getInnerId());
			map.put("__viewicon", "true");
			map.put("iconsrc", ContextPath.CONTEXT_PATH	+ TypeManager.getManager().getType(affectedPart.getClassId()).getIcon());
			map.put("expanded", false);
			map.put("enableSelect", false);//行不可选
			//构建更改点
			List<Map<String,Object>> children = new ArrayList<Map<String,Object>>();
			for(ViewChangePoint changePoint:tempEntry.getValue()){
				children.add(buildViewChangePointNode(changePoint));
			}
			SortUtil.sort(children, "AFFECTEDNODE", false);
			map.put("children", children);
			map.put("expanded",true);
			list.add(map);
		}
		SortUtil.sort(list, "AFFECTEDNODE", false);
		return list;
	
	}
	public Map<ViewChangeItem,List<ViewChangePoint>> getViewChangeItem(ATPublishPackage publishPackage){
		StringBuilder hql = null;
		List<Object> paramList = null;
		List result = null;
		Map<ViewChangeItem,List<ViewChangePoint>> returnMap = new HashMap<ViewChangeItem,List<ViewChangePoint>>();
		Map<String,List<ViewChangePoint>> viewChangeItemStringMap = new HashMap<String, List<ViewChangePoint>>();
		
		//部件新增更改点
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" select partmaster,part,changeitem,changepoint from PartMaster partmaster,Part part, PartAddChangeItem changeitem,PartAddChangePoint changepoint");
		hql.append(" where partmaster.innerId = part.masterRef.innerId");
		hql.append(" and part.innerId = changeitem.affectedViewManageableRef.innerId");
		hql.append(" and changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getInnerId());
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			PartMaster partmaster = (PartMaster)tempArray[0];
			Part part = (Part)tempArray[1];
			PartAddChangeItem changeitem = (PartAddChangeItem)tempArray[2];
			PartAddChangePoint changepoint = (PartAddChangePoint)tempArray[3];
			part.setMaster(partmaster);
			changeitem.setAffectedViewManageable(part);
			changepoint.setViewChangeItem(changeitem);
			changepoint.setCreatedPart(part);
			
			List<ViewChangePoint> viewChangePointList = viewChangeItemStringMap.get(changeitem.getInnerId());
			
			if(viewChangePointList==null){
				viewChangePointList = new ArrayList<ViewChangePoint>();
				viewChangeItemStringMap.put(changeitem.getInnerId(), viewChangePointList);
				returnMap.put(changeitem,viewChangePointList);
			}
			viewChangePointList.add(changepoint);
			
		}
		//部件关系更改点
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" select partmaster, part,changeitem,changepoint from PartMaster partmaster,Part part, PartAddChangeItem changeitem,PartLinkChangePoint changepoint");
		hql.append(" where partmaster.innerId = part.masterRef.innerId");
		hql.append(" and part.innerId = changeitem.affectedViewManageableRef.innerId");
		hql.append(" and changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getInnerId());
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			PartMaster partmaster = (PartMaster)tempArray[0];
			Part part = (Part)tempArray[1];
			PartAddChangeItem changeitem = (PartAddChangeItem)tempArray[2];
			PartLinkChangePoint changepoint = (PartLinkChangePoint)tempArray[3];
			part.setMaster(partmaster);
			changeitem.setAffectedViewManageable(part);
			changepoint.setViewChangeItem(changeitem);
			
			List<ViewChangePoint> viewChangePointList = viewChangeItemStringMap.get(changeitem.getInnerId());
			
			if(viewChangePointList==null){
				viewChangePointList = new ArrayList<ViewChangePoint>();
				viewChangeItemStringMap.put(changeitem.getInnerId(), viewChangePointList);
				returnMap.put(changeitem,viewChangePointList);
			}
			viewChangePointList.add(changepoint);
		}
		
		//部件关系更改点-修改
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" select partmaster, part,changeitem,changepoint from PartMaster partmaster,Part part, PartModifyChangeItem changeitem,PartLinkChangePoint changepoint");
		hql.append(" where partmaster.innerId = part.masterRef.innerId");
		hql.append(" and part.innerId = changeitem.affectedViewManageableRef.innerId");
		hql.append(" and changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getInnerId());
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
		
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			PartMaster partmaster = (PartMaster)tempArray[0];
			Part part = (Part)tempArray[1];
			PartModifyChangeItem changeitem = (PartModifyChangeItem)tempArray[2];
			PartLinkChangePoint changepoint = (PartLinkChangePoint)tempArray[3];
			part.setMaster(partmaster);
			changeitem.setAffectedViewManageable(part);
			changepoint.setViewChangeItem(changeitem);
			List<ViewChangePoint> viewChangePointList = viewChangeItemStringMap.get(changeitem.getInnerId());				
			if(viewChangePointList==null){
				viewChangePointList = new ArrayList<ViewChangePoint>();
				viewChangeItemStringMap.put(changeitem.getInnerId(), viewChangePointList);
				returnMap.put(changeitem,viewChangePointList);
			}
			viewChangePointList.add(changepoint);
		}
		
		//部件属性更改点-修改
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		hql.append(" select partmaster, part,changeitem,changepoint from PartMaster partmaster,Part part, PartModifyChangeItem changeitem,PartAttrChangePoint changepoint");
		hql.append(" where partmaster.innerId = part.masterRef.innerId");
		hql.append(" and part.innerId = changeitem.affectedViewManageableRef.innerId");
		hql.append(" and changeitem.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getInnerId());
		hql.append(" and changeitem.innerId = changepoint.viewChangeItemRef.innerId");
		result = Helper.getPersistService().find(hql.toString(), paramList.toArray());
				
		for(Object temp:result){
			Object[] tempArray = (Object[])temp;
			PartMaster partmaster = (PartMaster)tempArray[0];
			Part part = (Part)tempArray[1];
			PartModifyChangeItem changeitem = (PartModifyChangeItem)tempArray[2];
			PartAttrChangePoint changepoint = (PartAttrChangePoint)tempArray[3];
			part.setMaster(partmaster);
			changeitem.setAffectedViewManageable(part);
			changepoint.setViewChangeItem(changeitem);
			List<ViewChangePoint> viewChangePointList = viewChangeItemStringMap.get(changeitem.getInnerId());				
			if(viewChangePointList==null){
				viewChangePointList = new ArrayList<ViewChangePoint>();
				viewChangeItemStringMap.put(changeitem.getInnerId(), viewChangePointList);
				returnMap.put(changeitem,viewChangePointList);
			}
			viewChangePointList.add(changepoint);
		}
		
		return returnMap;
	}
	private Map<String,Object> buildViewChangePointNode(ViewChangePoint changePoint){
		Map<String,Object> map = new HashMap<String,Object>();
		
		String contextPath = ContextPath.CONTEXT_PATH;
		String nomalImageUrl = contextPath +"/plm/images/common/ok.png";
		if(ViewChangePointDealType.REFUSED.equals(changePoint.getDeal())){
			map.put("deleterow", true);
		}
		//map树的相关属性
		map.put("OID", Helper.getOid(changePoint));
		Part affectedPart = (Part) changePoint.getViewChangeItem().getAffectedViewManageable();
		map.put("AFFECTEDNODE", affectedPart.getPartMaster().getNumber());
		map.put("CHANGETYPE", changePoint.getChangeTip());
		//TODO 目标节点
		map.put("TARGETNODE",  buildTargetNode(changePoint));
		
		map.put("NOTE", changePoint.getNote());
		map.put("__viewicon", "false");
		map.put("iconsrc", ContextPath.CONTEXT_PATH	+ TypeManager.getManager().getType(changePoint.getTarget().getClassId()).getIcon());
		map.put("RESULT", changePoint.getResult());
		String conflictUrl = "";
		if(changePoint.getIsConflict()) {
			conflictUrl = contextPath + "/plm/view/viewreceive/conflictTabs.jsp?OID="+Helper.getOid(changePoint);
			map.put("enableSelect", true);
		    map.put("CONFLICT", "<a href='#' onclick='plm.showUrl(\""+conflictUrl+"\")' style='color:red'>冲突</a>");
		} else {
			map.put("enableSelect", true);
			map.put("CONFLICT", "<img title = '正常' src ='" + nomalImageUrl + "'/>");
		}
		return map;
	}
	private String buildTargetNode(ViewChangePoint changePoint){
		Persistable target = changePoint.getTarget();
		String targetNode = null;
		
		if(target == null){
			return "";
		}else if(target instanceof Named){
			Named named = (Named)target;
			targetNode = named.getName();
		}else if(target instanceof Iterated){
			Iterated iterated = (Iterated)target;
			
			String version = iterated.getIterationInfo().getFullVersionNo();
			
			if(iterated instanceof ViewManageable){
				ViewManageable viewManageable = (ViewManageable)iterated;
				version = version +"("+viewManageable.getView().getName()+")";
			}
			
			if(iterated.getMaster() instanceof Named){
				Named named = (Named)iterated.getMaster();
				String display = named.getName()+" "+version;
				targetNode = "<a href='#' onclick='openTarget(\""+Helper.getOid(target)+"\")' >"+display+"</a>";
			}
		}
		return targetNode;
	}
}
