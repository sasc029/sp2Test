package com.bjsasc.plm.part.publish.filter;

import com.bjsasc.plm.core.lifecycle.LifeCycleHelper;
import com.bjsasc.plm.core.session.SessionHelper;
import com.bjsasc.plm.core.view.publish.ViewPublishHelper;
import com.bjsasc.plm.core.view.publish.model.ATPublishPackage;
import com.bjsasc.plm.core.view.publish.model.ATPublishTask;
import com.bjsasc.plm.operate.Action;
import com.bjsasc.plm.ui.UIDataInfo;
import com.bjsasc.plm.ui.validation.UIState;
import com.bjsasc.plm.ui.validation.ValidationFilter;

public class SubmitPublishTaskFilter implements ValidationFilter {

	@Override
	public UIState doActionFilter(Action action, UIDataInfo uiData) {
		ATPublishTask task = (ATPublishTask)uiData.getMainObject();
		      //�ж�ִ���û��Ƿ�Ϊ��ǰ�û�
				if(!task.getTaskInfo().getExecutorRef().getInnerId().equals(SessionHelper.getService().getUser().getInnerId())){
					return UIState.DISABLED;
				}

				ATPublishPackage _package = task.getPublishPackage();
				//�жϰ��Ƿ��Ѿ��������
				if(LifeCycleHelper.getService().isInFinalPhase(_package)){
					return UIState.DISABLED;
				}
				//�жϵ�ǰ�������Ƿ�����ύ
				if(!ViewPublishHelper.getService().isCanSubmit(_package)){
					return UIState.DISABLED;
				}
					
                return UIState.ENABLED;
	}

}
