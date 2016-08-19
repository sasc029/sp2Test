package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.ViewManageable;
/**
 * 对象接口：视图转换冲突
 * @author zhuhongtao
 *
 */

public interface ViewChangeConflict extends Persistable{

	/**
	 * 冲突解决方案类别
	 */
	public static final String SOLUTION_RESERVE = "Reserve";
	public static final String SOLUTION_COVER = "Cover";
	public static final String SOLUTION_CUSTOM = "Custom";
	
	/**
	 * 
	 * @return 视图转换更改点
	 */
	public ViewChangePoint getViewChangePoint();
	/**
	 * 
	 * @return 下游试图对等对象
	 */
	public ViewManageable getDownStream();
	/**
	 * 
	 * @return 解决方案
	 */
	public String getSolution();
}
