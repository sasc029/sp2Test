package com.bjsasc.plm.core.view.publish.model;

import java.util.ArrayList;
import java.util.List;

import com.bjsasc.platform.objectmodel.managed.constants.RelationRole;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.view.ViewManageable;

/**
 * ��������Ա��ϵ��From�ˣ��������� To�ˣ�����ͼ�������
 * ά�����η�����Ҫ���������µĶ���ͷ������Ĺ�ϵ������ͳһ�����ȡ�������
 * @author avidm
 *
 */
public class ATPackageMemberLink extends ATLink {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5780746845922158521L;
	public static final String CLASSID = ATPackageMemberLink.class.getSimpleName();
	
	/**
	 * ��ϵ����
	 */
	public static final String ADD = "Add";
	public static final String MODIFY = "Modify";
	
	private String changeType; 
	
	public ATPackageMemberLink() {
		setClassId(CLASSID);
	}

	public String getChangeType() {
		return changeType;
	}

	public void setChangeType(ViewChangeType changeType) {
		this.changeType = changeType.name();
	}
	
	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}
	
	public ATPublishPackage getPublishPackage(){
		return (ATPublishPackage) getFromObject();
	}
	
	public void setPublishPackage(ATPublishPackage publishPackage){
		setFromObject(publishPackage);
	}
	
	public ViewManageable getViewManageable(){
		return (ViewManageable) getToObject();
	}
	
	public void setViewManageable(ViewManageable viewManageable){
		setToObject(viewManageable);
	}
	
	/**
	 * ��ȡ��������Ա
	 * @param changeType 
	 * @param baseline Ŀ�����
	 * @return ���߳�Ա�б�
	 */
	public static List<ViewManageable> getTo(ATPublishPackage publishPackage, ViewChangeType changeType){
		List<ViewManageable> result = new ArrayList<ViewManageable>();
		List<Persistable> list = PersistHelper.getService().navigate(publishPackage, RelationRole.FROM, CLASSID, false);
		for (Persistable link : list) {
			if(link instanceof ATPackageMemberLink) {
				ATPackageMemberLink memberLink = (ATPackageMemberLink) link;
				if (ViewChangeType.valueOf(memberLink.getChangeType()).equals(changeType)) {
					result.add(memberLink.getViewManageable());
				}
			}
		}
		return result;
	}
	
}
