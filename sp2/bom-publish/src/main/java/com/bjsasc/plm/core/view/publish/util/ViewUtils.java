package com.bjsasc.plm.core.view.publish.util;

import java.util.ArrayList;
import java.util.List;

import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;

public class ViewUtils {

	/**
	 * 根据目标视图进行过滤，返回在该视图下的受视图管理对象
	 * @param viewManageables
	 * @param targetView
	 * @return 过滤结果
	 */
	public static List<ViewManageable> filterViewManageablesInView(
			List<ViewManageable> viewManageables, View targetView) {
		List<ViewManageable> result = new ArrayList<ViewManageable>();
		for (ViewManageable vm : viewManageables) {
			if (vm.getView().equals(targetView)) {
				result.add(vm);
			}
		}
		return result;
	}
	
}
