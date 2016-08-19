package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.view.ViewManageable;

/**
 * ��ͼת����ͻ����ӿ�
 * @author zhuhongtao
 * @since 2014-6-23
 */
public interface ViewConflictService {

	/**
	 * ���ݸ��ĵ�����β��������ض�Ӧ�ò����ĳ�ͻ,������Ϊnull��������β����޳�ͻ
	 * @param changePoint
	 * @param downStream
	 * @return ��ͼת����ͻ
	 */
	public <T extends ViewChangeConflict> T getConflictByDownstream(ViewChangePoint changePoint, ViewManageable downStream, Class<T> c);
	
}
