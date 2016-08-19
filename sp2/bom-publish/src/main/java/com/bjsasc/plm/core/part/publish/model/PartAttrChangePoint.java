package com.bjsasc.plm.core.part.publish.model;

import java.io.Serializable;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangePoint;

/**
 * 部件属性更改点
 * 
 * @author zhuhongtao
 * 
 */
public class PartAttrChangePoint extends AbstractViewChangePoint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5975550870398264702L;
	public static final String CLASSID = PartAttrChangePoint.class
			.getSimpleName();

	/**
	 * 属性更改类别常量
	 */
	public static final String MASTER_ATTR_CHANGE = "masterattrchange";
	public static final String PART_ATTR_CHANGE = "partattrchange";
	public static final String USAGELINK_ATTR_CHANGE = "usagelinkattrchange";
	/**
	 * 更改点类型
	 */
	private String type;
	/**
	 * 属性名
	 */
	private String attrId;
	/**
	 * 改前值
	 */
	private Serializable beforeContent;
	/**
	 * 改后值
	 */
	private Serializable afterContent;
	/**
	 * 最终值
	 */
	private Serializable finalContent;
	/**
	 * 影响关系(使用关系属性)
	 */
	private ObjectReference affectedLinkRef;
	/**
	 * 使用部件(使用关系属性)
	 */
	private ObjectReference usesPartRef;

	public PartAttrChangePoint() {
		setClassId(CLASSID);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Serializable getBeforeContent() {
		return beforeContent;
	}

	public void setBeforeContent(Serializable beforeContent) {
		this.beforeContent = beforeContent;
	}

	public Serializable getAfterContent() {
		return afterContent;
	}

	public void setAfterContent(Serializable afterContent) {
		this.afterContent = afterContent;
	}
	
	public Serializable getFinalContent() {
		return finalContent;
	}

	public void setFinalContent(Serializable finalContent) {
		this.finalContent = finalContent;
	}

	public ObjectReference getAffectedLinkRef() {
		return affectedLinkRef;
	}

	public void setAffectedLinkRef(ObjectReference affectedLinkRef) {
		this.affectedLinkRef = affectedLinkRef;
	}

	public PartUsageLink getAffectedLink() {
		if(affectedLinkRef==null){
			return null;
		}
		return (PartUsageLink) affectedLinkRef.getObject();
	}

	public void setAffectedLink(PartUsageLink affectedLink) {
		this.affectedLinkRef = ObjectReference.newObjectReference(affectedLink);
	}

	public String getAttrId() {
		return attrId;
	}

	public void setAttrId(String attrId) {
		this.attrId = attrId;
	}

	public ObjectReference getUsesPartRef() {
		return usesPartRef;
	}

	public void setUsesPartRef(ObjectReference usesPartRef) {
		this.usesPartRef = usesPartRef;
	}

	public Part getUsesPart() {
		if(usesPartRef==null){
			return null;
		}
		return (Part) usesPartRef.getObject();
	}

	public void setUsesPart(Part usesPart) {
		this.usesPartRef = ObjectReference.newObjectReference(usesPart);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PartAttrChangePoint) {
			PartAttrChangePoint compartObj = (PartAttrChangePoint) obj;
			if (this.getInnerId() != null && compartObj.getInnerId() != null
					&& this.getInnerId().equals(compartObj.getInnerId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getChangeTip() {
		if (getType().equals(MASTER_ATTR_CHANGE)) {
			return "主对象属性更改";
		} else if (getType().equals(PART_ATTR_CHANGE)) {
			return "本身属性更改";
		} else if (getType().equals(USAGELINK_ATTR_CHANGE)) {
			return "使用关系属性更改";
		}
		return "";
	}
	public Persistable getTarget(){
		if(getType().equals(USAGELINK_ATTR_CHANGE)) {
			return getAffectedLink().getTo();
		}else{
			return getViewChangeItem();
		}
	}
	
}
