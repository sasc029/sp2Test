package com.bjsasc.plm.core.part.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangePoint;

/**
 * 部件链接更改点
 * @author zhuhongtao
 *
 */
public class PartLinkChangePoint extends AbstractViewChangePoint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4206360349373545988L;
	public static final String CLASSID = PartLinkChangePoint.class.getSimpleName();
	public static final String PART_USAGE_CHANGE = "PartUsageChange";
	public static final String PART_DESCRIBE_CHANGE = "PartDescribeChange";
	public static final String PART_REFERENCE_CHANGE = "PartReferenceChange";
	
	private ObjectReference createdLinkRef;
	
	private ObjectReference removedLinkRef;
	
	private ObjectReference createdToPartRef;
	
	private ObjectReference removedToPartRef;
	
	private String type;

	public PartLinkChangePoint() {
		setClassId(CLASSID);
	}
	
	public ObjectReference getCreatedLinkRef() {
		return createdLinkRef;
	}

	public void setCreatedLinkRef(ObjectReference createdLinkRef) {
		this.createdLinkRef = createdLinkRef;
	}

	public ObjectReference getRemovedLinkRef() {
		return removedLinkRef;
	}

	public void setRemovedLinkRef(ObjectReference removedLinkRef) {
		this.removedLinkRef = removedLinkRef;
	}
	
	public ATLink getCreatedLink(){
		if(createdLinkRef==null){
			return null;
		}
		return (ATLink)createdLinkRef.getObject();
	}
	
	public void setCreatedLink(ATLink createdLink){
		this.createdLinkRef = ObjectReference.newObjectReference(createdLink);
	}
	
	public ATLink getRemovedLink(){
		if(removedLinkRef==null){
			return null;
		}
		return (ATLink)removedLinkRef.getObject();
	}
	
	public void setRemovedLink(ATLink removedLink){
		this.removedLinkRef = ObjectReference.newObjectReference(removedLink);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ObjectReference getCreatedToPartRef() {
		return createdToPartRef;
	}

	public void setCreatedToPartRef(ObjectReference createdToPartRef) {
		this.createdToPartRef = createdToPartRef;
	}
	
	public Part getCreatedToPart(){
		if(createdToPartRef==null){
			return null;
		}
		return (Part)this.createdToPartRef.getObject();
	}
	
	public void setCreatedToPart(Part part){
		this.createdToPartRef = ObjectReference.newObjectReference(part);
	}

	public ObjectReference getRemovedToPartRef() {
		return removedToPartRef;
	}

	public void setRemovedToPartRef(ObjectReference removedToPartRef) {
		this.removedToPartRef = removedToPartRef;
	}
	
	public Part getRemovedToPart(){
		if(removedToPartRef==null){
			return null;
		}
		return (Part)this.removedToPartRef.getObject();
	}
	
	public void setRemovedToPart(Part part){
		this.removedToPartRef = ObjectReference.newObjectReference(part);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PartLinkChangePoint) {
			PartLinkChangePoint compartObj = (PartLinkChangePoint) obj;
			if (this.getInnerId() != null && compartObj.getInnerId() != null
					&& this.getInnerId().equals(compartObj.getInnerId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getChangeTip() {
		if (getType().equals(PART_USAGE_CHANGE)) {
			if (getCreatedLinkRef() != null) {
				return "部件使用关系新增";
			} else if (getRemovedToPartRef()!=null) {
				return "部件使用关系删除";
			}
		} else if (getType().equals(PART_REFERENCE_CHANGE)) {
			if (getCreatedLinkRef() != null) {
				return "部件参考关系新增";
			} else if (getRemovedLinkRef()!=null) {
				return "部件参考关系删除";
			}
		} else if (getType().equals(PART_DESCRIBE_CHANGE)) {
			if (getCreatedLinkRef() != null) {
				return "部件说明关系新增";
			} else if (getRemovedLinkRef()!=null) {
				return "部件说明关系删除";
			}
		}
		return "";
	}
	public Persistable getTarget(){
		if(getCreatedLinkRef()!=null){
			return getCreatedLink().getTo();
		}else if(getRemovedLinkRef()!=null){
			return getRemovedLink().getTo();
		}
		return null;
	}
	
}
