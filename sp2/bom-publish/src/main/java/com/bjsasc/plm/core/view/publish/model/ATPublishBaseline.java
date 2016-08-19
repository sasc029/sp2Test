package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.plm.core.baseline.model.Baseline;
import com.bjsasc.plm.core.lock.model.Lock;
import com.bjsasc.plm.core.type.ATObject;

/**
 * 发布基线
 * @author caorang
 * @since 2014-6-6
 * @version 1.0.0.0
 */
public class ATPublishBaseline extends ATObject implements Baseline {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4700593733575777076L;
	
	public static final String CLASSID = ATPublishBaseline.class.getSimpleName();
	
	private Lock lock;
	
	public ATPublishBaseline(){
		setClassId(ATPublishBaseline.CLASSID);
	}

	public Lock getLock() {
		return this.lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}
	
}
