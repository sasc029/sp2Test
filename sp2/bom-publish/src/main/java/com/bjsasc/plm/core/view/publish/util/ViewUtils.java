package com.bjsasc.plm.core.view.publish.util;

import java.util.ArrayList;
import java.util.List;

import com.bjsasc.plm.core.view.View;
import com.bjsasc.plm.core.view.ViewManageable;

public class ViewUtils {

	/**
	 * ����Ŀ����ͼ���й��ˣ������ڸ���ͼ�µ�����ͼ�������
	 * @param viewManageables
	 * @param targetView
	 * @return ���˽��
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
