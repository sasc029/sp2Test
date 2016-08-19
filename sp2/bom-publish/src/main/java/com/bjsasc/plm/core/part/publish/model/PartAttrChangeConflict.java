package com.bjsasc.plm.core.part.publish.model;

import java.io.Serializable;

import com.bjsasc.plm.core.part.publish.conflict.PartChangeSolution;
import com.bjsasc.plm.core.view.publish.conflict.ChangeSolution;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangeConflict;

/**
 * 部件属性转换冲突
 * @author zhuhongtao
 */
public class PartAttrChangeConflict extends AbstractViewChangeConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2152041805724054990L;
	public static final String CLASSID = PartAttrChangeConflict.class.getSimpleName();
	
	/**
	 * 现有值
	 */
	private Serializable currentContent;
	/**
	 * 用户定义值
	 */
	private String userContent;
	
	public PartAttrChangeConflict() {
		setClassId(CLASSID);
	}
	
	public Serializable getCurrentContent() {
		return currentContent;
	}
	
	public void setCurrentContent(Serializable currentContent) {
		this.currentContent = currentContent;
	}
	
	public String getUserContent() {
		return userContent;
	}
	
	public void setUserContent(String userContent) {
		this.userContent = userContent;
	}

	@Override
	public PartAttrChangePoint getViewChangePoint() {
		return (PartAttrChangePoint) super.getViewChangePoint();
	}

	@Override
	public void solveBy(ChangeSolution solution) {
		if (solution instanceof PartChangeSolution) {
			((PartChangeSolution) solution).solve(this);
		}
	}
	
}
