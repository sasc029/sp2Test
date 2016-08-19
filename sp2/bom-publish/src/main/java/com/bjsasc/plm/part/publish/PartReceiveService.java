package com.bjsasc.plm.part.publish;

import java.util.*;

import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;

/**
 * 视图转换部件接收Web服务类
 * @author avidm
 * @since 2014-6-19
 */
public interface PartReceiveService {

	/**
	 * 根据发布包和选择的更改点进行更改提交，并生成相应的更改日志
	 * @param publishPackage
	 * @param changePoints
	 */
	public void executeChangePoints(ATPublishPackage publishPackage, Collection<ViewChangePoint> selectedPoints);
	
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
	
}
