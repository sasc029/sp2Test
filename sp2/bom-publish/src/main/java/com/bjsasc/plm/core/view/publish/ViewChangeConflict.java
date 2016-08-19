package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.ViewManageable;
/**
 * ����ӿڣ���ͼת����ͻ
 * @author zhuhongtao
 *
 */

public interface ViewChangeConflict extends Persistable{

	/**
	 * ��ͻ����������
	 */
	public static final String SOLUTION_RESERVE = "Reserve";
	public static final String SOLUTION_COVER = "Cover";
	public static final String SOLUTION_CUSTOM = "Custom";
	
	/**
	 * 
	 * @return ��ͼת�����ĵ�
	 */
	public ViewChangePoint getViewChangePoint();
	/**
	 * 
	 * @return ������ͼ�Եȶ���
	 */
	public ViewManageable getDownStream();
	/**
	 * 
	 * @return �������
	 */
	public String getSolution();
}
