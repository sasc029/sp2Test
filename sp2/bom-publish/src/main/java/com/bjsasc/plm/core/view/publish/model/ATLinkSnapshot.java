package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.baseline.model.Baselined;
import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.type.ATObject;

/**
 * 关系快照
 * @author caorang
 * @since 2014-6-6
 * @version 1.0.0.0
 */
public class ATLinkSnapshot extends ATObject implements Baselined {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8339798031964164336L;
	public static final String CLASSID = ATLinkSnapshot.class.getSimpleName();
	
	private ObjectReference linkRef;
	private String masterIdBind;//前端和后端的MasterInnerId的合并。
	
	public ATLinkSnapshot(){
		setClassId(ATLinkSnapshot.CLASSID);
	}

	public ObjectReference getLinkRef() {
		return linkRef;
	}

	public void setLinkRef(ObjectReference linkRef) {
		this.linkRef = linkRef;
	}

	public ATLink getLink() {
		return (ATLink) linkRef.getObject();
	}
	
	public void setLink(ATLink link) {
		this.linkRef= ObjectReference.newObjectReference(link);
	}

	public String getMasterIdBind() {
		return masterIdBind;
	}

	public void setMasterIdBind(String masterIdBind) {
		this.masterIdBind = masterIdBind;
	}

	public String getLinkClassId() {
		return linkRef.getClassId();
	}

	

	
	
}
