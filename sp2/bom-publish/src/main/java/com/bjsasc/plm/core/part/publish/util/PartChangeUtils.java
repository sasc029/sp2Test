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
		//临时处理方案：如果属性是内部属性，则不修改
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
		//针对枚举类型的字段，由于A5的模型XML文件中将其配置成了字符串型，调用底层的commonObject.setValue会自动将枚举转成字符串再保存到对象上，此时就会报错
				//临时解决办法，不进行类型转换，直接通过反射设置值
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
		// 判断说明关系是否已经建立
		List<PartDecribeLink> list = partService.getPartDecribeLinkByFromAndTo(
				downStream, doc);
		if (list == null || list.size() == 0) { // 如果说明关系未建立
			PartDecribeLink decribeLink = partService.newPartDecribeLink(
					downStream, doc);
			persistService.save(decribeLink);
		}
	}

	public static void deleteDescribeLink(Part downStream, Document doc) {
		// 需要删除的说明文档关系
		List<PartDecribeLink> partDecribeLinks = partService
				.getPartDecribeLinkByFromAndTo(downStream, doc);
		persistService.delete(partDecribeLinks);
	}
	
	public static void createReferenceLink(Part downStream, DocumentMaster docMaster) {
		// 判断参考关系是否已经建立
		List<PartReferenceLink> list = partService.getPartReferenceLinkByFromAndTo(
				downStream, docMaster);
		if (list == null || list.size() == 0) { // 如果参考关系未建立
			PartReferenceLink referenceLink = partService.newPartReferenceLink(
					downStream, docMaster);
			persistService.save(referenceLink);
		}
	}
	
	public static void deleteReferenceLink(Part downStream,
			DocumentMaster docMaster) {
		// 需要删除的参考关系
		List<PartReferenceLink> list = partService
				.getPartReferenceLinkByFromAndTo(downStream, docMaster);
		persistService.delete(list);
	}
	
	@SuppressWarnings("deprecation")
	public static void createUsageLink(Part downStream, PartMaster partMaster, PartUsageLink srcLink) {
		
		PartUsageLink newLink = (PartUsageLink)srcLink.clone();
		newLink.setInnerId(UUIDService.getUUID());
		//临时解决视图转换时若存在关系多对多情况下，无法识别具体哪一条上游关系对应下游具体关系的问题
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
		// 修改使用关系
		List<PartUsageLink> toModifyLinks = Helper.getPartService().getPartUsageLinksByFromAndTo(downStream,partMaster);
		if (toModifyLinks == null || toModifyLinks.size()==0) {
			return;
		}
		//查找匹配的关系，如果没有匹配，则随便删除一条uniqueid为空 的链接
		PartUsageLink findlink=null;
		PartUsageLink emptylink=null;
		Object uniqueid=srcLink.getExtAttr("uniqueid");
		//如果源关系的唯一ID不存在，则不执行删除
		//TODO:后台打印错误信息
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
		//查找匹配的关系，如果没有匹配，则随便删除一条uniqueid为空 的链接
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


