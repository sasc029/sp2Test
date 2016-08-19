package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
/**
 * ����ӿڣ���ͼת��������
 * @author zhuhongtao
 *
 */

public interface ViewChangeItem extends Persistable {
	/**
	 * 
	 * @return Ӱ��ת������
	 */
	public ViewManageable getAffectedViewManageable();
	/**
	 * 
	 * @return ������
	 */
	public ATPublishPackage getPublishPackage();
	/**
	 * 
	 * @return Դ��ͼ
	 */
	public View getSourceView();
	/**
	 * 
	 * @return Ŀ����ͼ
	 */
	public View getTargetView();
	/**��ȡ����ʽ
	 * @return
	 */
	public String getDeal();

	/**���ô���ʽ
	 * @param deal
	 */
	public void setDeal(String deal);
}
