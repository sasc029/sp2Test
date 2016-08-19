package com.bjsasc.plm.core.part.publish.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.bjsasc.platform.objectmodel.business.persist.CommonObject;
import com.bjsasc.platform.objectmodel.business.persist.PersistUtil;
import com.bjsasc.platform.objectmodel.managed.external.util.ModelInfoUtil;
import com.bjsasc.platform.objectmodel.managed.modelattr.data.ModelAttr;
import com.bjsasc.platform.objectmodel.util.ReflectionUtil;
import com.bjsasc.plm.Helper;
import com.bjsasc.plm.core.doc.Document;
import com.bjsasc.plm.core.doc.DocumentMaster;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartHelper;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.PartService;
import com.bjsasc.plm.core.part.PartStandardConfigSpec;
import com.bjsasc.plm.core.part.Quantity;
import com.bjsasc.plm.core.part.link.PartDecribeLink;
import com.bjsasc.plm.core.part.link.PartReferenceLink;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.part.publish.model.PartAttrChangePoint;
import com.bjsasc.plm.core.persist.PersistHelper;
import com.bjsasc.plm.core.persist.PersistService;
import com.bjsasc.plm.core.persist.model.Link;
import com.bjsasc.plm.core.persist.model.Persistable;
import com.cascc.avidm.util.UUIDService;

public class PartChangeUtils {
	
	private static PersistService persistService = PersistHelper.getService();
	private static PartService partService = PartHelper.getService();
	
	private PartChangeUtils() {
		
	}
	
	/**
	 * 
	 * @param toModify
	 * @param attrChangePoint
	 */
	public static void changePartAttr(Part part, PartAttrChangePoint attrChangePoint) {
		String attrId = attrChangePoint.getAttrId();
		//��ʱ������������������ڲ����ԣ����޸�
		if(attrId.equalsIgnoreCase("CREATOR")) return;
		if(attrId.equalsIgnoreCase("MODIFIER")) return;
		Serializable attrValue = attrChangePoint.getFinalContent();
		setAttrValueIgnoreCase(part, attrId, attrValue);
		persistService.update(part);
	}

	public static void changeMasterAttr(PartMaster master, PartAttrChangePoint attrChangePoint) {
		String attrId = attrChangePoint.getAttrId();
		Serializable attrValue = attrChangePoint.getFinalContent();
		setAttrValueIgnoreCase(master, attrId, attrValue);
		persistService.update(master);
	}
	
	public static void changeUsageLinkAttr(PartUsageLink link, PartAttrChangePoint attrChangePoint) {
		PartUsageLink toModifyUsageLink = (PartUsageLink) link;
		String attrId = attrChangePoint.getAttrId();
		if (attrId.equalsIgnoreCase("Quantity")) {
			toModifyUsageLink.setQuantity((Quantity) attrChangePoint.getFinalContent());
		} else {
			setAttrValueIgnoreCase(toModifyUsageLink, attrId, attrChangePoint.getFinalContent());
		}
		persistService.update(toModifyUsageLink);
	}
	
	private static void setAttrValueIgnoreCase(CommonObject commonObject, String attrId, Object value) {
		
		String classId = commonObject.getClassId();
		List<ModelAttr> modelAttrs = ModelInfoUtil
				.getModelAttrsWithInComp(classId);
		Map<String, String> attrMap = new HashMap<String, String>(); 
		for (ModelAttr modelAttr : modelAttrs) {
			String newattrId = modelAttr.getAttrId();
			attrMap.put(newattrId.toUpperCase(), newattrId);
		}
		if("PARTSOURCE".equalsIgnoreCase(attrId)){
			attrId = "source";
		}
		String originalKey = attrMap.get(attrId.toUpperCase());
		//���ö�����͵��ֶΣ�����A5��ģ��XML�ļ��н������ó����ַ����ͣ����õײ��commonObject.setValue���Զ���ö��ת���ַ����ٱ��浽�����ϣ���ʱ�ͻᱨ��
				//��ʱ����취������������ת����ֱ��ͨ����������ֵ
				if("TRACETYPE".equalsIgnoreCase(attrId))
				{
					ReflectionUtil.invokeSet(commonObject, originalKey, value);
				}
				else
				{
					commonObject.setValue(originalKey, value);
				}
		}

	public static void createDescribeLink(Part downStream, Document doc) {
		// �ж�˵����ϵ�Ƿ��Ѿ�����
		List<PartDecribeLink> list = partService.getPartDecribeLinkByFromAndTo(
				downStream, doc);
		if (list == null || list.size() == 0) { // ���˵����ϵδ����
			PartDecribeLink decribeLink = partService.newPartDecribeLink(
					downStream, doc);
			persistService.save(decribeLink);
		}
	}

	public static void deleteDescribeLink(Part downStream, Document doc) {
		// ��Ҫɾ����˵���ĵ���ϵ
		List<PartDecribeLink> partDecribeLinks = partService
				.getPartDecribeLinkByFromAndTo(downStream, doc);
		persistService.delete(partDecribeLinks);
	}
	
	public static void createReferenceLink(Part downStream, DocumentMaster docMaster) {
		// �жϲο���ϵ�Ƿ��Ѿ�����
		List<PartReferenceLink> list = partService.getPartReferenceLinkByFromAndTo(
				downStream, docMaster);
		if (list == null || list.size() == 0) { // ����ο���ϵδ����
			PartReferenceLink referenceLink = partService.newPartReferenceLink(
					downStream, docMaster);
			persistService.save(referenceLink);
		}
	}
	
	public static void deleteReferenceLink(Part downStream,
			DocumentMaster docMaster) {
		// ��Ҫɾ���Ĳο���ϵ
		List<PartReferenceLink> list = partService
				.getPartReferenceLinkByFromAndTo(downStream, docMaster);
		persistService.delete(list);
	}
	
	@SuppressWarnings("deprecation")
	public static void createUsageLink(Part downStream, PartMaster partMaster, PartUsageLink srcLink) {
		
		PartUsageLink newLink = (PartUsageLink)srcLink.clone();
		newLink.setInnerId(UUIDService.getUUID());
		//��ʱ�����ͼת��ʱ�����ڹ�ϵ��Զ�����£��޷�ʶ�������һ�����ι�ϵ��Ӧ���ξ����ϵ������
		//By NYQ,ZM 2015/07/11
		newLink.setExtAttr("prelink", srcLink.getExtAttr("uniqueid"));
		newLink.setExtAttr("uniqueid", newLink.getInnerId());
		newLink.setFromObject(downStream);
		newLink.setToObject(partMaster);
		
		com.bjsasc.plm.core.persist.PersistUtil.getService().save(newLink);
		
	}
	
public static void deleteUsageLink(Part downStream, PartMaster partMaster, PartUsageLink srcLink) {
		
		
		PartStandardConfigSpec configspec = new PartStandardConfigSpec();
		configspec.setView(downStream.getView());
		
		Persistable downStreamUsesPart =  Helper.getConfigSpecService().filteredIterationsOf(partMaster, configspec);
		if (downStreamUsesPart == null || downStreamUsesPart instanceof PartMaster) {
			return;
		}
		// �޸�ʹ�ù�ϵ
		List<PartUsageLink> toModifyLinks = Helper.getPartService().getPartUsageLinksByFromAndTo(downStream,partMaster);
		if (toModifyLinks == null || toModifyLinks.size()==0) {
			return;
		}
		//����ƥ��Ĺ�ϵ�����û��ƥ�䣬�����ɾ��һ��uniqueidΪ�� ������
		PartUsageLink findlink=null;
		PartUsageLink emptylink=null;
		Object uniqueid=srcLink.getExtAttr("uniqueid");
		//���Դ��ϵ��ΨһID�����ڣ���ִ��ɾ��
		//TODO:��̨��ӡ������Ϣ
		if(null==uniqueid) return;
		for (PartUsageLink link : toModifyLinks) {
			if(link.getExtAttr("prelink")==null)
			{
				emptylink=link;
			}
			System.out.println("----------------");
			System.out.println(uniqueid);
			System.out.println(link.getExtAttr("prelink"));
			if((null!=uniqueid)&&(uniqueid.equals(link.getExtAttr("prelink"))))
			{
				findlink=link;
				break;
			}
		}
		//����ƥ��Ĺ�ϵ�����û��ƥ�䣬�����ɾ��һ��uniqueidΪ�� ������
		if(findlink!=null)
		{
			PersistHelper.getService().delete(findlink);
		}
		else
		{
			if(emptylink!=null)
			{
				PersistHelper.getService().delete(emptylink);
			}
		}
		
	}

}


