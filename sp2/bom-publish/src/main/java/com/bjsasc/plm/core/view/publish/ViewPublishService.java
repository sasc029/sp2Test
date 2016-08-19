package com.bjsasc.plm.core.view.publish;

import java.util.List;

import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.model.ATLinkSnapshot;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATPublishTask;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;

/**
 * 视图发布服务
 * @author caorang
 * @since 2014-06-17
 */
public interface ViewPublishService {
    
	/**
	 * 新建快照对象
	 * @return 部件 
	 */
	public ATSnapshot newATSnapshot(ViewManageable viewManageable);
	
	/**
	 * 新建关系快照对象
	 * @return 部件 
	 */
	public ATLinkSnapshot newATLinkSnapshot(ATLink link);
	
	/**
	 * 根据发布包，获取该发布包下特定的更改点集合
	 * @param publishPackage
	 * @param c
	 * @return 更改点集合
	 */
	public <T extends ViewChangeItem> List<T> getViewChangeItems(ATPublishPackage publishPackage, Class<T> c);
	
	/**
	 * 获取更改项下所有更改点
	 */
	public <T extends ViewChangePoint> List<T> getViewChangePoints(ViewChangeItem changeItem, Class<T> c);
	
	/**
	 * 获取更改点下所有更改冲突
	 */
	public <T extends ViewChangeConflict> List<T> getViewChangeConflicts(ViewChangePoint changePoint, Class<T> c);
	
	/**
	 * 根据当前发布包获取所有前置发布包和前置发布任务
	 * @param publishPackage
	 * @return
	*/	
	public List<Object[]> listAllPrePackages(ATPublishPackage publishPackage);
	
	/**
	 * 用于判断是否有未完成的前置任务
	 * @param publishPackage
	 * @return
	 */
	public Boolean isHasUndoPreTask(ATPublishPackage publishPackage);

	/**
	 * 根据当前发布包获取发布包中的任务
	 * @param publishPackage
	 * @return
	*/	
	public ATPublishTask getPublishTask(ATPublishPackage publishPackage);
	
	/**
	 * 判断发布包是否可以提交
	 * 
	 * @author liyu
	 * @param publishPackage
	 * @return
	 */
	public Boolean isCanSubmit(ATPublishPackage publishPackage);
}
