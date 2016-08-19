package com.bjsasc.plm.part.publish;

import java.util.*;

import com.bjsasc.plm.core.view.publish.ViewChangePoint;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;

/**
 * ��ͼת����������Web������
 * @author avidm
 * @since 2014-6-19
 */
public interface PartReceiveService {

	/**
	 * ���ݷ�������ѡ��ĸ��ĵ���и����ύ����������Ӧ�ĸ�����־
	 * @param publishPackage
	 * @param changePoints
	 */
	public void executeChangePoints(ATPublishPackage publishPackage, Collection<ViewChangePoint> selectedPoints);
	
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
	
}
