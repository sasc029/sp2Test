package com.bjsasc.plm.part.publish;

public interface PartPublishService {
    /**
     * 保存并下达发布任务
     * @param convertNodeInfo BOM
     * @param partOid root_part
     * @param targetViewOid 目标视图
     * @param number 发布标识
     * @param aaUserInnerId 执行人
     */
	public void savePublishTask(String convertNodeInfo, String partOid, 
			 String targetViewOid, String number, String aaUserInnerId);
}
