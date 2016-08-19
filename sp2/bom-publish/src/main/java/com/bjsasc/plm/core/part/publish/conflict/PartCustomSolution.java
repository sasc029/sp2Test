package com.bjsasc.plm.core.part.publish.conflict;

import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.publish.model.PartAddChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangeConflict;
import com.bjsasc.plm.core.part.publish.util.PartChangeUtils;

public class PartCustomSolution extends AbstractPartChangeSolution {
	
	public PartCustomSolution(Object toModify) {
		super(toModify);
	}

	@Override
	public void solve(PartAddChangeConflict conflict) {
		
	}
	
	@Override
	public void solve(PartAttrChangeConflict conflict) {
		PartAttrChangePoint attrChangePoint = conflict.getViewChangePoint();
		// 如果为主对象属性更改
		if (attrChangePoint.getType().equals(PartAttrChangePoint.MASTER_ATTR_CHANGE)) {
			PartMaster master = (PartMaster) getToModify();
			attrChangePoint.setFinalContent(conflict.getUserContent());
			PartChangeUtils.changeMasterAttr(master, attrChangePoint);
		}
	}

	@Override
	public void solve(PartLinkChangeConflict conflict) {
		
	}

}
