package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.util.SpringUtil;
/**
 * ��ͼ��������
 * @author zhangmeng
 *
 */
public class ViewPublishHelper {

private static ViewPublishService publishService;
	
	public static ViewPublishService getService() {
		if (publishService == null) {
			publishService = (ViewPublishService) SpringUtil.getBean("plm_view_publishservice");
		}
		return publishService;
	}
}
