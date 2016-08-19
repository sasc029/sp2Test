package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.persist.model.Persistable;
import com.bjsasc.plm.core.view.View;
/**
 * �����ڣ���ͼת�����ĵ�
 * @author zhuhongtao
 *
 */ 


public interface ViewChangePoint extends Persistable {
	/**
	 * @return ��ͼת��������
	 */
	public ViewChangeItem getViewChangeItem();
	/**
	 * ������ͼת��������
	 */
	public void setViewChangeItem(ViewChangeItem changeItem);
	/**
	 * @return Դ��ͼ
	 */
	public View getSourceView();
	/**
	 * @return Ŀ����ͼ
	 */
	public View getTargetView();
	/**
	 * �Ƿ��ͻ
	 */
	public boolean getIsConflict();
	
	public void setIsConflict(boolean isConflict);
	/**
	 * ��ȡ��ע
	 */
	public String getNote();
	/**
	 * ��ȡִ�н��
	 */
	public String getResult();
	/**
	 * ����ִ�н��
	 */
	public void setResult(String result);
	/**
	 * ��ø�����������
	 */
	public String getChangeTip();
	
	/**��ȡĿ���������
	 * @return
	 */
	public Persistable getTarget();
	
	/**��ȡ����ʽ
	 * @return
	 */
	public String getDeal();

	/**���ô���ʽ
	 * @param deal
	 */
	public void setDeal(String deal);
}
