package com.bjsasc.plm.core.view.publish.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.bjsasc.plm.core.Helper;
import com.bjsasc.plm.core.identifier.Identified;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.PersistService;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.vc.model.Mastered;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewChangeConflict;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.ViewPublishService;
import com.bjsasc.plm.core.view.publish.model.ATLinkSnapshot;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATPublishTask;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;
import com.bjsasc.plm.core.view.publish.model.MasterInfo;

public class ViewPublishServiceImpl<K> implements ViewPublishService {
	
	private PersistService persistService = PersistHelper.getService();
	
	@Override
	public ATSnapshot newATSnapshot(ViewManageable viewManageable) {
		ATSnapshot snapshot = new ATSnapshot();
		// 设置引用
		snapshot.setViewManageable(viewManageable);
		// 设置主对象信息
		MasterInfo masterInfo = buildMasterInfo(viewManageable);
		snapshot.setMasterInfo(masterInfo);
		return snapshot;
	}

	@Override
	public ATLinkSnapshot newATLinkSnapshot(ATLink link) {
		ATLinkSnapshot linkSnapshot = new ATLinkSnapshot();
		linkSnapshot.setLink(link);
		return linkSnapshot;
	}

	/**
	 * 构造MasterInfo
	 * @param viewManageable
	 * @return MasterInfo
	 */
	protected MasterInfo buildMasterInfo(ViewManageable viewManageable) {
		MasterInfo masterInfo = new MasterInfo();
		Mastered mastered = viewManageable.getMaster();
		if (mastered instanceof Identified) {
			masterInfo.setMasterName(((Identified) mastered).getName());
			masterInfo.setMasterNumber(((Identified) mastered).getNumber());
		}
		return masterInfo;
	}

	@Override
	public <T extends ViewChangeItem> List<T> getViewChangeItems(ATPublishPackage publishPackage, Class<T> c) {
		DetachedCriteria criteria = DetachedCriteria.forClass(c);
		criteria.add(Restrictions.eq("publishPackageRef.classId", publishPackage.getClassId()));
		criteria.add(Restrictions.eq("publishPackageRef.innerId", publishPackage.getInnerId()));
		return this.persistService.findByCriteria(criteria);
	}
	
	@Override
	public <T extends ViewChangePoint> List<T> getViewChangePoints(ViewChangeItem changeItem, Class<T> c) {
		DetachedCriteria criteria = DetachedCriteria.forClass(c);
		criteria.add(Restrictions.eq("viewChangeItemRef.classId", changeItem.getClassId()));
		criteria.add(Restrictions.eq("viewChangeItemRef.innerId", changeItem.getInnerId()));
		return this.persistService.findByCriteria(criteria);
	}

	@Override
	public <T extends ViewChangeConflict> List<T> getViewChangeConflicts(ViewChangePoint changePoint, Class<T> c) {
		DetachedCriteria criteria = DetachedCriteria.forClass(c);
		criteria.add(Restrictions.eq("pointRef.classId", changePoint.getClassId()));
		criteria.add(Restrictions.eq("pointRef.innerId", changePoint.getInnerId()));
		return this.persistService.findByCriteria(criteria);
	}

	/**
	 * 根据当前发布包获取所有前置发布包和前置发布任务
	 * @param publishPackage
	 * @return
	*/	
	public List<Object[]> listAllPrePackages(ATPublishPackage publishPackage){
		
			StringBuilder sb = new StringBuilder();
			sb.append(" select {t1.*}, {t2.*}");
			sb.append(" from   PLM_VIEW_PUB_PUBPACKAGE t1");
			sb.append(" left outer join PLM_VIEW_PUB_PUBTASK t2 ");
			sb.append(" on t1.innerid = t2.publishpackageid");
			sb.append(" start with t1.innerid = ? ");
			sb.append(" connect by prior t1.predecessorid = t1.innerid");
			sb.append(" order by level");
			
			Map<String,Class<?>> clazzMap = new LinkedHashMap<String, Class<?>>();
			clazzMap.put("t1", ATPublishPackage.class);
			clazzMap.put("t2", ATPublishTask.class);
			List<String> scalars = new ArrayList<String>();
			List<Object[]> listATPublishPackage = new ArrayList<Object[]>();
			if(publishPackage.getPredecessorRef() != null){
				listATPublishPackage =  Helper.getPersistService().query(sb.toString(), clazzMap, scalars, publishPackage.getPredecessorRef().getInnerId());
				return listATPublishPackage;
			}
			return listATPublishPackage;
	}

	/**
	 * 根据当前发布包判断其是否有未完成的前置任务
	 * @param publishPackage 
	 * @return
	 */
	public Boolean isHasUndoPreTask(ATPublishPackage publishPackage){
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select count(*) as TOTAL");
		sb.append(" from   PLM_VIEW_PUB_PUBPACKAGE t1");
		sb.append(" left outer join PLM_VIEW_PUB_PUBTASK t2");
		sb.append(" on t1.innerid = t2.publishpackageid");
		sb.append(" where t2.statename in ('执行中','新建','就绪','暂停')");
		sb.append(" start with t1.innerid = ? ");
		sb.append(" connect by prior t1.predecessorid = t1.innerid");
		
		if(publishPackage.getPredecessorRef() != null){
			List<Map<String,Object>> queryResult = Helper.getPersistService().query(sb.toString(), publishPackage.getPredecessorRef().getInnerId());
			if (queryResult.size()>0){
				Object totalObject = queryResult.get(0).get("TOTAL");
				if(totalObject instanceof BigDecimal){
					return ((BigDecimal)totalObject).compareTo(BigDecimal.valueOf(0))>0?true:false;
				}
			}
		}
		return false;
	}

	@Override
	public ATPublishTask getPublishTask(ATPublishPackage publishPackage) {
		
		ATPublishTask publishTask = null;
		List result = null;
		List<Object> paramList = null;
		paramList = new ArrayList<Object>();
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select task");
		sb.append(" from  ATPublishTask task");
		sb.append(" where task.publishPackageRef.innerId = ?");
		paramList.add(publishPackage.getInnerId());
		result = Helper.getPersistService().find(sb.toString(), paramList.toArray());
		
		if(result.size()>0){
			publishTask = (ATPublishTask) result.get(0);
		}
		return publishTask;
	}
	
	/**
	 * 判断发布包是否可以提交
	 * @param publishPackage
	 * @return
	 */
	public Boolean isCanSubmit(ATPublishPackage publishPackage){
		//同一个主对象的部件的发布任务有约束，限制后续的发布任务不能提交；
		//不同主对象的部件之间不应该有约束
		StringBuilder sb = new StringBuilder();
		sb.append(" select count(*) as TOTAL");
		sb.append(" from plm_view_pub_pubpackage pub, PLM_VIEW_PUB_PUBTASK task, plm_part_part part");
		sb.append(" where pub.publishpartid = part.innerid");
		sb.append(" and pub.innerid = task.publishpackageid");
		sb.append(" and task.statename in ('执行中', '新建', '就绪', '暂停')");
		sb.append(" and part.masterid = ?");
		if(publishPackage.getPredecessorRef() != null){
			List<Map<String,Object>> list = Helper.getPersistService().query(sb.toString(), publishPackage.getPublishPart().getMaster().getInnerId());
			if (list.size()>0){
				return Long.parseLong(list.get(0).get("TOTAL").toString()) > 1 ? false : true;
				
			}
		}
		return true;
	}

}
