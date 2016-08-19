package com.bjsasc.plm.core.view.publish.service;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.PersistService;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewChangeConflict;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.ViewConflictService;

public class ViewConflictServiceImpl implements ViewConflictService {
	
	private PersistService persistService = PersistHelper.getService();
	
	public <T extends ViewChangeConflict> T getConflictByDownstream(
			ViewChangePoint changePoint, ViewManageable downStream, Class<T> c) {
		DetachedCriteria criteria = DetachedCriteria.forClass(c);
		criteria.add(Restrictions.eq("pointRef.classId", changePoint.getClassId()));
		criteria.add(Restrictions.eq("pointRef.innerId", changePoint.getInnerId()));
		criteria.add(Restrictions.eq("downStreamRef.classId", downStream.getClassId()));
		criteria.add(Restrictions.eq("downStreamRef.innerId", downStream.getInnerId()));
		List<ViewChangeConflict> list = persistService.findByCriteria(criteria);
		if (list != null && list.size() > 0) {
			return (T) list.get(0);
		}
		return null;
	}

}
