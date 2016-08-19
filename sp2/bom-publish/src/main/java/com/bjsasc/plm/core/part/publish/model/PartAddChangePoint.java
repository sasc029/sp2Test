package com.bjsasc.plm.core.part.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangePoint;

/**
 * 部件新增更改点
 * @author zhuhongtao
 *
 */
public class PartAddChangePoint extends AbstractViewChangePoint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5028134762622082520L;
	public static final String CLASSID = PartAddChangePoint.class.getSimpleName();
	
	/**
	 * 新增部件
	 */
	private ObjectReference createdPartRef;

	public PartAddChangePoint() {
		setClassId(CLASSID);
	}
	
	public ObjectReference getCreatedPartRef() {
		return createdPartRef;
	}

	public void setCreatedPartRef(ObjectReference createdPartRef) {
		this.createdPartRef = createdPartRef;
	}
	
	public Part getCreatedPart(){
		if(createdPartRef==null){
			return null;
		}
		return (Part)createdPartRef.getObject();
	}
	
	public void setCreatedPart(Part createdPart){
		this.createdPartRef = ObjectReference.newObjectReference(createdPart);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PartAddChangePoint) {
			PartAddChangePoint compartObj = (PartAddChangePoint) obj;
			if (this.getInnerId() != null && compartObj.getInnerId() != null
					&& this.getInnerId().equals(compartObj.getInnerId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getChangeTip() {
		return "部件新增";
	}
	
	public Persistable getTarget(){
		return getCreatedPart();
	}

}
