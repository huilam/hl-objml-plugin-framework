package hl.objml2.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class FrameDetectedObj {
	
	protected final static String FRAME_ID				= "frame_id";
	protected final static String FRAME_TIMESTAMP 		= "frame_timestamp";
	protected final static String FRAME_SOURCE 			= "frame_source";
	protected final static String FRAME_DETECTED_OBJS 	= "detected_obj";
	
	private long frame_id 			= -1;
	private long frame_timestamp 	= -1;
	private String frame_source 	= null;
	private Map<String, List<DetectedObj>> mapDetectedObjs = new HashMap<>();
	////	
	public long getFrame_id() {
		return frame_id;
	}
	public void setFrame_id(long frame_id) {
		this.frame_id = frame_id;
	}
	////
	public long getFrame_timestamp() {
		return frame_timestamp;
	}
	public void setFrame_timestamp(long frame_timestamp) {
		this.frame_timestamp = frame_timestamp;
	}
	////
	public String getFrame_source() {
		return frame_source;
	}
	public void setFrame_source(String frame_source) {
		this.frame_source = frame_source;
	}
	////
	public void addDetectedObj(DetectedObj obj)
	{
		if(obj!=null)
		{
			List<DetectedObj> listObj = getDetectedObjList(obj);
			listObj.add(obj);
			this.mapDetectedObjs.put(obj.getObj_classname(), listObj);
		}
	}
	public String[] getObjClassNames()
	{
		Set<String> setObjClassName = this.mapDetectedObjs.keySet();
		
		if(setObjClassName==null)
			return new String[] {};
		
		return (String[]) setObjClassName.toArray(new String[setObjClassName.size()]);
	}
	public List<DetectedObj> getDetectedObjByObjClassName(String aObjClassName)
	{
		return this.mapDetectedObjs.get(aObjClassName);
	}
	
	public void clearDetection()
	{
		this.mapDetectedObjs.clear();
	}
	
	public void addAll(Map<String, List<DetectedObj>> mapDetectedObjs)
	{
		if(mapDetectedObjs!=null)
		{
			Iterator<String> iterObjListName = mapDetectedObjs.keySet().iterator();
			while(iterObjListName.hasNext())
			{
				String sObjListName = iterObjListName.next();
				List<DetectedObj> listObjs = mapDetectedObjs.get(sObjListName);
				for(DetectedObj obj : listObjs)
				{
					addDetectedObj(obj);
				}
			}
		}
	}
	
	public long getTotalDetectionCount()
	{
		long lTotal = 0;
		for(String sObjClassName : getObjClassNames())
		{
			List<DetectedObj> listObj = getDetectedObjByObjClassName(sObjClassName);
			lTotal += listObj.size();
		}
		return lTotal;
	}
	
	////
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(FRAME_ID, this.getFrame_id());
		json.put(FRAME_SOURCE, this.getFrame_source());
		json.put(FRAME_TIMESTAMP, this.getFrame_timestamp());
		
		JSONArray jArrDetectedObjs= new JSONArray();
		for(String sObjClassName : getObjClassNames())
		{
			JSONObject jsonObjClassName = new JSONObject();
			List<DetectedObj> objs = getDetectedObjByObjClassName(sObjClassName);
			JSONArray jArrObjs = new JSONArray();
			for(DetectedObj obj : objs)
			{
				JSONObject jsonObj = obj.toJson();
				jArrObjs.put(jsonObj);
			}
			jsonObjClassName.put(sObjClassName, jArrObjs);
			jArrDetectedObjs.put(jsonObjClassName);
		}
		json.put(FRAME_DETECTED_OBJS, jArrDetectedObjs);
		
		return json;
	}
	
	//////////////////////////
	//////////////////////////
	//////////////////////////
	
	private List<DetectedObj> getDetectedObjList(DetectedObj obj)
	{
		String sObjClassName = obj.getObj_classname();
		
		if(sObjClassName==null || sObjClassName.trim().length()==0)
		{
			sObjClassName = "unknown";
			int iObjClassId = obj.getObj_classid();
			if(iObjClassId>-1)
			{
				sObjClassName = String.valueOf(iObjClassId);
			}
			obj.setObj_classname(sObjClassName);
		}
		//
		List<DetectedObj> listObj = this.mapDetectedObjs.get(sObjClassName);
		if(listObj==null)
			listObj = new ArrayList<>();
		//
		return listObj;
	}
	
}