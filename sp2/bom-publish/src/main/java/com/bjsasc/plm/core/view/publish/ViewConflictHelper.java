package com.bjsasc.plm.core.view.publish;

import com.bjsasc.plm.core.util.SpringUtil;

public class ViewConflictHelper {

	private static ViewConflictService conflictService;
	
	public static ViewConflictService getService() {
		if (conflictService == null) {
			conflictService = (ViewConflictService) SpringUtil.getBean("plm_view_conflictservice");
		}
		return conflictService;
	}
	
}
