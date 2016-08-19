package com.bjsasc.plm.core.view.publish.model;

import java.util.ArrayList;
import java.util.List;

import com.bjsasc.platform.objectmodel.managed.constants.RelationRole;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.view.ViewManageable;

/**
 * 发布包成员关系（From端：发布包， To端：受视图管理对象）
 * 维护本次发布需要新增、更新的对象和发布包的关系（用来统一检入和取消检出）
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
	 * 关系类型
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
	 * 获取发布包成员
	 * @param changeType 
	 * @param baseline 目标基线
	 * @return 基线成员列表
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
