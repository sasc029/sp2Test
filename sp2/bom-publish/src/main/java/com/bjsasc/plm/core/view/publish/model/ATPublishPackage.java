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
 * ��ͼ����������������ͼ�����Ĳ�����������Դ��ͼ��Ŀ����ͼ���������ߵ���Ϣ
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
     * ����������
     */
	private String name;
	
	/**
     * ��������ʶ
     */
	private String number;
	
	/**
	 * ��������
	 */
	private ObjectReference publishPartRef;
	
	/**
	 * ��������
	 */
	private ObjectReference publishBaselineRef;
	
	/**
	 * ���ܻ���
	 */
	private ObjectReference summaryBaselineRef;
	
	/**
	 * ����������ͼ
	 */
	private ObjectReference sourceViewRef;
	
	/**
	 * ����������ͼ
	 */
	private ObjectReference targetViewRef;
	
	/**
	 * ��������Ϣ
	 */
	private ContextInfo contextInfo;
	
	/**
	 * ǰ��������
	 */
	private ObjectReference predecessorRef;
	
	/**
	 * ������Ϣ
	 */
	private ManageInfo manageInfo;
	
	/**
	 * ����������Ϣ
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
