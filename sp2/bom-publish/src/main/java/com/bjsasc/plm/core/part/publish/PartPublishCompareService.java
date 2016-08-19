package com.bjsasc.plm.core.part.publish;

import java.util.List;
import java.util.Map;

import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.link.PartDecribeLink;
import com.bjsasc.plm.core.part.link.PartReferenceLink;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.model.ATPublishBaseline;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;

public interface PartPublishCompareService {
	/**
	 * �ȽϿ���BOM���������и�����
	 * @param partMaster
	 * @param publishPackage
	 */
	public void createViewChangeItemsBySnapshot(PartMaster partMaster, ATPublishPackage publishPackage);
	/**
	 * �Ƚ��������գ����ɸ��ĵ�
	 * @param publishSnapshot
	 * @param summarySnapshot
	 * @return
	 */
	public List<ViewChangePoint> createViewChangePoint(
			ATSnapshot publishSnapshot, ATSnapshot summarySnapshot,
			ATPublishBaseline publishBaseline, ATPublishBaseline summaryBaseline);
	/**
	 * ��ȡ������
	 * @param part Ӱ�첿��
	 * @param atPublishPackage ������
	 * @return
	 */
	public ViewChangeItem getViewChangeItem(Part affectedpart,ATPublishPackage atPublishPackage);
	/**
	 * ������������,���ɸ��ĵ�
	 * @param createdSnapshot
	 * @return
	 */
	public List<ViewChangePoint> createViewChangePoint(ATSnapshot createdSnapshot,ATPublishBaseline publishBaseline, ATPublishBaseline summaryBaseline);		
	/**
	 * �ڷ��������л�ȡ���յĲο���ϵ
	 * @param atPublishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<PartReferenceLink> getPartReferenecLinkInPublishBaseline(ATPublishBaseline atPublishBaseline,ATSnapshot atSnapshot);
	/**
	 * �ڷ��������л�ȡ���յ�������ϵ
	 * @param atPublishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<PartDecribeLink> getPartDecribeLinkInPublishBaseline(ATPublishBaseline atPublishBaseline,ATSnapshot atSnapshot);
	/**
	 * �ڷ��������л�ȡ���յ�ʹ�ù�ϵ
	 * @param atPublishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<PartUsageLink> getPartUsageLinkInPublishBaseline(ATPublishBaseline atPublishBaseline,ATSnapshot atSnapshot);
	/**
	 * �ڷ�����������ָ�����ӵ�ʹ�ù�ϵ
	 * @param parentPartMaster
	 * @param childPartMaster
	 * @param atPublishBaseline
	 * @return
	 */
	public PartUsageLink getPartUsageLinkFromPublishBaseline(PartMaster parentPartMaster,PartMaster childPartMaster,ATPublishBaseline atPublishBaseline);
	/**
	 * ���ɳ�ͻ
	 * @param atPublishPackage
	 */
	public void createViewChangeConflicts(ATPublishPackage atPublishPackage);
	/**
	 * ������и��ĵ�
	 * @param atPublishPackage
	 * @return
	 */
	public List<ViewChangePoint> getViewChangePoints(ATPublishPackage atPublishPackage);
}
