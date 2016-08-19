package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.system.task.ATTask;

/**
 * 发布任务
 * @author caorang
 * @since 2014-6-6
 * @version 1.0.0.0
 */
public class ATPublishTask extends ATTask {

	private static final long serialVersionUID = 4137950445695587871L;
	private static final String CLASSID = ATPublishTask.class.getSimpleName();
	
	private ObjectReference publishPackageRef;
	
	public ATPublishTask(){
		this.setClassId(CLASSID);		
	}

	public ObjectReference getPublishPackageRef() {
		return publishPackageRef;
	}

	public void setPublishPackageRef(ObjectReference publishPackageRef) {
		this.publishPackageRef = publishPackageRef;
	}
	
	public ATPublishPackage getPublishPackage() {
		return (ATPublishPackage) publishPackageRef.getObject();
	}

	public void setPublishPackage(ATPublishPackage publishPackage) {
		this.publishPackageRef = ObjectReference.newObjectReference(publishPackage);
	}
}
