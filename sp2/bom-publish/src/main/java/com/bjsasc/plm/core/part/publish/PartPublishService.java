package com.bjsasc.plm.core.part.publish;

import java.util.ArrayList;
import java.util.List;

import com.bjsasc.plm.core.baseline.model.Baseline;
import com.bjsasc.plm.core.baseline.model.BaselineMemberLink;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.view.publish.ViewPublishService;
import com.bjsasc.plm.core.view.publish.model.ATPublishBaseline;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATSnapshot;

public interface PartPublishService extends ViewPublishService {
	/**
	 * ������ͼת��������
	 * @param publishPackage
	 */
	public void createViewChangeItems(ATPublishPackage publishPackage);
	/**
	 * �ڷ����������Ҳ�������
	 * @param partMaster
	 * @return
	 */
	public ATSnapshot getPartSnapshotInPublishBaseline(PartMaster partMaster,ATPublishBaseline publishBaseline);
	/**
	 * ��ȡ���������ڷ��������е���
	 * @param publishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<ATSnapshot> getChildInPublishBaseline(ATPublishBaseline publishBaseline,ATSnapshot atSnapshot);
	
	/**
	 * �ڷ����������Ҳ�������,���Ҹò������ն�Ӧ�Ĳ���û�б�ɾ��
	 * @param partMaster
	 * @return
	 */
	public List<BaselineMemberLink> getPartSnapshotInPublishBaseline(Baseline baseline);
	
	
	
}
