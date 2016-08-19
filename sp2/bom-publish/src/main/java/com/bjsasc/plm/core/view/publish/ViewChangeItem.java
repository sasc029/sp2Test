package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
/**
 * 对象接口：视图转换更改项
 * @author zhuhongtao
 *
 */

public interface ViewChangeItem extends Persistable {
	/**
	 * 
	 * @return 影响转换对象
	 */
	public ViewManageable getAffectedViewManageable();
	/**
	 * 
	 * @return 发布包
	 */
	public ATPublishPackage getPublishPackage();
	/**
	 * 
	 * @return 源视图
	 */
	public View getSourceView();
	/**
	 * 
	 * @return 目标视图
	 */
	public View getTargetView();
	/**获取处理方式
	 * @return
	 */
	public String getDeal();

	/**设置处理方式
	 * @param deal
	 */
	public void setDeal(String deal);
}
