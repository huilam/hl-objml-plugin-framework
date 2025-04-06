package hl.objml.api;

import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.objml2.common.FrameDetectedObj;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;

public class ObjMLApi {
	
	private MLPluginMgr pluginMge = new MLPluginMgr();
	
	public FrameDetectedObj detectFrame(String aPluginName, ObjMLInputParam aObjMlInput)
	{
		FrameDetectedObj frameObj = null;
		
		MLPluginFrameOutput frameOutput = doDetection(aPluginName, aObjMlInput);
		if(frameOutput!=null)
		{
			frameObj = frameOutput.getFrameDetectedObj();
		}
		return frameObj;
	}
	
	
	private MLPluginFrameOutput doDetection(String aPluginName, ObjMLInputParam aObjMlInput)
	{
		MLPluginFrameOutput frameOutput = null;
	
		ObjDetBasePlugin plugin = (ObjDetBasePlugin) pluginMge.getMLInstance(aPluginName);
		if(plugin!=null)
		{
			Mat matInputImg = aObjMlInput.getInput_image();
			JSONObject jsonMLConfig = null;
			
			Map<String, Object> outputMap = plugin.detect(matInputImg, jsonMLConfig);
			if(outputMap!=null)
			{
				frameOutput = new MLPluginFrameOutput(outputMap);
			}
		}
		return frameOutput;
	}
	
}