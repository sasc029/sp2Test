package com.bjsasc.plm.core.part.publish.model;

import com.bjsasc.plm.core.part.publish.conflict.PartChangeSolution;
import com.bjsasc.plm.core.view.publish.conflict.ChangeSolution;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangeConflict;

/**
 * 部件新增转换冲突
 * @author zhuhongtao
 *
 */
public class PartAddChangeConflict extends AbstractViewChangeConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8018749686041507770L;
	public static final String CLASSID = PartAddChangeConflict.class.getSimpleName();
	
	public PartAddChangeConflict() {
		setClassId(CLASSID);
	}

	@Override
	public void solveBy(ChangeSolution solution) {
		if (solution instanceof PartChangeSolution) {
			((PartChangeSolution) solution).solve(this);
		}
	}
	
}
