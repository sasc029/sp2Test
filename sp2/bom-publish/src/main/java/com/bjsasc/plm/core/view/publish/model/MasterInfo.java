package com.bjsasc.plm.core.view.publish.model;

import com.bjsasc.plm.core.vc.model.Master;

/**
 * 主对象信息
 * @author avidm
 * @since 2014-6-15
 */
public class MasterInfo extends Master {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5434051722820499377L;
	public static final String CLASSID = MasterInfo.class.getSimpleName();
	
	/**
	 * 主对象名称
	 */
	private String masterName;
	
	/**
	 * 主对象编号
	 */
	private String masterNumber;
	
	public MasterInfo(){
		setClassId(MasterInfo.CLASSID);
	}

	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	public String getMasterNumber() {
		return masterNumber;
	}

	public void setMasterNumber(String masterNumber) {
		this.masterNumber = masterNumber;
	}
	
}
