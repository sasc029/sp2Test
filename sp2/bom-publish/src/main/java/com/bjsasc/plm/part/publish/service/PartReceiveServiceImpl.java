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
 * ��ͼת����������Web����ʵ��
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
		// ����������Ѿ���ֹ������ִ�и��ĵ�
		if (lifeCycleService.isInFinalPhase(publishPackage)) {
			throw new PlmException(
					"plm.part.receive.executeChangePoints.publishPackage.terminate",
					publishPackage.getNumber());
		}
		// �Ƚ��ϴ�ִ�е��м������лع�
		rollbackChange(publishPackage);
		// ��ȡ�÷������µ�������������޸ĸ�����
		List<PartAddChangeItem> addItems = publishService.getViewChangeItems(
				publishPackage, PartAddChangeItem.class);
		List<PartModifyChangeItem> modifyItems = publishService
				.getViewChangeItems(publishPackage, PartModifyChangeItem.class);
		View targetView = publishPackage.getTargetView();
		// ִ��������������������
		for (PartAddChangeItem addItem : addItems) {
			// ��ȡ�ڸø������µ�����������
			Set<ViewChangePoint> selected = getSelectedPointsInItem(addItem,
					selectedPoints, PartAddChangePoint.class);
			if (selected.size() > 0) {
				receiveService.executeAddChangeItem(targetView, addItem, selected);
			}
		}
		// ִ�������������޸���������
		for (PartAddChangeItem addItem : addItems) {
			// ��ȡ�ڸø������µĹ�ϵ�޸ĵ�
			Set<ViewChangePoint> selected = getSelectedPointsInItem(addItem,
					selectedPoints, PartLinkChangePoint.class);
			if (selected.size() > 0) {
				receiveService.executeModifyChangeItem(targetView, addItem,
						selected);
			}
		}
		// ��ִ���޸ĸ������޸����в���
		for (PartModifyChangeItem modifyItem : modifyItems) {
			// ��ȡ�ڸø������µ������޸ĵ�͹�ϵ�޸ĵ�
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
		// ��ȡ���ĵ�
		List<T> points = publishService.getViewChangePoints(item, c);
		// ��ȡѡ�еĸ��ĵ㼯��
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
