package com.bjsasc.plm.part.publish;

public interface PartPublishService {
    /**
     * ���沢�´﷢������
     * @param convertNodeInfo BOM
     * @param partOid root_part
     * @param targetViewOid Ŀ����ͼ
     * @param number ������ʶ
     * @param aaUserInnerId ִ����
     */
	public void savePublishTask(String convertNodeInfo, String partOid, 
			 String targetViewOid, String number, String aaUserInnerId);
}
