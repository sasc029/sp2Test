package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.type.ATObject;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;

/**
 * ��ͼת�����ļ�¼
 * 
 * @author caorang
 * @since 2014-6-25
 */
public class ATViewChangeLog extends ATObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7351915358385712522L;
	public static final String CLASSID = ATViewChangeLog.class.getSimpleName();
	/**
	 * �����ĸ��ĵ�
	 */
	private ObjectReference changeItemRef;
	/**
	 * �����ĵ���ͼ�ܹ������
	 */
	private ObjectReference toChangeRef;
	/**
	 * ��ע
	 */
	private String note;
	
	public ATViewChangeLog() {
		setClassId(CLASSID);
	}
	
	public ObjectReference getChangeItemRef() {
		return changeItemRef;
	}
	
	public void setChangeItemRef(ObjectReference changeItemRef) {
		this.changeItemRef = changeItemRef;
	}
	
	public ViewChangeItem getChangeItem() {
		return (ViewChangeItem) changeItemRef.getObject();
	}
	
	public void setChangeItem(ViewChangeItem changeItem) {
		this.changeItemRef = ObjectReference.newObjectReference(changeItem);
	}
	
	public ObjectReference getToChangeRef() {
		return toChangeRef;
	}
	
	public void setToChangeRef(ObjectReference toChangeRef) {
		this.toChangeRef = toChangeRef;
	}
	public ViewManageable getToChange() {
		return (ViewManageable) toChangeRef.getObject();
	}
	
	public void setToChange(ViewManageable toChange) {
		this.toChangeRef = ObjectReference.newObjectReference(toChange);
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
}
