package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.view.ViewManageable;

/**
 * 视图转换冲突服务接口
 * @author zhuhongtao
 * @since 2014-6-23
 */
public interface ViewConflictService {

	/**
	 * 根据更改点和下游部件，返回对应该部件的冲突,若返回为null，则该下游部件无冲突
	 * @param changePoint
	 * @param downStream
	 * @return 视图转换冲突
	 */
	public <T extends ViewChangeConflict> T getConflictByDownstream(ViewChangePoint changePoint, ViewManageable downStream, Class<T> c);
	
}
