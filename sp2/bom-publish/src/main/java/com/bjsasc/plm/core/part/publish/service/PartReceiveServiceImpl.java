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
 * ������ͼת�����շ���ʵ����
 * 
 * @author caorang
 */
public class PartReceiveServiceImpl extends ViewReceiveServiceImpl implements PartReceiveService {

	/**
	 * �����ķ�����
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
	 * ִ�и��ĵ��������β���
	 */
	@Override
	protected void applyAdd(View targetView, ViewChangeItem item,
			Collection<ViewChangePoint> selected) {
		// ������в������Ը��ĵ�
		List<PartAddChangePoint> addChangePoints = partPublishService.getViewChangePoints(item, PartAddChangePoint.class);
		for (PartAddChangePoint addChangePoint : addChangePoints) {
			if (selected.contains(addChangePoint)) {
				applyAddChange(targetView, addChangePoint);
			}
		}
	}
	
	private void applyAddChange(View targetView, PartAddChangePoint addChangePoint) {
		// ��ȡ��Ҫ��������ͼ�ܹ������
		Part toAddPart = addChangePoint.getCreatedPart();
		List<ViewManageable> downStreams = ViewUtils
				.filterViewManageablesInView(ViewHelper.getEquivalentService()
						.getDownstreamObjects(toAddPart), targetView);
		// ���Ŀ����ͼ�ڲ����ڸò���
		if (downStreams == null || downStreams.size() == 0) {
			// ���ö����������Ӧ��ͼ
			ViewManageable newViewManageable = Helper.getViewService()
					.newBranchForView(addChangePoint.getCreatedPart(),
							targetView);
			addMemberToPackage(newViewManageable, addChangePoint.getPublishPackage(), ViewChangeType.ViewChangeType_Add);
			updatePointResult(addChangePoint, "�ɹ�");
		} else {
			updatePointResult(addChangePoint, "�Ѵ������β���");
		}
	}

	/**
	 * ִ�и��ĵ�������β���
	 */
	@Override
	protected void applyModify(ViewManageable downStream, ViewChangeItem item,
			Collection<ViewChangePoint> selected) {
		// ������в������Ը��ĵ�
		List<PartAttrChangePoint> attrChangePoints = partPublishService
				.getViewChangePoints(item, PartAttrChangePoint.class);
		for (PartAttrChangePoint attrChangePoint : attrChangePoints) {
			if (selected.contains(attrChangePoint)) {
				applyAttrChange((Part) downStream, attrChangePoint);
			}
		}
		// ������в������Ը��ĵ�
		List<PartLinkChangePoint> linkChangePoints = partPublishService
				.getViewChangePoints(item, PartLinkChangePoint.class);
		for (PartLinkChangePoint linkChangePoint : linkChangePoints) {
			if (selected.contains(linkChangePoint)) {
				applyLinkChange((Part) downStream, linkChangePoint);
			}
		}
	}

	/**
	 * ִ�в������Ը��ĵ�
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private void applyAttrChange(Part downStream,
			PartAttrChangePoint attrChangePoint) {
		String type = attrChangePoint.getType();
		if (type.equals(PartAttrChangePoint.MASTER_ATTR_CHANGE)) { // ���������Ը��ĵ�
			applyMasterAttrChange(downStream, attrChangePoint);
		} else if (type.equals(PartAttrChangePoint.PART_ATTR_CHANGE)) { // �������Ը��ĵ�
			applyPartAttrChange(downStream, attrChangePoint);
		} else if (type.equals(PartAttrChangePoint.USAGELINK_ATTR_CHANGE)) { // ʹ�ù�ϵ���Ը��ĵ�
			applyPartUsageAttrChange(downStream, attrChangePoint);
		}
	}

	/**
	 * Ӧ�����������Ը��ĵ�
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private void applyMasterAttrChange(Part downStream,
			PartAttrChangePoint attrChangePoint) {
		PartMaster master = downStream.getPartMaster();
		// ��ȡ��ͻ
		PartAttrChangeConflict conflict = conflictService
				.getConflictByDownstream(attrChangePoint, downStream,
						PartAttrChangeConflict.class);
		if (conflict != null) { // ���ڳ�ͻ
			// ��ȡ���������
			PartChangeSolution solution = ConflictUtils.buildSolution(conflict
					.getSolution(), master);
			// ִ�н������
			solution.solve(conflict);
		} else { // �����ڳ�ͻ
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changeMasterAttr(master, attrChangePoint);
		}
		updatePointResult(attrChangePoint, "�ɹ�");
	}

	/**
	 * Ӧ�ñ������Ը��ĵ�
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private void applyPartAttrChange(Part downStream,
			PartAttrChangePoint attrChangePoint) {
		// ��ȡ��ͻ
		PartAttrChangeConflict conflict = conflictService
				.getConflictByDownstream(attrChangePoint, downStream,
						PartAttrChangeConflict.class);
		if (conflict != null) { // ���ڳ�ͻ
			PartChangeSolution solution = ConflictUtils.buildSolution(conflict
					.getSolution(), downStream);
			// ִ�н������
			solution.solve(conflict);
		} else { // �����ڳ�ͻ
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changePartAttr(downStream, attrChangePoint);
		}
		updatePointResult(attrChangePoint, "�ɹ�");
	}

	/**
	 * Ӧ��ʹ�ù�ϵ���Ը��ĵ�
	 * 
	 * @param downStream
	 * @param attrChangePoint
	 */
	private void applyPartUsageAttrChange(Part downStream,
			PartAttrChangePoint attrChangePoint) {
		Part upStreamUsesPart = attrChangePoint.getUsesPart();
		View targetView = attrChangePoint.getTargetView();
		// �����Ŀ����ͼ�ڵ��������²���
		List<ViewManageable> result = ViewUtils.filterViewManageablesInView(
				viewEquivalentService.getDownstreamObjects(upStreamUsesPart),
				targetView);
		List<ViewManageable> downStreamUsesParts = structService
				.findLastestIteration(result);
		if (downStreamUsesParts == null || downStreamUsesParts.size()==0) {
			updatePointResult(attrChangePoint, "δ�ҵ�ʹ�ò���");
			return;
		}
		// �޸�ʹ�ù�ϵ
		for (ViewManageable downStreamUsesPart : downStreamUsesParts) {
			Part uses = (Part) downStreamUsesPart;
			List<PartUsageLink> toModifyLinks = partService
					.getPartUsageLinksByFromAndTo(downStream,
							uses.getPartMaster());
			if (toModifyLinks == null || toModifyLinks.size()==0) {
				updatePointResult(attrChangePoint, "δ�ҵ�ʹ�ù�ϵ");
				return;
			}
			// ��ȡ��ͻ
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
		updatePointResult(attrChangePoint, "�ɹ�");
	}

	/**
	 * ִ�в�����ϵ���ĵ�
	 * 
	 * @param downStream
	 * @param linkChangePoint
	 */
	private void applyLinkChange(Part downStream,
			PartLinkChangePoint linkChangePoint) {
		String type = linkChangePoint.getType();
		if (type.equals(PartLinkChangePoint.PART_USAGE_CHANGE)) { // ʹ�ù�ϵ���ĵ�
			applyPartUsageChange(downStream, linkChangePoint);
		} else if (type.equals(PartLinkChangePoint.PART_DESCRIBE_CHANGE)) { // ˵����ϵ���ĵ�
			applyPartDescribeChange(downStream, linkChangePoint);
		} else if (type.equals(PartLinkChangePoint.PART_REFERENCE_CHANGE)) { // �ο���ϵ���ĵ�
			applyPartReferenceChange(downStream, linkChangePoint);
		}
	}

	/**
	 * Ӧ��ʹ�ù�ϵ����
	 * 
	 * @param downStream
	 *            �����ĵ����β���
	 * @param linkChangePoint
	 *            ���ĵ�
	 */
	private void applyPartUsageChange(Part downStream,
			PartLinkChangePoint linkChangePoint) {
		// �����Ҫ�����Ĳ�����Ϊ�գ���Ϊ����ʹ�ù�ϵ
		if (linkChangePoint.getCreatedToPartRef() != null) {
			Part createdPart = linkChangePoint.getCreatedToPart();
			View targetView = linkChangePoint.getTargetView();
			// �����Ŀ����ͼ�ڵ��������²���
			List<ViewManageable> result = ViewUtils
					.filterViewManageablesInView(viewEquivalentService
							.getDownstreamObjects(createdPart), targetView);
			List<ViewManageable> downStreamUsesParts = structService
					.findLastestIteration(result);
			if (downStreamUsesParts == null || downStreamUsesParts.size()==0) {
				updatePointResult(linkChangePoint, "δ�ҵ�ʹ�ò���");
				return;
			}
			// ����ʹ�ù�ϵ
			for (ViewManageable usesvm : downStreamUsesParts) {
				Part uses = (Part) usesvm;
				PartChangeUtils.createUsageLink(downStream, uses.getPartMaster(), null);
			}
		}
		// �����Ҫɾ���Ĳ�����Ϊ�գ���Ϊɾ��ʹ�ù�ϵ
		if (linkChangePoint.getRemovedToPartRef() != null) {
			Part removedPart = linkChangePoint.getRemovedToPart();
			View targetView = linkChangePoint.getTargetView();
			// �����Ŀ����ͼ�ڵ��������²���
			List<ViewManageable> result = ViewUtils
					.filterViewManageablesInView(viewEquivalentService
							.getDownstreamObjects(removedPart), targetView);
			List<ViewManageable> downStreamUsesParts = structService
					.findLastestIteration(result);
			if (downStreamUsesParts == null || downStreamUsesParts.size()==0) {
				updatePointResult(linkChangePoint, "δ�ҵ�ʹ�ò���");
				return;
			}
			// ɾ��ʹ�ù�ϵ
			for (ViewManageable usesvm : downStreamUsesParts) {
				Part uses = (Part) usesvm;
				PartChangeUtils.deleteUsageLink(downStream, uses.getPartMaster(),null);
			}
		}
		updatePointResult(linkChangePoint, "�ɹ�");
	}

	/**
	 * Ӧ��˵����ϵ����
	 * 
	 * @param downStream
	 *            �����ĵ����β���
	 * @param linkChangePoint
	 *            ���ĵ�
	 */
	private void applyPartDescribeChange(Part downStream,
			PartLinkChangePoint linkChangePoint) {
		// ���Ϊ����˵����ϵ
		if (linkChangePoint.getCreatedLinkRef() != null) {
			// ���Ҹ����β����ĳ�ͻ��
			PartLinkChangeConflict conflict = conflictService
					.getConflictByDownstream(linkChangePoint, downStream,
							PartLinkChangeConflict.class);
			// ��ͻ����
			if (conflict != null) {
				PartChangeSolution solution = ConflictUtils.buildSolution(conflict.getSolution(), downStream);
				solution.solve(conflict);
			} else { // ��ͻ������
				// ��Ҫ������˵���ĵ�
				Document decribeDoc = (Document) ((PartDecribeLink) linkChangePoint
						.getCreatedLink()).getDescribesObject();
				PartChangeUtils.createDescribeLink(downStream, decribeDoc);
			}
		}
		// ���Ϊɾ��˵����ϵ
		if (linkChangePoint.getRemovedLinkRef() != null) {
			// ��Ҫɾ����˵���ĵ�
			Document decribeDoc = (Document) ((PartDecribeLink) linkChangePoint
					.getRemovedLink()).getDescribesObject();
			PartChangeUtils.deleteDescribeLink(downStream, decribeDoc);
		}
		updatePointResult(linkChangePoint, "�ɹ�");
	}

	/**
	 * Ӧ�òο���ϵ����
	 * 
	 * @param downStream
	 *            �����ĵ����β���
	 * @param linkChangePoint
	 *            ���ĵ�
	 */
	private void applyPartReferenceChange(Part downStream,
			PartLinkChangePoint linkChangePoint) {
		// ���Ϊ�����ο���ϵ
		if (linkChangePoint.getCreatedLinkRef() != null) {
			// ��Ҫ�����Ĳο��ĵ�������
			DocumentMaster referenceDocMaster = (DocumentMaster) ((PartReferenceLink) linkChangePoint
					.getCreatedLink()).getReferencesObject();
			PartChangeUtils.createReferenceLink(downStream, referenceDocMaster);
		}
		// ���Ϊɾ���ο���ϵ
		if (linkChangePoint.getRemovedLinkRef() != null) {
			DocumentMaster referenceDocMaster = (DocumentMaster) ((PartReferenceLink) linkChangePoint
					.getRemovedLink()).getReferencesObject();
			PartChangeUtils.deleteReferenceLink(downStream, referenceDocMaster);
		}
		updatePointResult(linkChangePoint, "�ɹ�");
	}

	// �ڼ������Ƿ���ڴ�Ԫ�أ����ղ��������link�� ���ش��ڵ�Ԫ��
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
				// �Ƚ�����������master�Ƿ���ͬ
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
				// �������ͺ�ǰ��Ԫ��master����� �Ƿ���ͬ
				if (oneLinkClassId.equals(twoLinkClassId)
						&& oneMasterIdBind.equals(twoMasterIdBind)) {
					return baselined;
				}

			}

		}

		return null;
	}

	// ��ȡ�ȶԼ��� summaryList ���ܻ�����Ԫ�ؼ��� publishBaseline ����������Ԫ�ؼ���
	@Override
	protected List<Baselined> getCompareList(List<Baselined> summaryList,
			List<Baselined> publishList) {
		List<Baselined> results = new ArrayList<Baselined>();
		if (null == summaryList || null == publishList) {
			return results;
		} else {
			for (Baselined publish : publishList) {
				if (publish instanceof ATSnapshot) {
					// �ֱ𽫷��������еġ����ղ��������ô˲���ȥ���ܻ����в�������ġ����ӿ��ա��Ž��ȶ��б�
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
							// ��Link��Դ�ˣ�part���Ƚ��Ƿ���һ�������� ���ӿ��շ���ȶ��б�
							if (publishMasterId.equals(linkFromMasterId)) {
								results.add(summary);
							}

						} else if (summary instanceof ATSnapshot) {
							ATSnapshot summaryATS = (ATSnapshot) summary;
							Part summaryPart = (Part) summaryATS
									.getViewManageableRef().getObject();
							String summaryMasterId = summaryPart.getMaster()
									.getInnerId();
							// �Ƿ���һ���������part
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
	 * ������ʱ���ܻ���
	 */
	public void updateSummaryBaseline(ATPublishPackage publishPackage) {
		ATPublishBaseline publishBaseline=publishPackage.getPublishBaseline();
		ATSummaryBaseline summaryBaseline =publishPackage.getSummaryBaseline();
		
		
		StringBuilder hql = null;
		List<Object> paramList = null;
		
		//�Ƴ�֮ǰ�汾�Ķ�����Ӧ��ʹ�ù�ϵ���ο���ϵ��������ϵ
		
		//�Ƴ��汾����
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
		
		//���������ߵ�������������ܻ���
		
		StringBuilder sql = new StringBuilder();
		paramList = new ArrayList<Object>();
		sql.append(" insert into plm_baseline_memberLink(INNERID,CLASSID,UPDATETIME,UPDATECOUNT,FROMOBJECTID,FROMOBJECTCLASSID,TOOBJECTID,TOOBJECTCLASSID,ISMAIN,MASTERID,MASTERCLASSID,BRANCHID,BRANCHCLASSID)");
		sql.append(" select sys_guid(),l.CLASSID,l.UPDATETIME,l.UPDATECOUNT,'"+summaryBaseline.getInnerId()+"','ATSummaryBaseline',TOOBJECTID,TOOBJECTCLASSID,ISMAIN,MASTERID,MASTERCLASSID,BRANCHID,BRANCHCLASSID from plm_baseline_memberLink l");
		sql.append(" where l.FROMOBJECTID = ?");
		paramList.add(publishBaseline.getInnerId());
		
		Helper.getPersistService().bulkUpdateBySql(sql.toString(), paramList.toArray());
	}
	
}
