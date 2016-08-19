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
	 * 创建视图转换更改项
	 * @param publishPackage
	 */
	public void createViewChangeItems(ATPublishPackage publishPackage);
	/**
	 * 在发布基线中找部件快照
	 * @param partMaster
	 * @return
	 */
	public ATSnapshot getPartSnapshotInPublishBaseline(PartMaster partMaster,ATPublishBaseline publishBaseline);
	/**
	 * 获取部件快照在发布基线中的子
	 * @param publishBaseline
	 * @param atSnapshot
	 * @return
	 */
	public List<ATSnapshot> getChildInPublishBaseline(ATPublishBaseline publishBaseline,ATSnapshot atSnapshot);
	
	/**
	 * 在发布基线中找部件快照,并且该部件快照对应的部件没有被删除
	 * @param partMaster
	 * @return
	 */
	public List<BaselineMemberLink> getPartSnapshotInPublishBaseline(Baseline baseline);
	
	
	
}
