package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.type.ATObject;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;

public abstract class AbstractViewChangePoint extends ATObject implements ViewChangePoint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2548098987155206483L;
	
	/**
	 * 所属更改项
	 */
	private ObjectReference viewChangeItemRef;
	/**
	 * 是否冲突
	 */
	private boolean isConflict = false;
	/**
	 * 备注
	 */
	private String note;
	/**
	 * 处理方式
	 */
	private String deal;
	/**
	 * 执行结果
	 */
	private String result;

	public ObjectReference getViewChangeItemRef() {
		return viewChangeItemRef;
	}

	public void setViewChangeItemRef(ObjectReference viewChangeItemRef) {
		this.viewChangeItemRef = viewChangeItemRef;
	}

	public ViewChangeItem getViewChangeItem() {
		if(viewChangeItemRef==null){
			return null;
		}
		return (ViewChangeItem) this.viewChangeItemRef.getObject();
	}
	
    public void setViewChangeItem(ViewChangeItem changeItem) {
    	this.viewChangeItemRef = ObjectReference.newObjectReference(changeItem);
	}
    
	public boolean getIsConflict() {
		return isConflict;
	}
	
	public void setIsConflict(boolean isConflict) {
		this.isConflict = isConflict;
	}
	
	public View getSourceView() {
		return getViewChangeItem().getSourceView();
	}
	
	public View getTargetView() {
		return getViewChangeItem().getTargetView();
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	public ATPublishPackage getPublishPackage() {
		return getViewChangeItem().getPublishPackage();
	}

	@Override
	public int hashCode() {
		if (getInnerId() != null) {
			return getInnerId().hashCode();
		}
		return super.hashCode();
	}

	public String getDeal() {
		return deal;
	}

	public void setDeal(String deal) {
		this.deal = deal;
	}
	
}
