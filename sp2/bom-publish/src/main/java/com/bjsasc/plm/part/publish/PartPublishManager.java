package com.bjsasc.plm.part.publish;

import com.bjsasc.platform.spring.PlatformApplicationContext;

public class PartPublishManager {

	private static PartReceiveService partReceiveWebService = null;
	private static PartPublishService partPublishService= null;

	public static PartReceiveService getPartReceiveManager() {
		if (null == partReceiveWebService) {
			partReceiveWebService = (PartReceiveService) PlatformApplicationContext
					.getBean("plm_partreceive_manager");
		}
		return partReceiveWebService;
	}

	public static PartPublishService getPartPublishManager() {
		if (null == partPublishService) {
			partPublishService = (PartPublishService) PlatformApplicationContext
					.getBean("plm_partpublish_manager");
		}
		return partPublishService;
	}

}
