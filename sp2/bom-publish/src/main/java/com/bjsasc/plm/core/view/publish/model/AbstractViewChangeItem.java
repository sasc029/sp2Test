package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.type.ATObject;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;

/**
 * 视图转换更改项抽象类
 * @author avidm
 */
public abstract class AbstractViewChangeItem extends ATObject implements ViewChangeItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7360961886467458864L;
    public static final String CLASSID = AbstractViewChangeItem.class.getSimpleName();
	
    /**
     * 受影响的部件
     */
	private ObjectReference affectedViewManageableRef;
	/**
	 * 所属发布包
	 */
	private ObjectReference publishPackageRef;
	/**
	 * 处理方式
	 */
	private String deal;
	
	public ObjectReference getAffectedViewManageableRef() {
		return affectedViewManageableRef;
	}
	
	public void setAffectedViewManageableRef(ObjectReference affectedViewManageableRef) {
		this.affectedViewManageableRef = affectedViewManageableRef;
	}
	
	public ViewManageable getAffectedViewManageable() {
		if(affectedViewManageableRef==null){
			return null;
		}
		return (ViewManageable) this.affectedViewManageableRef.getObject();
	}
	
	public void setAffectedViewManageable(ViewManageable viewManageable) {
		this.affectedViewManageableRef = ObjectReference.newObjectReference(viewManageable);
	}
	
	public ObjectReference getPublishPackageRef() {
		return publishPackageRef;
	}
	
	public void setPublishPackageRef(ObjectReference publishPackageRef) {
		this.publishPackageRef = publishPackageRef;
	}
	
	public ATPublishPackage getPublishPackage() {
		if(publishPackageRef==null){
			return null;
		}
		return (ATPublishPackage) this.publishPackageRef.getObject();
	}
	
	public void setPublishPackage(ATPublishPackage publishPackage) {
		this.publishPackageRef = ObjectReference.newObjectReference(publishPackage);
	}
	
	public View getSourceView() {
		return getPublishPackage().getSourceView();
	}
	
    public View getTargetView() {
    	return getPublishPackage().getTargetView();
	}

	public String getDeal() {
		return deal;
	}

	public void setDeal(String deal) {
		this.deal = deal;
	}
	
}
