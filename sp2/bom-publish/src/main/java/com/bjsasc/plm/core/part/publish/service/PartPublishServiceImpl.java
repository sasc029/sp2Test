package com.bjsasc.plm.core.part.publish.service;

import java.util.ArrayList;
import java.util.List;

import com.bjsasc.plm.core.Helper;
import com.bjsasc.plm.core.baseline.BaselineHelper;
import com.bjsasc.plm.core.baseline.model.Baseline;
import com.bjsasc.plm.core.baseline.model.BaselineMemberLink;
import com.bjsasc.plm.core.baseline.model.Baselined;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.part.publish.PartPublishService;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.view.publish.model.ATLinkSnapshot;
import com.bjsasc.plm.core.view.publish.model.ATPublishBaseline;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;
import com.bjsasc.plm.core.view.publish.service.ViewPublishServiceImpl;

public class PartPublishServiceImpl extends ViewPublishServiceImpl implements PartPublishService {

	
	public void createViewChangeItems(ATPublishPackage publishPackage) {
		// TODO Auto-generated method stub
		//获得发布和汇总基线
		ATPublishBaseline publishBaseline =  publishPackage.getPublishBaseline();
		ATPublishBaseline summaryBaseline =  publishPackage.getSummaryBaseline();
		Part rootPart = (Part)publishPackage.getPublishPart();
		//比较
	}
	/**
	 * 在发布基线中找部件快照
	 * @param part
	 * @return
	 */
	public ATSnapshot getPartSnapshotInPublishBaseline(PartMaster partMaster,ATPublishBaseline publishBaseline){
		
		StringBuilder hqls = new StringBuilder();
		List<Object> hql_paramList = new ArrayList<Object>(); 
		hql.append(" select snapshot from BaselineMemberLink baseline,ATSnapshot snapshot,Part part");
		
		hql.append(" where baseline.fromObjectRef.innerId = ?");
		hql_paramList.add(publishBaseline.getInnerId());
		
		hql.append(" and baseline.toObjectRef.innerId = snapshot.innerId");
		hql.append(" and snapshot.viewManageableRef.innerId = part.innerId");
		hql.append(" and part.masterRef.innerId = ?");
		hql_paramList.add(partMaster.getInnerId());
		Sysout;
		
		@SuppressWarnings("unchecked")
		List<ATSnapshot> snapshotList =  Helper.getPersistService().find(hql.toString(), hql_paramList.toArray());
		
		if(snapshotList!=null&&snapshotList.size()>0){
			return snapshotList.get(0);
		}
		return null;
	}
	/**
	 * 获取部件快照在发布基线中的子
	 * @param publishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<ATSnapshot> getChildInPublishBaseline(ATPublishBaseline publishBaseline,ATSnapshot atSnapshot){
		
		Part part = (Part)atSnapshot.getViewManageable();
		List<BaselineMemberLink> baselineMemberLinks = BaselineHelper.getService().getBaselineMemberLinks(publishBaseline);
		List<ATSnapshot> atSnapshots = new ArrayList<ATSnapshot>();
		for(BaselineMemberLink baselineMemberLink :baselineMemberLinks){
			Persistable persistable = baselineMemberLink.getTo();
			if(persistable instanceof ATLinkSnapshot){
				ATLinkSnapshot atLinkSnapshot = (ATLinkSnapshot)persistable;
				ATLink atLink = (ATLink)atLinkSnapshot.getLink();
				if(atLink instanceof PartUsageLink){
					PartUsageLink partUsageLink = (PartUsageLink)atLink;
					String innerId = partUsageLink.getUsedByObject().getInnerId();
					if(innerId.equals(part.getInnerId())){
						atSnapshots.add(getPartSnapshotInPublishBaseline(partUsageLink.getUsesObject(), publishBaseline));
					}
				}

			}
		}
		return atSnapshots;
	}
	
	/**
	 * 在发布基线中找部件快照,并且该部件快照对应的部件没有被删除
	 * @param partMaster
	 * @return
	 */
	public List<BaselineMemberLink> getPartSnapshotInPublishBaseline(Baseline baseline){
		StringBuilder hql = new StringBuilder();
		List<Object> hql_paramList = new ArrayList<Object>(); 
		hql.append(" select baselineMemberLink,snapshot, part from BaselineMemberLink baselineMemberLink,ATSnapshot snapshot,Part part");
		
		hql.append(" where baselineMemberLink.fromObjectRef.innerId = ?");
		hql_paramList.add(baseline.getInnerId());
		
		hql.append(" and baselineMemberLink.toObjectRef.innerId = snapshot.innerId");
		hql.append(" and snapshot.viewManageableRef.innerId = part.innerId");
		
		List queryResultList =  Helper.getPersistService().find(hql.toString(), hql_paramList.toArray());
		
		List<BaselineMemberLink> returnBaselineMemberLink = new ArrayList<BaselineMemberLink>();
		if(queryResultList!=null&&queryResultList.size()>0){
			for(Object temp:queryResultList){
				Object[] queryResult = (Object[])temp;
				BaselineMemberLink tempbaselineMemberLink = (BaselineMemberLink)queryResult[0];
				ATSnapshot tempATSnapshot = (ATSnapshot)queryResult[1];
				Part tempPart = (Part)queryResult[2];
				tempATSnapshot.setViewManageable(tempPart);
				tempbaselineMemberLink.setBaselineItem(tempATSnapshot);
				tempbaselineMemberLink.setBaseline(baseline);
				returnBaselineMemberLink.add(tempbaselineMemberLink);
			}
		}
		return returnBaselineMemberLink;
		
	}
}

