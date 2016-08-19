package com.bjsasc.plm.core.view.publish;

import java.util.Collection;
import java.util.List;

import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ViewChangeType;

/**
 * ��ͼ���շ���
 * @author caorang
 * @since 2014-06-17
 */
public interface ViewReceiveService {
	
	/**
	 * Ӧ������������
	 * @param targetView ������ͼ
	 * @param item ������
	 * @param selected 
	 */
	public void executeAddChangeItem(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected);
	
	/**
	 * Ӧ���޸ĸ�����
	 * @param viewManageable ����
	 * @param targetView ������ͼ
	 * @param item ������
	 * @param selected 
	 */
	public void executeModifyChangeItem(View targetView, ViewChangeItem item, Collection<ViewChangePoint> selected);
	
	/**
	 * �����������޸ĵĶ��������������
	 * @param viewManageable
	 * @param publishPackage
	 * @param changeType
	 */
	public void addMemberToPackage(ViewManageable viewManageable, ATPublishPackage publishPackage, ViewChangeType changeType);
	
	/**
	 * �������ͻ�ȡ��������Ա�б�
	 * @param publishPackage
	 * @param changeType
	 */
	public List<ViewManageable> getMembersFromPackage(ATPublishPackage publishPackage, ViewChangeType changeType);
	
	/**
	 * ����������Ա�б����
	 * @param publishPackage
	 */
	public void clearPackageMembers(ATPublishPackage publishPackage);
	
	/**
	 * �ύ���ģ����뷢���������д������Ա����
	 * @param publishPackage
	 */
	public void commitChange(ATPublishPackage publishPackage);
	
	/**
	 * �ع����ģ����������ڼ���Ķ���ȡ������������Ķ������ɾ��
	 * @param publishPackage
	 */
	public void rollbackChange(ATPublishPackage publishPackage);
	
	/**
	 * ���·������ܻ���
	 * @param publishPackage �µķ����� 
	 * @param updateType 1,���ܻ��� 2,��ʱ���ܻ���
	 */
	public void updateSummaryBaseline(ATPublishPackage publishPackage);
	
}
