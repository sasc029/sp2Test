package com.bjsasc.plm.core.part.publish.model;

import com.bjsasc.plm.core.view.publish.model.AbstractViewChangeItem;

/**
 * ��������������
 * @author zhuhongtao
 *
 */
public class PartAddChangeItem extends AbstractViewChangeItem {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 239261075033798649L;
	public static final String CLASSID = PartAddChangeItem.class.getSimpleName();
	
    public PartAddChangeItem() {
    	setClassId(CLASSID);
    }
    
}
