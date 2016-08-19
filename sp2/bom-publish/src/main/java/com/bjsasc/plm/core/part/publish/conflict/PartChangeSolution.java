package com.bjsasc.plm.core.part.publish.conflict;

import com.bjsasc.plm.core.part.publish.model.PartAddChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangeConflict;
import com.bjsasc.plm.core.view.publish.conflict.ChangeSolution;

/**
 * 解决方案接口
 * @author caorang
 */
public interface PartChangeSolution extends ChangeSolution {
	
	/**
	 * 解决新增冲突
	 * @param toModify
	 * @param conflict
	 */
	public void solve(PartAddChangeConflict conflict);
	
	/**
	 * 解决属性冲突
	 * @param toModify
	 * @param conflict
	 */
	public void solve(PartAttrChangeConflict conflict);
	
	/**
	 * 解决关系冲突
	 * @param toModify
	 * @param conflict
	 */
	public void solve(PartLinkChangeConflict conflict);

}
