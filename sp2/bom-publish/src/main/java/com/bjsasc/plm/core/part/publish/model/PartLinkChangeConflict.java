package com.bjsasc.plm.core.part.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.part.publish.conflict.PartChangeSolution;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.view.publish.conflict.ChangeSolution;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangeConflict;

/**
 * 部件链接转换冲突
 * @author zhuhongtao
 *
 */
public class PartLinkChangeConflict extends AbstractViewChangeConflict {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2834719827939362438L;
	public static final String CLASSID = PartLinkChangeConflict.class.getSimpleName();
	
	/**
	 * 现有链接
	 */
	private ObjectReference currentLinkRef;

	public PartLinkChangeConflict() {
		setClassId(CLASSID);
	}
	
	public ObjectReference getCurrentLinkRef() {
		return currentLinkRef;
	}

	public void setCurrentLinkRef(ObjectReference currentLinkRef) {
		this.currentLinkRef = currentLinkRef;
	}
	
	public ATLink getCurrentLink(){
		if(currentLinkRef==null){
			return null;
		}
		return (ATLink)currentLinkRef.getObject();
	}

	public void setCurrentLink(ATLink currentLink){
		this.currentLinkRef = ObjectReference.newObjectReference(currentLink);
	}
	
	@Override
	public PartLinkChangePoint getViewChangePoint() {
		return (PartLinkChangePoint) super.getViewChangePoint();
	}

	@Override
	public void solveBy(ChangeSolution solution) {
		if (solution instanceof PartChangeSolution) {
			((PartChangeSolution) solution).solve(this);
		}
	}
	
}
