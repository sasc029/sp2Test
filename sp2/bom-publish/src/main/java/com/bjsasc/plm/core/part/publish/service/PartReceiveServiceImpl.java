package com.bjsasc.plm.core.part.publish.service;

import java.util.*;

import com.bjsasc.plm.core.Helper;
import com.bjsasc.plm.core.baseline.model.Baselined;
import com.bjsasc.plm.core.doc.Document;
import com.bjsasc.plm.core.doc.DocumentMaster;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartHelper;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.PartService;
import com.bjsasc.plm.core.part.link.PartDecribeLink;
import com.bjsasc.plm.core.part.link.PartReferenceLink;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.part.publish.PartPublishHelper;
import com.bjsasc.plm.core.part.publish.PartPublishService;
import com.bjsasc.plm.core.part.publish.PartReceiveService;
import com.bjsasc.plm.core.part.publish.conflict.PartChangeSolution;
import com.bjsasc.plm.core.part.publish.model.PartAddChangeItem;
import com.bjsasc.plm.core.part.publish.model.PartAddChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartModifyChangeItem;
import com.bjsasc.plm.core.part.publish.util.ConflictUtils;
import com.bjsasc.plm.core.part.publish.util.PartChangeUtils;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.PersistUtil;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.vc.struct.StructHelper;
import com.bjsasc.plm.core.vc.struct.StructService;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewHelper;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.equivalent.ViewEquivalentService;
import com.bjsasc.plm.core.view.publish.ViewChangeItem;
import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.ViewConflictHelper;
import com.bjsasc.plm.core.view.publish.ViewConflictService;
import com.bjsasc.plm.core.view.publish.model.ATLinkSnapshot;
import com.bjsasc.plm.core.view.publish.model.ATPublishBaseline;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;
import com.bjsasc.plm.core.view.publish.model.ATSummaryBaseline;
import com.bjsasc.plm.core.view.publish.model.ViewChangeType;
import com.bjsasc.plm.core.view.publish.service.ViewReceiveServiceImpl;
import com.bjsasc.plm.core.view.publish.util.ViewUtils;

/**
 * 部件视图转换接收服务实现类
 * 
 * @author caorang
 */
public class PartReceiveServiceImpl extends ViewReceiveServiceImpl implements PartReceiveService {

	/**
	 * 依赖的服务类
	 */
	private PartService partService = PartHelper.getService();
	private PartPublishService partPublishService = PartPublishHelper
			.getPublishService();
	private ViewEquivalentService viewEquivalentService = ViewHelper
			.getEquivalentService();
	private StructService structService = StructHelper.getService();
	private ViewConflictService conflictService = ViewConflictHelper
			.getService();

	/**
	 * 执行更改点新增下游部件
	 */
	@Override
	protected void applyAdd(View targetView, ViewChangeItem item,
			Collection<ViewChangePoint> selected) {
		// 获得所有部件属性更改点
		List<PartAddChangePoint> addChangePoints = partPublishService.getViewChangePoints(item, PartAddChangePoint.class);
		for (PartAddChangePoint addChangePoint : addChangePoints) {
			if (selected.contains(addChangePoint)) {
				applyAddChange(targetView, addChangePoint);
			}
		}
	}
	
	private void applyAddChange(View targetView, PartAddChangePoint addChangePoint) {
		// 获取需要新增的视图受管理对象
		Part toAddPart = addChangePoint.getCreatedPart();
		List<ViewManageable> downStreams = ViewUtils
				.filterViewManageablesInView(ViewHelper.getEquivalentService()
						.getDownstreamObjects(toAddPart), targetView);
		// 如果目标视图内不存在该部件
		if (downStreams == null || downStreams.size() == 0) {
			// 将该对象添加至对应视图
			ViewManageable newViewManageable = Helper.getViewService()
					.newBranchForView(addChangePoint.getCreatedPart(),
							targetView);
			addMemberToPackage(newViewManageable, addChangePoint.getPublishPackage(), ViewChangeType.ViewChangeType_Add);
			updatePointResult(addChangePoint, "成功");
		} else {
			updatePointResult(addChangePoint, "已存在下游部件");
		}
	}

	/**
	 * 执行更改点更改下游部件
	 */
	@Override
	protected void applyModify(ViewManageable downStream, ViewChangeItem item,
			Collection<ViewChangePoint> selected) {
		// 获得所有部件属性更改点
		List<PartAttrChangePoint> attrChangePoints = partPublishService
				.getViewChangePoints(item, PartAttrChangePoint.class);
		for (PartAttrChangePoint attrChangePoint : attrChangePoints) {
			if (selected.contains(attrChangePoint)) {
				applyAttrChange((Part) downStream, attrChangePoint);
			}
		}
		// 获得所有部件属性更改点
		List<PartLinkChangePoint> linkChangePoints = partPublishService
				.getViewChangePoints(item, PartLinkChangePoint.class);
		for (PartLinkChangePoint linkChangePoint : linkChangePoints) {
			if (selected.contains(linkChangePoint)) {
				applyLinkChange((Part) downStream, linkChangePoint);
			}
		}
	}

	/**
	 * 执行部件属性更改点
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private void applyAttrChange(Part downStream,
			PartAttrChangePoint attrChangePoint) {
		String type = attrChangePoint.getType();
		if (type.equals(PartAttrChangePoint.MASTER_ATTR_CHANGE)) { // 主对象属性更改点
			applyMasterAttrChange(downStream, attrChangePoint);
		} else if (type.equals(PartAttrChangePoint.PART_ATTR_CHANGE)) { // 本身属性更改点
			applyPartAttrChange(downStream, attrChangePoint);
		} else if (type.equals(PartAttrChangePoint.USAGELINK_ATTR_CHANGE)) { // 使用关系属性更改点
			applyPartUsageAttrChange(downStream, attrChangePoint);
		}
	}

	/**
	 * 应用主对象属性更改点
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private void applyMasterAttrChange(Part downStream,
			PartAttrChangePoint attrChangePoint) {
		PartMaster master = downStream.getPartMaster();
		// 获取冲突
		PartAttrChangeConflict conflict = conflictService
				.getConflictByDownstream(attrChangePoint, downStream,
						PartAttrChangeConflict.class);
		if (conflict != null) { // 存在冲突
			// 获取解决方案类
			PartChangeSolution solution = ConflictUtils.buildSolution(conflict
					.getSolution(), master);
			// 执行解决方案
			solution.solve(conflict);
		} else { // 不存在冲突
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changeMasterAttr(master, attrChangePoint);
		}
		updatePointResult(attrChangePoint, "成功");
	}

	/**
	 * 应用本身属性更改点
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private void applyPartAttrChange(Part downStream,
			PartAttrChangePoint attrChangePoint) {
		// 获取冲突
		PartAttrChangeConflict conflict = conflictService
				.getConflictByDownstream(attrChangePoint, downStream,
						PartAttrChangeConflict.class);
		if (conflict != null) { // 存在冲突
			PartChangeSolution solution = ConflictUtils.buildSolution(conflict
					.getSolution(), downStream);
			// 执行解决方案
			solution.solve(conflict);
		} else { // 不存在冲突
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changePartAttr(downStream, attrChangePoint);
		}
		updatePointResult(attrChangePoint, "成功");
	}

	/**
	 * 应用使用关系属性更改点
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private void applyPartUsageAttrChange(Part downStream,
			PartAttrChangePoint attrChangePoint) {
		Part upStreamUsesPart = attrChangePoint.getUsesPart();
		View targetView = attrChangePoint.getTargetView();
		// 获得在目标视图内的下游最新部件
		List<ViewManageable> result = ViewUtils.filterViewManageablesInView(
				viewEquivalentService.getDownstreamObjects(upStreamUsesPart),
				targetView);
		List<ViewManageable> downStreamUsesParts = structService
				.findLastestIteration(result);
		if (downStreamUsesParts == null || downStreamUsesParts.size()==0) {
			updatePointResult(attrChangePoint, "未找到使用部件");
			return;
		}
		// 修改使用关系
		for (ViewManageable downStreamUsesPart : downStreamUsesParts) {
			Part uses = (Part) downStreamUsesPart;
			List<PartUsageLink> toModifyLinks = partService
					.getPartUsageLinksByFromAndTo(downStream,
							uses.getPartMaster());
			if (toModifyLinks == null || toModifyLinks.size()==0) {
				updatePointResult(attrChangePoint, "未找到使用关系");
				return;
			}
			// 获取冲突
			PartAttrChangeConflict conflict = conflictService
					.getConflictByDownstream(attrChangePoint, uses,
							PartAttrChangeConflict.class);
			if (conflict != null) {
				for (PartUsageLink link : toModifyLinks) {
					PartChangeSolution solution = ConflictUtils.buildSolution(conflict.getSolution(), link);
					solution.solve(conflict);
				}
			} else {
				for (PartUsageLink link : toModifyLinks) {
					attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
					PartChangeUtils.changeUsageLinkAttr(link, attrChangePoint);
				}
			}
		}
		updatePointResult(attrChangePoint, "成功");
	}

	/**
	 * 执行部件关系更改点
	 * 
	 * @param downStream
	 * @param linkChangePoint
	 */
	private void applyLinkChange(Part downStream,
			PartLinkChangePoint linkChangePoint) {
		String type = linkChangePoint.getType();
		if (type.equals(PartLinkChangePoint.PART_USAGE_CHANGE)) { // 使用关系更改点
			applyPartUsageChange(downStream, linkChangePoint);
		} else if (type.equals(PartLinkChangePoint.PART_DESCRIBE_CHANGE)) { // 说明关系更改点
			applyPartDescribeChange(downStream, linkChangePoint);
		} else if (type.equals(PartLinkChangePoint.PART_REFERENCE_CHANGE)) { // 参考关系更改点
			applyPartReferenceChange(downStream, linkChangePoint);
		}
	}

	/**
	 * 应用使用关系更改
	 * 
	 * @param downStream
	 *            被更改的下游部件
	 * @param linkChangePoint
	 *            更改点
	 */
	private void applyPartUsageChange(Part downStream,
			PartLinkChangePoint linkChangePoint) {
		// 如果需要新增的部件不为空，则为新增使用关系
		if (linkChangePoint.getCreatedToPartRef() != null) {
			Part createdPart = linkChangePoint.getCreatedToPart();
			View targetView = linkChangePoint.getTargetView();
			// 获得在目标视图内的下游最新部件
			List<ViewManageable> result = ViewUtils
					.filterViewManageablesInView(viewEquivalentService
							.getDownstreamObjects(createdPart), targetView);
			List<ViewManageable> downStreamUsesParts = structService
					.findLastestIteration(result);
			if (downStreamUsesParts == null || downStreamUsesParts.size()==0) {
				updatePointResult(linkChangePoint, "未找到使用部件");
				return;
			}
			// 建立使用关系
			for (ViewManageable usesvm : downStreamUsesParts) {
				Part uses = (Part) usesvm;
				PartChangeUtils.createUsageLink(downStream, uses.getPartMaster(), null);
			}
		}
		// 如果需要删除的部件不为空，则为删除使用关系
		if (linkChangePoint.getRemovedToPartRef() != null) {
			Part removedPart = linkChangePoint.getRemovedToPart();
			View targetView = linkChangePoint.getTargetView();
			// 获得在目标视图内的下游最新部件
			List<ViewManageable> result = ViewUtils
					.filterViewManageablesInView(viewEquivalentService
							.getDownstreamObjects(removedPart), targetView);
			List<ViewManageable> downStreamUsesParts = structService
					.findLastestIteration(result);
			if (downStreamUsesParts == null || downStreamUsesParts.size()==0) {
				updatePointResult(linkChangePoint, "未找到使用部件");
				return;
			}
			// 删除使用关系
			for (ViewManageable usesvm : downStreamUsesParts) {
				Part uses = (Part) usesvm;
				PartChangeUtils.deleteUsageLink(downStream, uses.getPartMaster(),null);
			}
		}
		updatePointResult(linkChangePoint, "成功");
	}

	/**
	 * 应用说明关系更改
	 * 
	 * @param downStream
	 *            被更改的下游部件
	 * @param linkChangePoint
	 *            更改点
	 */
	private void applyPartDescribeChange(Part downStream,
			PartLinkChangePoint linkChangePoint) {
		// 如果为新增说明关系
		if (linkChangePoint.getCreatedLinkRef() != null) {
			// 查找该下游部件的冲突项
			PartLinkChangeConflict conflict = conflictService
					.getConflictByDownstream(linkChangePoint, downStream,
							PartLinkChangeConflict.class);
			// 冲突存在
			if (conflict != null) {
				PartChangeSolution solution = ConflictUtils.buildSolution(conflict.getSolution(), downStream);
				solution.solve(conflict);
			} else { // 冲突不存在
				// 需要新增的说明文档
				Document decribeDoc = (Document) ((PartDecribeLink) linkChangePoint
						.getCreatedLink()).getDescribesObject();
				PartChangeUtils.createDescribeLink(downStream, decribeDoc);
			}
		}
		// 如果为删除说明关系
		if (linkChangePoint.getRemovedLinkRef() != null) {
			// 需要删除的说明文档
			Document decribeDoc = (Document) ((PartDecribeLink) linkChangePoint
					.getRemovedLink()).getDescribesObject();
			PartChangeUtils.deleteDescribeLink(downStream, decribeDoc);
		}
		updatePointResult(linkChangePoint, "成功");
	}

	/**
	 * 应用参考关系更改
	 * 
	 * @param downStream
	 *            被更改的下游部件
	 * @param linkChangePoint
	 *            更改点
	 */
	private void applyPartReferenceChange(Part downStream,
			PartLinkChangePoint linkChangePoint) {
		// 如果为新增参考关系
		if (linkChangePoint.getCreatedLinkRef() != null) {
			// 需要新增的参考文档主对象
			DocumentMaster referenceDocMaster = (DocumentMaster) ((PartReferenceLink) linkChangePoint
					.getCreatedLink()).getReferencesObject();
			PartChangeUtils.createReferenceLink(downStream, referenceDocMaster);
		}
		// 如果为删除参考关系
		if (linkChangePoint.getRemovedLinkRef() != null) {
			DocumentMaster referenceDocMaster = (DocumentMaster) ((PartReferenceLink) linkChangePoint
					.getRemovedLink()).getReferencesObject();
			PartChangeUtils.deleteReferenceLink(downStream, referenceDocMaster);
		}
		updatePointResult(linkChangePoint, "成功");
	}

	// 在集合中是否存在此元素（快照部件或快照link） 返回存在的元素
	@Override
	protected Baselined getExistBaselined(Baselined one, List<Baselined> list) {
		if (null == list || list.size() == 0) {
			return null;
		}

		for (Baselined baselined : list) {
			if (one instanceof ATSnapshot && baselined instanceof ATSnapshot) {

				ATSnapshot snapshotOne = (ATSnapshot) one;
				String oneMasterId = snapshotOne.getViewManageable()
						.getMaster().getInnerId();

				ATSnapshot snapshotTwo = (ATSnapshot) baselined;
				String twoMasterId = snapshotTwo.getViewManageable()
						.getMaster().getInnerId();
				// 比较两个部件的master是否相同
				if (oneMasterId.equals(twoMasterId)) {
					return baselined;
				}

			} else if (one instanceof ATLinkSnapshot
					&& baselined instanceof ATLinkSnapshot) {
				ATLinkSnapshot linkOne = (ATLinkSnapshot) one;
				String oneMasterIdBind = linkOne.getMasterIdBind();
				String oneLinkClassId = linkOne.getLinkClassId();

				ATLinkSnapshot linkTwo = (ATLinkSnapshot) baselined;
				String twoMasterIdBind = linkTwo.getMasterIdBind();
				String twoLinkClassId = linkTwo.getLinkClassId();
				// 链接类型和前后元素master绑定组合 是否都相同
				if (oneLinkClassId.equals(twoLinkClassId)
						&& oneMasterIdBind.equals(twoMasterIdBind)) {
					return baselined;
				}

			}

		}

		return null;
	}

	// 获取比对集合 summaryList 汇总基线中元素集合 publishBaseline 发布基线中元素集合
	@Override
	protected List<Baselined> getCompareList(List<Baselined> summaryList,
			List<Baselined> publishList) {
		List<Baselined> results = new ArrayList<Baselined>();
		if (null == summaryList || null == publishList) {
			return results;
		} else {
			for (Baselined publish : publishList) {
				if (publish instanceof ATSnapshot) {
					// 分别将发布基线中的《快照部件》和用此部件去汇总基线中查找下面的《链接快照》放进比对列表
					ATSnapshot publishSnapshot = (ATSnapshot) publish;
					Part publishPart = (Part) publishSnapshot
							.getViewManageableRef().getObject();
					String publishMasterId = publishPart.getMaster()
							.getInnerId();

					for (Baselined summary : summaryList) {
						if (summary instanceof ATLinkSnapshot) {
							ATLinkSnapshot linkSnapshot = (ATLinkSnapshot) summary;
							ATLink link = (ATLink) linkSnapshot.getLinkRef()
									.getObject();
							Part linkFromPart = (Part) link.getFrom();
							String linkFromMasterId = linkFromPart.getMaster()
									.getInnerId();
							// 与Link来源端（part）比较是否是一个主对象 链接快照放入比对列表
							if (publishMasterId.equals(linkFromMasterId)) {
								results.add(summary);
							}

						} else if (summary instanceof ATSnapshot) {
							ATSnapshot summaryATS = (ATSnapshot) summary;
							Part summaryPart = (Part) summaryATS
									.getViewManageableRef().getObject();
							String summaryMasterId = summaryPart.getMaster()
									.getInnerId();
							// 是否是一个主对象的part
							if (publishMasterId.equals(summaryMasterId)) {
								results.add(summary);
							}
						}

					}

				}

			}
			return results;
		}
	}

	@Override
	protected void clearChangeResult(ATPublishPackage publishPackage) {
		List<ViewChangeItem> changeItems = getAllViewChangeItems(publishPackage);
		for (ViewChangeItem changeItem : changeItems) {
			PersistUtil.getService().update(changeItem);
			List<ViewChangePoint> changePoints = getAllChangePoints(changeItem);
			for (ViewChangePoint changePoint : changePoints) {
				changePoint.setResult("");
				PersistUtil.getService().update(changePoint);
			}
		}
	}

	private List<ViewChangeItem> getAllViewChangeItems(ATPublishPackage publishPackage) {
		List<ViewChangeItem> result = new ArrayList<ViewChangeItem>();
		List<PartAddChangeItem> addChangeItems = partPublishService.getViewChangeItems(publishPackage, PartAddChangeItem.class);
		List<PartModifyChangeItem> modifyChangeItems = partPublishService.getViewChangeItems(publishPackage, PartModifyChangeItem.class);
		result.addAll(addChangeItems);
		result.addAll(modifyChangeItems);
		return result;
	}

	private List<ViewChangePoint> getAllChangePoints(ViewChangeItem item) {
		List<ViewChangePoint> result = new ArrayList<ViewChangePoint>();
		List<PartAddChangePoint> addChangePoints = partPublishService.getViewChangePoints(item, PartAddChangePoint.class);
		List<PartAttrChangePoint> attrChangePoints = partPublishService.getViewChangePoints(item, PartAttrChangePoint.class);
		List<PartLinkChangePoint> linkChangePoints = partPublishService.getViewChangePoints(item, PartLinkChangePoint.class);
		result.addAll(addChangePoints);
		result.addAll(attrChangePoints);
		result.addAll(linkChangePoints);
		return result;
	}
	
	/* 
	 * 更新临时汇总基线
	 */
	public void updateSummaryBaseline(ATPublishPackage publishPackage) {
		ATPublishBaseline publishBaseline=publishPackage.getPublishBaseline();
		ATSummaryBaseline summaryBaseline =publishPackage.getSummaryBaseline();
		
		
		StringBuilder hql = null;
		List<Object> paramList = null;
		
		//移除之前版本的对象及相应的使用关系、参考关系、描述关系
		
		//移除版本对象
		hql = new StringBuilder();
		paramList = new ArrayList<Object>();
		
		hql.append(" delete BaselineMemberLink t");
		hql.append(" where t.innerId in (");
		hql.append(" 	select memberlink.innerId from BaselineMemberLink memberlink,ATSnapshot snapshot,Part part,Part part1,ATSnapshot snapshot1,BaselineMemberLink memberlink1");
		hql.append("	where memberlink.fromObjectRef.innerId = ?");
		paramList.add(summaryBaseline.getInnerId());
		hql.append("	and memberlink.toObjectRef.innerId = snapshot.innerId");
		hql.append("	and snapshot.viewManageableRef.innerId = part.innerId");
		hql.append("	and part.masterRef.innerId = part1.masterRef.innerId");
		hql.append(" 	and part1.innerId = snapshot1.viewManageableRef.innerId");
		hql.append("	and snapshot1.innerId = memberlink1.toObjectRef.innerId");
		hql.append("	and memberlink1.fromObjectRef.innerId = ?");
		paramList.add(publishBaseline.getInnerId());
		hql.append(" )");
		
		Helper.getPersistService().bulkUpdate(hql.toString(), paramList.toArray());
		
		//将发布基线的内容添加至汇总基线
		
		StringBuilder sql = new StringBuilder();
		paramList = new ArrayList<Object>();
		sql.append(" insert into plm_baseline_memberLink(INNERID,CLASSID,UPDATETIME,UPDATECOUNT,FROMOBJECTID,FROMOBJECTCLASSID,TOOBJECTID,TOOBJECTCLASSID,ISMAIN,MASTERID,MASTERCLASSID,BRANCHID,BRANCHCLASSID)");
		sql.append(" select sys_guid(),l.CLASSID,l.UPDATETIME,l.UPDATECOUNT,'"+summaryBaseline.getInnerId()+"','ATSummaryBaseline',TOOBJECTID,TOOBJECTCLASSID,ISMAIN,MASTERID,MASTERCLASSID,BRANCHID,BRANCHCLASSID from plm_baseline_memberLink l");
		sql.append(" where l.FROMOBJECTID = ?");
		paramList.add(publishBaseline.getInnerId());
		
		Helper.getPersistService().bulkUpdateBySql(sql.toString(), paramList.toArray());
	}
	
}
