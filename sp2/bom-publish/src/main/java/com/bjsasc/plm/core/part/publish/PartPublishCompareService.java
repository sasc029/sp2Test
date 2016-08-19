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
	 * 比较快照BOM，生成所有更改项
	 * @param partMaster
	 * @param publishPackage
	 */
	public void createViewChangeItemsBySnapshot(PartMaster partMaster, ATPublishPackage publishPackage);
	/**
	 * 比较两个快照，生成更改点
	 * @param publishSnapshot
	 * @param summarySnapshot
	 * @return
	 */
	public List<ViewChangePoint> createViewChangePoint(
			ATSnapshot publishSnapshot, ATSnapshot summarySnapshot,
			ATPublishBaseline publishBaseline, ATPublishBaseline summaryBaseline);
	/**
	 * 获取更改项
	 * @param part 影响部件
	 * @param atPublishPackage 发布包
	 * @return
	 */
	public ViewChangeItem getViewChangeItem(Part affectedpart,ATPublishPackage atPublishPackage);
	/**
	 * 根据新增快照,生成更改点
	 * @param createdSnapshot
	 * @return
	 */
	public List<ViewChangePoint> createViewChangePoint(ATSnapshot createdSnapshot,ATPublishBaseline publishBaseline, ATPublishBaseline summaryBaseline);		
	/**
	 * 在发布基线中获取快照的参考关系
	 * @param atPublishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<PartReferenceLink> getPartReferenecLinkInPublishBaseline(ATPublishBaseline atPublishBaseline,ATSnapshot atSnapshot);
	/**
	 * 在发布基线中获取快照的描述关系
	 * @param atPublishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<PartDecribeLink> getPartDecribeLinkInPublishBaseline(ATPublishBaseline atPublishBaseline,ATSnapshot atSnapshot);
	/**
	 * 在发布基线中获取快照的使用关系
	 * @param atPublishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<PartUsageLink> getPartUsageLinkInPublishBaseline(ATPublishBaseline atPublishBaseline,ATSnapshot atSnapshot);
	/**
	 * 在发布基线中找指定父子的使用关系
	 * @param parentPartMaster
	 * @param childPartMaster
	 * @param atPublishBaseline
	 * @return
	 */
	public PartUsageLink getPartUsageLinkFromPublishBaseline(PartMaster parentPartMaster,PartMaster childPartMaster,ATPublishBaseline atPublishBaseline);
	/**
	 * 生成冲突
	 * @param atPublishPackage
	 */
	public void createViewChangeConflicts(ATPublishPackage atPublishPackage);
	/**
	 * 获得所有更改点
	 * @param atPublishPackage
	 * @return
	 */
	public List<ViewChangePoint> getViewChangePoints(ATPublishPackage atPublishPackage);
}
