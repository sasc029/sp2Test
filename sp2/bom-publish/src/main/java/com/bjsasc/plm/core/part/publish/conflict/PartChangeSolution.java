package com.bjsasc.plm.core.part.publish.conflict;

import com.bjsasc.plm.core.part.publish.model.PartAddChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangeConflict;
import com.bjsasc.plm.core.part.publish.model.PartLinkChangeConflict;
import com.bjsasc.plm.core.view.publish.conflict.ChangeSolution;

/**
 * ��������ӿ�
 * @author caorang
 */
public interface PartChangeSolution extends ChangeSolution {
	
	/**
	 * ���������ͻ
	 * @param toModify
	 * @param conflict
	 */
	public void solve(PartAddChangeConflict conflict);
	
	/**
	 * ������Գ�ͻ
	 * @param toModify
	 * @param conflict
	 */
	public void solve(PartAttrChangeConflict conflict);
	
	/**
	 * �����ϵ��ͻ
	 * @param toModify
	 * @param conflict
	 */
	public void solve(PartLinkChangeConflict conflict);

}
