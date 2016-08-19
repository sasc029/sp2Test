package com.bjsasc.plm.core.part.publish.model;

import com.bjsasc.plm.core.view.publish.model.AbstractViewChangeItem;

/**
 * 部件修改更改项
 * @author zhuhongtao
 */
public class PartModifyChangeItem extends AbstractViewChangeItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5974889038382850872L;
	public static final String CLASSID = PartModifyChangeItem.class.getSimpleName();

	public PartModifyChangeItem() {
		setClassId(CLASSID);
	}
	
}
