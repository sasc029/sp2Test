package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.lifeCycle.LifeCycleInfo;
import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.context.model.ContextInfo;
import com.bjsasc.plm.core.context.model.Contexted;
import com.bjsasc.plm.core.identifier.Identified;
import com.bjsasc.plm.core.lifecycle.LifeCycleManaged;
import com.bjsasc.plm.core.managed.model.ManageInfo;
import com.bjsasc.plm.core.managed.model.Manageable;
import com.bjsasc.plm.core.type.ATObject;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;

/**
 * 视图发布包，包含了视图发布的部件，发布的源视图，目标视图，发布基线等信息
 * @author caorang
 * @since 2014-6-16
 */
public class ATPublishPackage extends ATObject implements Contexted, Identified, Manageable, LifeCycleManaged {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3760703269191580807L;
	public static final String CLASSID = ATPublishPackage.class.getSimpleName();
	
	/**
     * 发布包名称
     */
	private String name;
	
	/**
     * 发布包标识
     */
	private String number;
	
	/**
	 * 发布部件
	 */
	private ObjectReference publishPartRef;
	
	/**
	 * 发布基线
	 */
	private ObjectReference publishBaselineRef;
	
	/**
	 * 汇总基线
	 */
	private ObjectReference summaryBaselineRef;
	
	/**
	 * 发布上游视图
	 */
	private ObjectReference sourceViewRef;
	
	/**
	 * 发布下游视图
	 */
	private ObjectReference targetViewRef;
	
	/**
	 * 上下文信息
	 */
	private ContextInfo contextInfo;
	
	/**
	 * 前驱发布包
	 */
	private ObjectReference predecessorRef;
	
	/**
	 * 管理信息
	 */
	private ManageInfo manageInfo;
	
	/**
	 * 生命周期信息
	 */
	private LifeCycleInfo lifeCycleInfo;
	
	public ATPublishPackage() {
		this.setClassId(CLASSID);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}

	public ObjectReference getPublishBaselineRef() {
		return publishBaselineRef;
	}

	public void setPublishBaselineRef(ObjectReference publishBaselineRef) {
		this.publishBaselineRef = publishBaselineRef;
	}
	
	public ATPublishBaseline getPublishBaseline() {
		return (ATPublishBaseline) publishBaselineRef.getObject();
	}
	
	public void setPublishBaseline(ATPublishBaseline publishBaseline) {
		this.publishBaselineRef = ObjectReference.newObjectReference(publishBaseline);
	}
	
	public ObjectReference getSummaryBaselineRef() {
		return summaryBaselineRef;
	}

	public void setSummaryBaselineRef(ObjectReference summaryBaselineRef) {
		this.summaryBaselineRef = summaryBaselineRef;
	}
	

	public ATSummaryBaseline getSummaryBaseline() {
		return (ATSummaryBaseline) summaryBaselineRef.getObject();
	}
	
	public void setSummaryBaseline(ATSummaryBaseline summaryBaseline) {
		this.summaryBaselineRef = ObjectReference.newObjectReference(summaryBaseline);
	}
	
	public ObjectReference getSourceViewRef() {
		return sourceViewRef;
	}

	public void setSourceViewRef(ObjectReference sourceViewRef) {
		this.sourceViewRef = sourceViewRef;
	}

	public View getSourceView() {
		return (View) sourceViewRef.getObject();
	}

	public void setSourceView(View sourceView) {
		this.sourceViewRef = ObjectReference.newObjectReference(sourceView);
	}
	
	public ObjectReference getTargetViewRef() {
		return targetViewRef;
	}

	public void setTargetViewRef(ObjectReference targetViewRef) {
		this.targetViewRef = targetViewRef;
	}

	public View getTargetView() {
		return (View) targetViewRef.getObject();
	}

	public void setTargetView(View targetView) {
		this.targetViewRef = ObjectReference.newObjectReference(targetView);
	}
	
	public ObjectReference getPublishPartRef() {
		return publishPartRef;
	}

	public void setPublishPartRef(ObjectReference publishPartRef) {
		this.publishPartRef = publishPartRef;
	}
	
	public ViewManageable getPublishPart() {
		return (ViewManageable) publishPartRef.getObject();
	}

	public void setPublishPart(ViewManageable part) {
		this.publishPartRef = ObjectReference.newObjectReference(part);
	}
	
	public void setContextInfo(ContextInfo contextInfo) {
		this.contextInfo = contextInfo;
	}

	public ContextInfo getContextInfo() {
		return this.contextInfo;
	}

	public ObjectReference getPredecessorRef() {
		return predecessorRef;
	}

	public void setPredecessorRef(ObjectReference predecessorRef) {
		this.predecessorRef = predecessorRef;
	}
	
	public ATPublishPackage getPredecessor() {
		if(null==predecessorRef){
			return null;
		}
		return (ATPublishPackage) predecessorRef.getObject();
	}

	public void setPredecessor(ATPublishPackage predecessor) {
		this.predecessorRef = ObjectReference.newObjectReference(predecessor);
	}

	public ManageInfo getManageInfo() {
		return manageInfo;
	}

	public void setManageInfo(ManageInfo manageInfo) {
		this.manageInfo = manageInfo;
	}

	public LifeCycleInfo getLifeCycleInfo() {
		return lifeCycleInfo;
	}

	public void setLifeCycleInfo(LifeCycleInfo lifeCycleInfo) {
		this.lifeCycleInfo = lifeCycleInfo;
	}
	
}
