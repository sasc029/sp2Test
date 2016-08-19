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
 * 视图转换冲突工具类
 * 
 * @author avidm
 */
public class ConflictUtils {
	
	private ConflictUtils() {

	}

	/**
	 * 构造解决方案
	 * @param solution 解决方案类型
	 * @param toModify 需要更改的对象
	 * @return
	 */
	public static PartChangeSolution buildSolution(String solution, Object toModify) {
		PartChangeSolution changeSolution = null;
		if (solution.equals(ViewChangeConflict.SOLUTION_COVER)) {// 覆盖
			changeSolution = new PartCoverSolution(toModify);
		} else if (solution.equals(ViewChangeConflict.SOLUTION_CUSTOM)) {// 自定义
			changeSolution = new PartCustomSolution(toModify);
		} else {
			changeSolution = new PartReserveSolution(toModify);
		}
		return changeSolution;
	}
	
	public static boolean CanReCreateLink=false;
	
	public static void UpdatePartUsageLink(String viewid)
	{
		//1.查询所有部件主对象
		StringBuilder hql = new StringBuilder();
		hql.append("from PartMaster");
		List<PartMaster> masterlist =  Helper.getPersistService().find(hql.toString());
		//2.查找部件的第一个版本
		int i=0;
		for(PartMaster master:masterlist)
		{
			i++;
			System.out.println(i+" in " +masterlist.size()+ "	"+ master.getNumber());
			//ControlBranch designbranch=Helper.getVersionService().getLatestControlBranch(master, viewid);
			//Iterated latestdesign=Helper.getVersionService().getLatestIteration(designbranch);
			//Iterated firstdeisgn=Helper.getVersionService().getFirstIteration(latestdesign);
			
			//获取该master的所有小版本对象
			List<Iterated> allversions=Helper.getVersionService().allIterationsOf(master);
			Map<String,List<String>> existdesignids=new HashMap<String,List<String>>();
			//该标识位表示是否是第一个小版本。
			boolean first=true;
			//该for循环遍历处理对应master的每一个小版本。(从最小的版本开始处理)
			for(int j=allversions.size()-1;j>=0;j--)
			{
				Iterated design=allversions.get(j);
				//如果当前的视图不是指定的视图，直接跳出。
				if(!((Part)design).getViewRef().getInnerId().equalsIgnoreCase(viewid)) break;
				//3.根据部件的第一个版本获取所有使用关系
				List<PartUsageLink> designlinks=Helper.getPartService().getPartUsageLinkByFrom((Part)design);
				Map<String,List<String>> removeddesignids=new HashMap<String,List<String>>();
				//该for循环遍历处理每一个版本对应的所有使用关系。
				for(PartUsageLink designlink:designlinks)
				{
					String childmasterid=designlink.getTo().getInnerId();
					//获取第一个版本对应的使用关系上的uniqueid.
					Object oldid=designlink.getExtAttr("uniqueid");
					
					if(null==oldid)
					{
						//如果该使用关系上的uniqueid为空，并且Map中不存在该版本对象的masterid，并且不是第一个小版本
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
							 * 将删除的数据记录下来，便于后续还原
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
							
							//将删除的数据记录下来，便于后续还原
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
				
				
				//还原删除的id
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
