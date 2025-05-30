package hl.objml2.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;

public class FrameDetectedObj {
	
	public final static String JSON_FRAME_ID				= "frame_id";
	public final static String JSON_FRAME_TIMESTAMP 		= "frame_timestamp";
	public final static String JSON_FRAME_SOURCE 			= "frame_source";
	public final static String JSON_FRAME_TOTAL_DETECTION 	= "frame_total_detection";
	//
	public final static String JSON_FRAME_DETECTIONS 		= "frame_detections";
	public final static String JSON_FRAME_DETECTION_META 	= "frame_detection_meta";
	//
	private long frame_id 				= -1;
	private long frame_timestamp_ms 	= -1;
	private String frame_source 		= null;
	private Mat mat_output_frameimg		= null;
	//
	private Map<String, List<DetectedObj>> mapDetectedObjs = new HashMap<>();
	//
	
	////	
	public long getFrame_id() {
		return frame_id;
	}
	public void setFrame_id(long frame_id) {
		this.frame_id = frame_id;
	}
	////
	public long getFrame_timestamp_ms() {
		return frame_timestamp_ms;
	}
	public void setFrame_timestamp_ms(long frame_timestamp) {
		this.frame_timestamp_ms = frame_timestamp;
	}
	////
	public String getFrame_source() {
		return frame_source;
	}
	public void setFrame_source(String frame_source) {
		this.frame_source = frame_source;
	}
	////
	
	public Mat getMat_output_frameimg() {
		return mat_output_frameimg;
	}
	public long getFrame_total_detection() {
		return getTotalDetectionCount();
	}

	public void setMat_output_frameimg(Mat mat_output_frameimg) {
		this.mat_output_frameimg = mat_output_frameimg;
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
	

	public List<DetectedObj> getAllDetectedObjs()
	{
		List<DetectedObj> listDetectedObj = new ArrayList<DetectedObj>(); 
		String[] sObjClassNames = this.getObjClassNames();
		for(String sObjClassName : sObjClassNames)
		{
			List<DetectedObj> listTmp = getDetectedObjByObjClassName(sObjClassName);
		
			listDetectedObj.addAll(listTmp);
		}
		
		return listDetectedObj;
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
	
	private long getTotalDetectionCount()
	{
		long lTotal = 0;
		for(String sObjClassName : getObjClassNames())
		{
			lTotal += getDetectionCount(sObjClassName);
		}
		return lTotal;
	}
	
	public long getDetectionCount(String aObjClassName)
	{
		List<DetectedObj> listObj = getDetectedObjByObjClassName(aObjClassName);
		if(listObj==null)
			return 0;
		return listObj.size();
	}
	
	////
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(JSON_FRAME_ID, this.getFrame_id());
		json.put(JSON_FRAME_SOURCE, this.getFrame_source());
		json.put(JSON_FRAME_TIMESTAMP, this.getFrame_timestamp_ms());
		json.put(JSON_FRAME_TOTAL_DETECTION, this.getFrame_total_detection());
		
		JSONObject jsonDetections = new JSONObject();
		for(String sObjClassName : getObjClassNames())
		{
			List<DetectedObj> objs = getDetectedObjByObjClassName(sObjClassName);
			JSONArray jArrObjs = new JSONArray();
			for(DetectedObj obj : objs)
			{
				JSONObject jsonObj = obj.toJson();
				jArrObjs.put(jsonObj);
			}
			jsonDetections.put(sObjClassName,jArrObjs);
		}
		json.put(JSON_FRAME_DETECTIONS, jsonDetections);
		
		return json;
	}
	

	protected FrameDetectedObj fromJson(JSONObject aJson)
	{
		FrameDetectedObj frame = null;
		
		long lFrameId = (aJson.optLong(FrameDetectedObj.JSON_FRAME_ID, -1));
		long lFrameTimestamp = (aJson.optLong(FrameDetectedObj.JSON_FRAME_TIMESTAMP, -1));
		String sFrameSource = (aJson.optString(FrameDetectedObj.JSON_FRAME_SOURCE, ""));
		
		frame = this;
		frame.setFrame_id(lFrameId);
		frame.setFrame_timestamp_ms(lFrameTimestamp);
		frame.setFrame_source(sFrameSource);
		
		JSONObject jsonDetections = aJson.optJSONObject(FrameDetectedObj.JSON_FRAME_DETECTIONS);
		if(jsonDetections!=null)
		{
			Iterator<String> iter = jsonDetections.keys();
			while(iter.hasNext())
			{
				String sObjClassName = iter.next();
				JSONArray jArrDetections = jsonDetections.optJSONArray(sObjClassName);
				for(int i=0; i<jArrDetections.length(); i++)
				{
					JSONObject jsonDetectedObj = jArrDetections.getJSONObject(i);
					DetectedObj obj = new DetectedObj(jsonDetectedObj);
					frame.addDetectedObj(obj);
				}
			}
		}
		
		return frame;
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