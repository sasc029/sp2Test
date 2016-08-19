package com.bjsasc.plm.core.view.publish;

import java.util.List;

import com.bjsasc.plm.core.type.ATLink;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.model.ATLinkSnapshot;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATPublishTask;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;

/**
 * ��ͼ��������
 * @author caorang
 * @since 2014-06-17
 */
public interface ViewPublishService {
    
	/**
	 * �½����ն���
	 * @return ���� 
	 */
	public ATSnapshot newATSnapshot(ViewManageable viewManageable);
	
	/**
	 * �½���ϵ���ն���
	 * @return ���� 
	 */
	public ATLinkSnapshot newATLinkSnapshot(ATLink link);
	
	/**
	 * ���ݷ���������ȡ�÷��������ض��ĸ��ĵ㼯��
	 * @param publishPackage
	 * @param c
	 * @return ���ĵ㼯��
	 */
	public <T extends ViewChangeItem> List<T> getViewChangeItems(ATPublishPackage publishPackage, Class<T> c);
	
	/**
	 * ��ȡ�����������и��ĵ�
	 */
	public <T extends ViewChangePoint> List<T> getViewChangePoints(ViewChangeItem changeItem, Class<T> c);
	
	/**
	 * ��ȡ���ĵ������и��ĳ�ͻ
	 */
	public <T extends ViewChangeConflict> List<T> getViewChangeConflicts(ViewChangePoint changePoint, Class<T> c);
	
	/**
	 * ���ݵ�ǰ��������ȡ����ǰ�÷�������ǰ�÷�������
	 * @param publishPackage
	 * @return
	*/	
	public List<Object[]> listAllPrePackages(ATPublishPackage publishPackage);
	
	/**
	 * �����ж��Ƿ���δ��ɵ�ǰ������
	 * @param publishPackage
	 * @return
	 */
	public Boolean isHasUndoPreTask(ATPublishPackage publishPackage);

	/**
	 * ���ݵ�ǰ��������ȡ�������е�����
	 * @param publishPackage
	 * @return
	*/	
	public ATPublishTask getPublishTask(ATPublishPackage publishPackage);
	
	/**
	 * �жϷ������Ƿ�����ύ
	 * 
	 * @author liyu
	 * @param publishPackage
	 * @return
	 */
	public Boolean isCanSubmit(ATPublishPackage publishPackage);
}
