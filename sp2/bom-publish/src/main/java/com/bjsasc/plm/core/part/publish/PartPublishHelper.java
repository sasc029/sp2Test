package com.bjsasc.plm.core.part.publish;

import com.bjsasc.plm.core.util.SpringUtil;

public class PartPublishHelper {
	
	private static PartPublishService publishService;
	private static PartReceiveService receiveService;
	
	public PartPublishHelper() {
		
	}

	public static PartPublishService getPublishService() {
		if (publishService == null) {
			publishService = (PartPublishService) SpringUtil.getBean("plm_part_publishservice");
		}
		return publishService;
	}
	
	public static PartReceiveService getReceiveService() {
		if (receiveService == null) {
			receiveService = (PartReceiveService) SpringUtil.getBean("plm_part_receiveservice");
		}
		return receiveService;
	}
	


}
