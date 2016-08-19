package com.bjsasc.plm.core.view.publish;

import java.util.Collection;
import java.util.List;

import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ViewChangeType;

/**
 * 视图接收服务
 * @author caorang
 * @since 2014-06-17
 */
public interface ViewReceiveService {
	
	/**
	 * 应用新增更改项
	 * @param targetView 下游视图
	 * @param item 更改项
	 * @param selected 
	 */
	public void executeAddChangeItem(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected);
	
	/**
	 * 应用修改更改项
	 * @param viewManageable 部件
	 * @param targetView 下游视图
	 * @param item 更改项
	 * @param selected 
	 */
	public void executeModifyChangeItem(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected);
	
	/**
	 * 将新增或者修改的对象添加至发布包
	 * @param viewManageable
	 * @param publishPackage
	 * @param changeType
	 */
	public void addMemberToPackage(ViewManageable viewManageable, ATPublishPackage publishPackage, ViewChangeType changeType);
	
	/**
	 * 根据类型获取发布包成员列表
	 * @param publishPackage
	 * @param changeType
	 */
	public List<ViewManageable> getMembersFromPackage(ATPublishPackage publishPackage, ViewChangeType changeType);
	
	/**
	 * 将发布包成员列表清空
	 * @param publishPackage
	 */
	public void clearPackageMembers(ATPublishPackage publishPackage);
	
	/**
	 * 提交更改，检入发布包内所有待检入成员对象
	 * @param publishPackage
	 */
	public void commitChange(ATPublishPackage publishPackage);
	
	/**
	 * 回滚更改，将发布包内检出的对象取消检出，新增的对象进行删除
	 * @param publishPackage
	 */
	public void rollbackChange(ATPublishPackage publishPackage);
	
	/**
	 * 更新发布汇总基线
	 * @param publishPackage 新的发布包 
	 * @param updateType 1,汇总基线 2,临时汇总基线
	 */
	public void updateSummaryBaseline(ATPublishPackage publishPackage);
	
}
