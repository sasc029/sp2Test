package com.bjsasc.plm.part.publish.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.formula.functions.T;

import com.bjsasc.plm.core.lifecycle.LifeCycleHelper;
import com.bjsasc.plm.core.lifecycle.LifeCycleService;
import com.bjsasc.plm.core.part.publish.PartPublishHelper;
import com.bjsasc.plm.core.part.publish.PartPublishService;
import com.bjsasc.plm.core.part.publish.PartReceiveService;
import com.bjsasc.plm.core.part.publish.model.PartAddChangeItem;
import com.bjsasc.plm.core.part.publish.model.PartAddChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartModifyChangeItem;
import com.bjsasc.plm.core.util.PlmException;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;

/**
 * 视图转换部件接收Web服务实现
 * @author caorang
 * @since 2014-6-25
 */
public class PartReceiveServiceImpl implements com.bjsasc.plm.part.publish.PartReceiveService {
	
	private PartPublishService publishService = PartPublishHelper
			.getPublishService();
	private PartReceiveService receiveService = PartPublishHelper
			.getReceiveService();
	private LifeCycleService lifeCycleService = LifeCycleHelper.getService();

	@Override
	public void executeChangePoints(ATPublishPackage publishPackage,
			Collection<ViewChangePoint> selectedPoints) {
		// 如果发布包已经终止则不能再执行更改点
		if (lifeCycleService.isInFinalPhase(publishPackage)) {
			throw new PlmException(
					"plm.part.receive.executeChangePoints.publishPackage.terminate",
					publishPackage.getNumber());
		}
		// 先将上次执行的中间结果进行回滚
		rollbackChange(publishPackage);
		// 获取该发布包下的新增更改项和修改更改项
		List<PartAddChangeItem> addItems = publishService.getViewChangeItems(
				publishPackage, PartAddChangeItem.class);
		List<PartModifyChangeItem> modifyItems = publishService
				.getViewChangeItems(publishPackage, PartModifyChangeItem.class);
		View targetView = publishPackage.getTargetView();
		// 执行新增更改项新增部件
		for (PartAddChangeItem addItem : addItems) {
			// 获取在该更改项下的新增部件点
			Set<ViewChangePoint> selected = getSelectedPointsInItem(addItem,
					selectedPoints, PartAddChangePoint.class);
			if (selected.size() > 0) {
				receiveService.executeAddChangeItem(targetView, addItem, selected);
			}
		}
		// 执行新增更改项修改新增部件
		for (PartAddChangeItem addItem : addItems) {
			// 获取在该更改项下的关系修改点
			Set<ViewChangePoint> selected = getSelectedPointsInItem(addItem,
					selectedPoints, PartLinkChangePoint.class);
			if (selected.size() > 0) {
				receiveService.executeModifyChangeItem(targetView, addItem,
						selected);
			}
		}
		// 再执行修改更改项修改已有部件
		for (PartModifyChangeItem modifyItem : modifyItems) {
			// 获取在该更改项下的属性修改点和关系修改点
			Set<ViewChangePoint> selected = new HashSet<ViewChangePoint>();
			selected.addAll(getSelectedPointsInItem(modifyItem, selectedPoints,
					PartAttrChangePoint.class));
			selected.addAll(getSelectedPointsInItem(modifyItem, selectedPoints,
					PartLinkChangePoint.class));
			if (selected.size() > 0) {
				receiveService.executeModifyChangeItem(targetView, modifyItem,
						selected);
			}
		}
	}

	private <T extends ViewChangePoint> Set<ViewChangePoint> getSelectedPointsInItem(
			ViewChangeItem item, Collection<ViewChangePoint> selectedPoints,
			Class<T> c) {
		Set<ViewChangePoint> result = new HashSet<ViewChangePoint>();
		// 获取更改点
		List<T> points = publishService.getViewChangePoints(item, c);
		// 获取选中的更改点集合
		for (T point : points) {
			if (selectedPoints.contains(point)) {
				result.add(point);
			}
		}
		return result;
	}

	@Override
	public void commitChange(ATPublishPackage publishPackage) {
		receiveService.commitChange(publishPackage);
	}

	@Override
	public void rollbackChange(ATPublishPackage publishPackage) {
		receiveService.rollbackChange(publishPackage);
	}

}
