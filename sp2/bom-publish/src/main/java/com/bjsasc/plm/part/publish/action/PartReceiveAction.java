package com.bjsasc.plm.part.publish.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.bjsasc.plm.KeyS;
import com.bjsasc.plm.core.Helper;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.system.task.TaskHelper;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATPublishTask;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangeItem;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangePoint;
import com.bjsasc.plm.core.view.publish.model.ViewChangePointDealType;
import com.bjsasc.plm.part.publish.PartPublishManager;
import com.bjsasc.plm.part.publish.service.ViewChangeItemTreeNodeBuilder;
import com.bjsasc.plm.util.JsonUtil;
import com.bjsasc.ui.json.DataUtil;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class PartReceiveAction extends ActionSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5718926512191844521L;
	
	/**
	 * 应用更改
	 * 
	 * @return
	 */
	public String applyChange() {
		HttpServletRequest request = ServletActionContext.getRequest();
		String applyData = request.getParameter("applyRecords");
		String remainData = request.getParameter("remainRecords");
		List<String> applyOids = new ArrayList<String>();
		List<String> remainOids = new ArrayList<String>();
		if (JsonUtil.isList(applyData)) {
			List<Map<String, Object>> mapList = JsonUtil.toList(applyData);
			for (Map<String, Object> temp : mapList) {
				applyOids.add(temp.get(KeyS.OID).toString());
			}
		} else {
			Map<String, Object> temp = JsonUtil.toMap(applyData);
			applyOids.add(temp.get(KeyS.OID).toString());
		}
		
		if(remainData!=null && !"".equals(remainData)){
			if (JsonUtil.isList(remainData)) {
				List<Map<String, Object>> mapList = JsonUtil.toList(remainData);
				for (Map<String, Object> temp : mapList) {
					remainOids.add(temp.get(KeyS.OID).toString());
				}
			} else {
				Map<String, Object> temp = JsonUtil.toMap(remainData);
				remainOids.add(temp.get(KeyS.OID).toString());
			}
		}
		
		//应用更改点
		for(String applyOid:applyOids){
			Persistable changeObj = Helper.getPersistService().getObject(applyOid);
			if(changeObj instanceof AbstractViewChangeItem){
				AbstractViewChangeItem changeItem = (AbstractViewChangeItem)changeObj;
				changeItem.setDeal(ViewChangePointDealType.RESOLVED);
				PersistHelper.getService().update(changeItem);
			}else if(changeObj instanceof AbstractViewChangePoint){
				AbstractViewChangePoint changePoint = (AbstractViewChangePoint)changeObj;
				changePoint.setDeal(ViewChangePointDealType.RESOLVED);
				PersistHelper.getService().update(changePoint);
			}
		}
		
		//保留更改点(未处理)
		if(remainOids!=null){
			for(String remainOid:remainOids){
				Persistable changeObj = Helper.getPersistService().getObject(remainOid);
				if(changeObj instanceof AbstractViewChangeItem){
					AbstractViewChangeItem changeItem = (AbstractViewChangeItem)changeObj;
					changeItem.setDeal(ViewChangePointDealType.REMAINED);
					PersistHelper.getService().update(changeItem);
				}else if(changeObj instanceof AbstractViewChangePoint){
					AbstractViewChangePoint changePoint = (AbstractViewChangePoint)changeObj;
					changePoint.setDeal(ViewChangePointDealType.REMAINED);
					PersistHelper.getService().update(changePoint);
				}
			}
		}
		
		String packageOid = request.getParameter(KeyS.OID);
		new PartReceiveExecutor(packageOid, applyOids).execute();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("SUCCESS", "true");
		String temp = DataUtil.mapToSimpleJson(result);
		ActionContext.getContext().put("data", temp);
		return "outputData";
	}
	
	/**
	 * 设置为拒绝
	 * 
	 * @return
	 */
	public String setRefused() {
		HttpServletRequest request = ServletActionContext.getRequest();
		String refuseData = request.getParameter("refuseRecords");
		List<String> refuseOids = new ArrayList<String>();
		if (JsonUtil.isList(refuseData)) {
			List<Map<String, Object>> mapList = JsonUtil.toList(refuseData);
			for (Map<String, Object> temp : mapList) {
				refuseOids.add(temp.get(KeyS.OID).toString());
			}
		} else {
			Map<String, Object> temp = JsonUtil.toMap(refuseData);
			refuseOids.add(temp.get(KeyS.OID).toString());
		}
		
		for(String refuseOid:refuseOids){
			Persistable changeObj = Helper.getPersistService().getObject(refuseOid);
			if(changeObj instanceof AbstractViewChangeItem){
				AbstractViewChangeItem changeItem = (AbstractViewChangeItem)changeObj;
				changeItem.setDeal(ViewChangePointDealType.REFUSED);
				PersistHelper.getService().update(changeItem);
			}else if(changeObj instanceof AbstractViewChangePoint){
				AbstractViewChangePoint changePoint = (AbstractViewChangePoint)changeObj;
				changePoint.setDeal(ViewChangePointDealType.REFUSED);
				PersistHelper.getService().update(changePoint);
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("SUCCESS", "true");
		String temp = DataUtil.mapToSimpleJson(result);
		ActionContext.getContext().put("data", temp);
		return "outputData";
	}
	
	
	/**
	 * 取消设置为拒绝
	 * 
	 * @return
	 */
	public String cancelSetRefused() {
		HttpServletRequest request = ServletActionContext.getRequest();
		String notRefuseData = request.getParameter("notRefuseRecords");
		List<String> notrefuseOids = new ArrayList<String>();
		if (JsonUtil.isList(notRefuseData)) {
			List<Map<String, Object>> mapList = JsonUtil.toList(notRefuseData);
			for (Map<String, Object> temp : mapList) {
				notrefuseOids.add(temp.get(KeyS.OID).toString());
			}
		} else {
			Map<String, Object> temp = JsonUtil.toMap(notRefuseData);
			notrefuseOids.add(temp.get(KeyS.OID).toString());
		}
		
		for(String notrefuseOid:notrefuseOids){
			Persistable changeObj = Helper.getPersistService().getObject(notrefuseOid);
			if(changeObj instanceof AbstractViewChangeItem){
				AbstractViewChangeItem changeItem = (AbstractViewChangeItem)changeObj;
				changeItem.setDeal(ViewChangePointDealType.REMAINED);
				PersistHelper.getService().update(changeItem);
			}else if(changeObj instanceof AbstractViewChangePoint){
				AbstractViewChangePoint changePoint = (AbstractViewChangePoint)changeObj;
				changePoint.setDeal(ViewChangePointDealType.REMAINED);
				PersistHelper.getService().update(changePoint);
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("SUCCESS", "true");
		String temp = DataUtil.mapToSimpleJson(result);
		ActionContext.getContext().put("data", temp);
		return "outputData";
	}
	
	/**
	 * 回滚
	 * 
	 * @return
	 */
	public String rollbackChange() {
		HttpServletRequest request = ServletActionContext.getRequest();
		String packageOid = request.getParameter(KeyS.OID);
		ATPublishPackage publishPackage = (ATPublishPackage) PersistHelper.getService().getObject(packageOid);
		PartPublishManager.getPartReceiveManager().rollbackChange(publishPackage);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("SUCCESS", "true");
		String temp = DataUtil.mapToSimpleJson(result);
		ActionContext.getContext().put("data", temp);
		return "outputData";
	}
	
	/**
	 * 提交
	 * 
	 * @return
	 */
	public String commitChange() {
		HttpServletRequest request = ServletActionContext.getRequest();
		String taskOid=request.getParameter("taskOid");
		String packageOid = request.getParameter(KeyS.OID);
		ATPublishPackage publishPackage = (ATPublishPackage) PersistHelper.getService().getObject(packageOid);
		ATPublishTask task=(ATPublishTask) PersistHelper.getService().getObject(taskOid);
		PartPublishManager.getPartReceiveManager().commitChange(publishPackage);
		TaskHelper.getService().submitTask(task);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("SUCCESS", "true");
		String temp = DataUtil.mapToSimpleJson(result);
		ActionContext.getContext().put("data", temp);
		return "outputData";
	}
	
	
	/**
	 * @return
	 */
	public String buildChangeTreeNode(){
		HttpServletRequest request = ServletActionContext.getRequest();
		String oid = request.getParameter(KeyS.OID);
		ATPublishPackage publishPackage = (ATPublishPackage) PersistHelper.getService().getObject(oid);
		String result = DataUtil.encode(new ViewChangeItemTreeNodeBuilder().build(publishPackage));
		ActionContext.getContext().put("data", result);
		return "outputData";
	}
}
