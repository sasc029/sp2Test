package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.baseline.model.Baselined;
import com.bjsasc.plm.core.type.ATObject;
import com.bjsasc.plm.core.view.ViewManageable;

/**
 * ����ͼ����������
 * @author caorang
 * @since 2014-6-6
 * @version 1.0.0.0
 */
public class ATSnapshot extends ATObject implements Baselined {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2010036726868351885L;
	public static final String CLASSID = ATSnapshot.class.getSimpleName();
	
	/**
	 * ��������
	 */
	private ObjectReference viewManageableRef;
	
	/**
	 * ��������Ϣ
	 */
	private MasterInfo masterInfo;
	
	public ATSnapshot(){
		setClassId(ATSnapshot.CLASSID);
	}
	
	public ObjectReference getViewManageableRef() {
		return viewManageableRef;
	}

	public void setViewManageableRef(ObjectReference viewManageableRef) {
		this.viewManageableRef = viewManageableRef;
	}

	public ViewManageable getViewManageable() {
		return (ViewManageable) viewManageableRef.getObject();
	}
	
	public void setViewManageable(ViewManageable vm) {
		this.viewManageableRef = ObjectReference.newObjectReference(vm);
	}

	public MasterInfo getMasterInfo() {
		return masterInfo;
	}

	public void setMasterInfo(MasterInfo masterInfo) {
		this.masterInfo = masterInfo;
	}
	
}
