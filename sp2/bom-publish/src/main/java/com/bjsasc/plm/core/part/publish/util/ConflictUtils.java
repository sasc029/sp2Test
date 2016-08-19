package com.bjsasc.plm.core.part.publish.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bjsasc.plm.Helper;
import com.bjsasc.plm.core.part.Part;
import com.bjsasc.plm.core.part.PartMaster;
import com.bjsasc.plm.core.part.link.PartUsageLink;
import com.bjsasc.plm.core.part.publish.conflict.*;
import com.bjsasc.plm.core.persist.PersistUtil;
import com.bjsasc.plm.core.vc.model.ControlBranch;
import com.bjsasc.plm.core.vc.model.Iterated;
import com.bjsasc.plm.core.view.publish.ViewChangeConflict;

/**
 * ��ͼת����ͻ������
 * 
 * @author avidm
 */
public class ConflictUtils {
	
	private ConflictUtils() {

	}

	/**
	 * ����������
	 * @param solution �����������
	 * @param toModify ��Ҫ���ĵĶ���
	 * @return
	 */
	public static PartChangeSolution buildSolution(String solution, Object toModify) {
		PartChangeSolution changeSolution = null;
		if (solution.equals(ViewChangeConflict.SOLUTION_COVER)) {// ����
			changeSolution = new PartCoverSolution(toModify);
		} else if (solution.equals(ViewChangeConflict.SOLUTION_CUSTOM)) {// �Զ���
			changeSolution = new PartCustomSolution(toModify);
		} else {
			changeSolution = new PartReserveSolution(toModify);
		}
		return changeSolution;
	}
	
	public static boolean CanReCreateLink=false;
	
	public static void UpdatePartUsageLink(String viewid)
	{
		//1.��ѯ���в���������
		StringBuilder hql = new StringBuilder();
		hql.append("from PartMaster");
		List<PartMaster> masterlist =  Helper.getPersistService().find(hql.toString());
		//2.���Ҳ����ĵ�һ���汾
		int i=0;
		for(PartMaster master:masterlist)
		{
			i++;
			System.out.println(i+" in " +masterlist.size()+ "	"+ master.getNumber());
			//ControlBranch designbranch=Helper.getVersionService().getLatestControlBranch(master, viewid);
			//Iterated latestdesign=Helper.getVersionService().getLatestIteration(designbranch);
			//Iterated firstdeisgn=Helper.getVersionService().getFirstIteration(latestdesign);
			
			//��ȡ��master������С�汾����
			List<Iterated> allversions=Helper.getVersionService().allIterationsOf(master);
			Map<String,List<String>> existdesignids=new HashMap<String,List<String>>();
			//�ñ�ʶλ��ʾ�Ƿ��ǵ�һ��С�汾��
			boolean first=true;
			//��forѭ�����������Ӧmaster��ÿһ��С�汾��(����С�İ汾��ʼ����)
			for(int j=allversions.size()-1;j>=0;j--)
			{
				Iterated design=allversions.get(j);
				//�����ǰ����ͼ����ָ������ͼ��ֱ��������
				if(!((Part)design).getViewRef().getInnerId().equalsIgnoreCase(viewid)) break;
				//3.���ݲ����ĵ�һ���汾��ȡ����ʹ�ù�ϵ
				List<PartUsageLink> designlinks=Helper.getPartService().getPartUsageLinkByFrom((Part)design);
				Map<String,List<String>> removeddesignids=new HashMap<String,List<String>>();
				//��forѭ����������ÿһ���汾��Ӧ������ʹ�ù�ϵ��
				for(PartUsageLink designlink:designlinks)
				{
					String childmasterid=designlink.getTo().getInnerId();
					//��ȡ��һ���汾��Ӧ��ʹ�ù�ϵ�ϵ�uniqueid.
					Object oldid=designlink.getExtAttr("uniqueid");
					
					if(null==oldid)
					{
						//�����ʹ�ù�ϵ�ϵ�uniqueidΪ�գ�����Map�в����ڸð汾�����masterid�����Ҳ��ǵ�һ��С�汾
						if((existdesignids.containsKey(childmasterid)==true)&&(!first))
						{
							List<String> ids=existdesignids.get(childmasterid);
							String id=ids.get(0);
							designlink.setExtAttr("uniqueid",id);
							ids.remove(0);
							if(ids.size()==0) existdesignids.remove(childmasterid);
							
							/*???
							 * String sid;
							 * 
							 * List<String> ids=existdesignids.get(childmasterid);
							 * if(ids.size()!=0){ 
							 * 	String id=ids.get(0);
							 * 	designlink.setExtAttr("uniqueid",id);
							 * 	sid=id;
							 * 	ids.remove(0);
							 * }
							 * else{
							 * 	designlink.setExtAttr("uniqueid",designlink.getInnerId());
							 *  sid=designlink.getInnerId();
							 * }
							 * 
							 * PersistUtil.getService().merge(designlink); 
							 * 
							 * ��ɾ�������ݼ�¼���������ں�����ԭ
							 * if(removeddesignids.containsKey(childmasterid)==false)
							 * {
							 * removeddesignids.put(childmasterid,new ArrayList<String>());
							 * }
							 *List<String> removedids=removeddesignids.get(childmasterid);
							 *if(removedids.contains(sid)==false)
							 *{
							 * removedids.add(sid);
							 *}
							 */
							
							PersistUtil.getService().merge(designlink); 
							
							//��ɾ�������ݼ�¼���������ں�����ԭ
							if(removeddesignids.containsKey(childmasterid)==false)
							{
								removeddesignids.put(childmasterid,new ArrayList<String>());
								
							}
							List<String> removedids=removeddesignids.get(childmasterid);
							if(removedids.contains(id)==false)
							{
								removedids.add(id);
							}
						}
						else
						{
							String id=designlink.getInnerId();
							
							/*???
							 * List<String> ids=existdesignids.get(childmasterid);
							if(ids.contains(id)==false)
							{
								ids.add(id);
							}	*/
							
							List<String> ids=new ArrayList<String>();
							ids.add(id);
							existdesignids.put(childmasterid,ids);
							designlink.setExtAttr("uniqueid",id);
							PersistUtil.getService().merge(designlink); 
						}
					}
					else
					{
						if(existdesignids.containsKey(childmasterid)==false)
						{
							existdesignids.put(childmasterid,new ArrayList<String>());
							
						}
						List<String> ids=existdesignids.get(childmasterid);
						if(ids.contains(oldid.toString())==false)
						{
							ids.add(oldid.toString());
						}
						
					}
				}
				
				
				//��ԭɾ����id
				for(String key:removeddesignids.keySet())
				{
					List<String> ids;
					if(existdesignids.containsKey(key)==false)
					{
						existdesignids.put(key, new ArrayList<String>());
					}
					ids=existdesignids.get(key);
					
					for(String value:removeddesignids.get(key))
					{
						if(ids.contains(value)==false)
						{
							ids.add(value);
						}
					}
					
				}
				if(existdesignids.size()>0)
				{
					first=false;
				}
				System.out.println(design.getIterationInfo().getVersionNo()+"."+ design.getIterationInfo().getIterationNo() + "----" +existdesignids.toString());
			}
			
		}
	}

}
