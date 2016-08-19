package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.type.ATObject;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewChangeConflict;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.conflict.ChangeSolution;

public abstract class AbstractViewChangeConflict extends ATObject implements ViewChangeConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8019867175157606787L;

	/**
	 * 所属更改点
	 */
	private ObjectReference pointRef;
	/**
	 * 关联下游部件
	 */
	private ObjectReference downStreamRef;
	/**
	 * 解决方案
	 */
	private String solution;
	
	public ObjectReference getPointRef() {
		return pointRef;
	}

	public void setPointRef(ObjectReference pointRef) {
		this.pointRef = pointRef;
	}

	public ObjectReference getDownStreamRef() {
		return downStreamRef;
	}

	public void setDownStreamRef(ObjectReference downStreamRef) {
		this.downStreamRef = downStreamRef;
	}

	@Override
	public ViewChangePoint getViewChangePoint() {
		return (ViewChangePoint) this.pointRef.getObject();
	}
	
	public void setViewChangePoint(ViewChangePoint viewChangePoint) {
		this.pointRef = ObjectReference.newObjectReference(viewChangePoint);
	}
	
	@Override
	public ViewManageable getDownStream() {
		return (ViewManageable) this.downStreamRef.getObject();
	}
	
	public void setDownStream(ViewManageable viewManageable) {
		this.downStreamRef = ObjectReference.newObjectReference(viewManageable);
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}
	
	public abstract void solveBy(ChangeSolution solution);
	
}
