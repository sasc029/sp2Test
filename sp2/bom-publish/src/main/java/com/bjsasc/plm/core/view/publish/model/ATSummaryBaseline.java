package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.platform.objectmodel.business.persist.ObjectReference;
import com.bjsasc.plm.core.context.model.ProductContext;
import com.bjsasc.plm.core.view.View;

/**
 * »ã×Ü»ùÏß
 * @author avidm
 * @since 2014-6-18
 */
public class ATSummaryBaseline extends ATPublishBaseline {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1838285798652004135L;
	public static final String CLASSID = ATSummaryBaseline.class.getSimpleName();
	
	private ObjectReference prodContextRef;
	private ObjectReference sourceViewRef;
	private ObjectReference targetViewRef;
	
	public ATSummaryBaseline() {
		setClassId(CLASSID);
	}
	
	public ObjectReference getProdContextRef() {
		return prodContextRef;
	}

	public void setProdContextRef(ObjectReference prodContextRef) {
		this.prodContextRef = prodContextRef;
	}

	public ProductContext getProdContext() {
		return (ProductContext) prodContextRef.getObject();
	}

	public void setProdContext(ProductContext prodContext) {
		this.prodContextRef = ObjectReference.newObjectReference(prodContext);
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
	
}
