package hl.objml.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.objml2.common.FrameDetectedObj;
import hl.objml2.plugin.IObjDetectionPlugin;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;

public class ObjMLApi {
	
	private MLPluginMgr pluginMgr 			= null;
	private Map<String, String> mapPlugins 	= null;
	
	public ObjMLApi()
	{
		pluginMgr = new MLPluginMgr();
		reScanPlugins();
	}
	
	public void reScanPlugins()
	{
		mapPlugins = pluginMgr.scanForPluginJavaClassName();
	}
	
	public List<String> listPlugins()
	{
		List<String> listPlugins = new ArrayList<String>();
		
		if(mapPlugins!=null && mapPlugins.size()>0)
		{
			listPlugins = new ArrayList<>(mapPlugins.keySet());
		}
		
		return listPlugins;
	}
	
	public FrameDetectedObj detectFrame(IObjDetectionPlugin aIObjDetPlugin, ObjMLInputParam aObjMlInput)
	{
		MLPluginFrameOutput frameOutput = doDetection(aIObjDetPlugin, aObjMlInput);
		if(frameOutput!=null)
		{
			return frameOutput.getFrameDetectedObj();
		}
		return null;
	}
	
	public FrameDetectedObj detectFrame(String aPluginName, ObjMLInputParam aObjMlInput)
	{
		MLPluginFrameOutput frameOutput = doDetection(aPluginName, aObjMlInput);
		if(frameOutput!=null)
		{
			return frameOutput.getFrameDetectedObj();
		}
		return null;
	}
	
	private MLPluginFrameOutput doDetection(String aPluginName, ObjMLInputParam aObjMlInput)
	{
		if(aPluginName.trim().length()>0)
		{
			ObjDetBasePlugin plugin = (ObjDetBasePlugin) pluginMgr.getMLInstance(aPluginName);
			return doDetection(plugin, aObjMlInput);
		}
		return null;
	}
	
	private MLPluginFrameOutput doDetection(IObjDetectionPlugin aIObjDetPlugin, ObjMLInputParam aObjMlInput)
	{
		MLPluginFrameOutput frameOutput = null;
	
		ObjDetBasePlugin plugin = (ObjDetBasePlugin) aIObjDetPlugin;
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