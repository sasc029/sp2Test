package com.bjsasc.plm.core.part.publish.conflict;

import com.bjsasc.plm.core.part.publish.model.PartAddChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangeConflict;

/**
 * 保留解决方案
 * @author caorang
 * 
 */
public class PartReserveSolution extends AbstractPartChangeSolution {

	public PartReserveSolution(Object toModify) {
		super(toModify);
	}

	@Override
	public void solve(PartAddChangeConflict conflict) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void solve(PartAttrChangeConflict conflict) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void solve(PartLinkChangeConflict conflict) {
		// TODO Auto-generated method stub
		
	}

}
