package com.bjsasc.plm.core.part.publish.model;

import java.io.Serializable;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.publish.model.AbstractViewChangePoint;

/**
 * �������Ը��ĵ�
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
	 * ���Ը��������
	 */
	public static final String MASTER_ATTR_CHANGE = "masterattrchange";
	public static final String PART_ATTR_CHANGE = "partattrchange";
	public static final String USAGELINK_ATTR_CHANGE = "usagelinkattrchange";
	/**
	 * ���ĵ�����
	 */
	private String type;
	/**
	 * ������
	 */
	private String attrId;
	/**
	 * ��ǰֵ
	 */
	private Serializable beforeContent;
	/**
	 * �ĺ�ֵ
	 */
	private Serializable afterContent;
	/**
	 * ����ֵ
	 */
	private Serializable finalContent;
	/**
	 * Ӱ���ϵ(ʹ�ù�ϵ����)
	 */
	private ObjectReference affectedLinkRef;
	/**
	 * ʹ�ò���(ʹ�ù�ϵ����)
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
			return "���������Ը���";
		} else if (getType().equals(PART_ATTR_CHANGE)) {
			return "�������Ը���";
		} else if (getType().equals(USAGELINK_ATTR_CHANGE)) {
			return "ʹ�ù�ϵ���Ը���";
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
