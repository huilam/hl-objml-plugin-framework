package hl.objml2.plugin;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.objml2.common.FrameDetectedObj;
import hl.objml2.common.FrameDetectionMeta;

public class MLPluginFrameOutput  {
	
	private Map<String,Object> map_plugin_output = null;
	private JSONObject jsonErrors = new JSONObject();
	
	//=============
	
	public MLPluginFrameOutput()
	{
		map_plugin_output = new HashMap<String,Object>();
	}
	
	public MLPluginFrameOutput(Map<String,Object> aPluginOutput)
	{
		map_plugin_output = aPluginOutput;
	}
	
	public Map<String,Object> getDetectionOutputMap()
	{
		return map_plugin_output;
	}
	
	public void clear()
	{
		map_plugin_output.clear();
		clearErrors();
	}
	//=============
	
	public void clearErrors()
	{
		if(jsonErrors!=null)
			jsonErrors.clear();
	}
	
	public boolean addError(String aErrCat, String aErrMsg)
	{
		if(jsonErrors==null)
			jsonErrors = new JSONObject();
		
		JSONArray jArrErrCat = jsonErrors.optJSONArray(aErrCat, null);
		if(jArrErrCat==null)
			jArrErrCat = new JSONArray();
		jArrErrCat.put(aErrMsg);
		jsonErrors.put(aErrCat, jArrErrCat);
		return true;
	}
	
	public JSONObject getErrorsJson()
	{
		return jsonErrors;
	}
	//=============
	public void setFrameDetectedObj(FrameDetectedObj aFrameObj)
	{
		if(aFrameObj!=null)
		{
			map_plugin_output.put(IObjDetectionPlugin._KEY_OUTPUT_FRAME_DETECTIONS, aFrameObj);
		}
		
	}
	public FrameDetectedObj getFrameDetectedObj()
	{
		FrameDetectedObj frameObj = null;
		
		if(map_plugin_output!=null)
		{
			frameObj = (FrameDetectedObj) map_plugin_output.getOrDefault(
					IObjDetectionPlugin._KEY_OUTPUT_FRAME_DETECTIONS, null);
		}
		
		if(frameObj==null)
			 frameObj = new FrameDetectedObj();
		
		return frameObj;
	}
	//=============
	public void setAnnotatedFrameImage(Mat aFrameImg)
	{
		if(aFrameImg!=null)
		{
			map_plugin_output.put(IObjDetectionPlugin._KEY_OUTPUT_FRAME_ANNOTATED_IMG, aFrameImg);
		}
		
	}
	public Mat getAnnotatedFrameImage()
	{
		Mat matImg = null;
		
		if(map_plugin_output!=null)
		{
			matImg = (Mat) map_plugin_output.getOrDefault(
					IObjDetectionPlugin._KEY_OUTPUT_FRAME_ANNOTATED_IMG, null);
		}
		
		return matImg;
	}
	//=============
	public void setFrameDetectionMeta(FrameDetectionMeta aFrameDetectionMeta)
	{
		if(aFrameDetectionMeta!=null)
		{
			map_plugin_output.put(IObjDetectionPlugin._KEY_OUTPUT_FRAME_DETECTION_META, aFrameDetectionMeta);
		}
		
	}
	public FrameDetectionMeta getFrameDetectionMeta()
	{
		FrameDetectionMeta meta = null;
		
		if(map_plugin_output!=null)
		{
			meta = (FrameDetectionMeta) map_plugin_output.getOrDefault(
					IObjDetectionPlugin._KEY_OUTPUT_FRAME_DETECTION_META, null);
		}
		
		if(meta==null)
			meta = new FrameDetectionMeta();
		
		return meta;
	}
	//
}