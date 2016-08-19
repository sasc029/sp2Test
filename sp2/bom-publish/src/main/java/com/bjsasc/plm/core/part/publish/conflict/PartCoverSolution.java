package com.bjsasc.plm.core.part.publish.conflict;

import java.util.ArrayList;
import java.util.List;

import com.bjsasc.plm.core.doc.Document;
import com.bjsasc.plm.core.doc.DocumentMaster;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartHelper;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.link.PartDecribeLink;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.part.publish.model.PartAddChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangePoint;
import com.bjsasc.plm.core.part.publish.util.PartChangeUtils;
import com.bjsasc.plm.core.persist.PersistHelper;

public class PartCoverSolution extends AbstractPartChangeSolution {

	public PartCoverSolution(Object toModify) {
		super(toModify);
	}

	@Override
	public void solve(PartAddChangeConflict conflict) {
		
	}
	
	@Override
	public void solve(PartAttrChangeConflict conflict) {
		PartAttrChangePoint attrChangePoint = conflict.getViewChangePoint();
		// 如果为使用关系属性更改
		if (attrChangePoint.getType().equals(PartAttrChangePoint.USAGELINK_ATTR_CHANGE)) {
			PartUsageLink link = (PartUsageLink) getToModify();
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changeUsageLinkAttr(link, attrChangePoint);
		} else if (attrChangePoint.getType().equals(PartAttrChangePoint.PART_ATTR_CHANGE)) {
			// 如果为部件本身属性更改
			Part part = (Part) getToModify();
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changePartAttr(part, attrChangePoint);
		} else if (attrChangePoint.getType().equals(PartAttrChangePoint.MASTER_ATTR_CHANGE)) {
			// 如果为部件主对象属性更改
			PartMaster partMaster = (PartMaster) getToModify();
			attrChangePoint.setFinalContent(attrChangePoint.getAfterContent());
			PartChangeUtils.changeMasterAttr(partMaster, attrChangePoint);
		}
	}

	@Override
	public void solve(PartLinkChangeConflict conflict) {
		PartLinkChangePoint attrChangePoint = conflict.getViewChangePoint();
		Part toModify = (Part) getToModify();
		if (attrChangePoint.getType().equals(PartLinkChangePoint.PART_DESCRIBE_CHANGE)) {
			PartDecribeLink describeLink = (PartDecribeLink) attrChangePoint.getCreatedLink();
			Document doc = (Document) describeLink.getDescribesObject();
			// 获取已经存在的说明关系
			List<PartDecribeLink> list = PartHelper.getService()
					.getPartDecribeLinkByFrom(toModify);
			// 把其他相同主对象的文档的说明关系删掉
			List<PartDecribeLink> toDelete = filterPartDecribeLinkInMaster(
					list, (DocumentMaster) doc.getMaster());
			PersistHelper.getService().delete(toDelete);
			// 建立新的说明关系
			PartDecribeLink newDecribeLink = PartHelper.getService()
					.newPartDecribeLink(toModify, doc);
			PersistHelper.getService().save(newDecribeLink);
		}
	}

	private List<PartDecribeLink> filterPartDecribeLinkInMaster(
			List<PartDecribeLink> links, DocumentMaster master) {
		List<PartDecribeLink> result = new ArrayList<PartDecribeLink>();
		for (PartDecribeLink link : links) {
			Document doc = (Document) link.getDescribesObject();
			if (((DocumentMaster) doc.getMaster()).getInnerId().equals(
					master.getInnerId())) {
				result.add(link);
			}
		}
		return result;
	}

}
