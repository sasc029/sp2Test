package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.View;
/**
 * 对象借口：视图转换更改点
 * @author zhuhongtao
 *
 */ 


public interface ViewChangePoint extends Persistable {
	/**
	 * @return 视图转换更改项
	 */
	public ViewChangeItem getViewChangeItem();
	/**
	 * 设置视图转换更改项
	 */
	public void setViewChangeItem(ViewChangeItem changeItem);
	/**
	 * @return 源视图
	 */
	public View getSourceView();
	/**
	 * @return 目标视图
	 */
	public View getTargetView();
	/**
	 * 是否冲突
	 */
	public boolean getIsConflict();
	
	public void setIsConflict(boolean isConflict);
	/**
	 * 获取备注
	 */
	public String getNote();
	/**
	 * 获取执行结果
	 */
	public String getResult();
	/**
	 * 设置执行结果
	 */
	public void setResult(String result);
	/**
	 * 获得更改类型描述
	 */
	public String getChangeTip();
	
	/**获取目标对象类型
	 * @return
	 */
	public Persistable getTarget();
	
	/**获取处理方式
	 * @return
	 */
	public String getDeal();

	/**设置处理方式
	 * @param deal
	 */
	public void setDeal(String deal);
}
