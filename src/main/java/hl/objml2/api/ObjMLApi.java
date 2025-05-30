package hl.objml2.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.objml2.common.FrameDetectedObj;
import hl.objml2.plugin.IObjDetectionPlugin;
import hl.objml2.plugin.MLPluginConfigProp;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;

public class ObjMLApi {
	
	private MLPluginMgr pluginMgr 						= null;
	private Map<String, MLPluginConfigProp> mapPlugins 	= null;
	
	public ObjMLApi()
	{
		initPluginMgr(new MLPluginMgr());
	}
	
	public ObjMLApi(MLPluginMgr aMLPluginMgr)
	{
		initPluginMgr(aMLPluginMgr);
	}
	
	private void initPluginMgr(MLPluginMgr aMLPluginMgr)
	{
		pluginMgr = aMLPluginMgr;
		reScanPlugins();
	}
	
	public void addPluginFolders(File[] aPluginFolders)
	{
		pluginMgr.addPluginPaths(aPluginFolders);
		reScanPlugins();
	}
	
	public void reScanPlugins()
	{
		mapPlugins = pluginMgr.scanForPluginJavaClassName();
	}
	
	public List<String> listPluginClassNames()
	{
		List<String> listPlugins = new ArrayList<String>();
		
		if(mapPlugins!=null && mapPlugins.size()>0)
		{
			listPlugins = new ArrayList<>(mapPlugins.keySet());
		}
		
		return listPlugins;
	}
	
	public Properties getPluginProps(String aJavaClassName)
	{
		Properties prop = null;
		if(mapPlugins!=null)
		{
			prop = mapPlugins.get(aJavaClassName);
		}
		
		return prop;
	}
	
	public MLPluginFrameOutput detectFrame(IObjDetectionPlugin aIObjDetPlugin, ObjMLInputParam aObjMlInput)
	{
		return doDetection(aIObjDetPlugin, aObjMlInput);
	}
	
	public FrameDetectedObj initAndDetectFrame(String aPluginClassName, ObjMLInputParam aObjMlInput)
	{
		MLPluginFrameOutput frameOutput = doDetection(aPluginClassName, aObjMlInput);
		if(frameOutput!=null)
		{
			return frameOutput.getFrameDetectedObj();
		}
		return null;
	}
	
	public ObjDetBasePlugin initPlugin(String aPluginClassName)
	{
		ObjDetBasePlugin plugin = null;
		
		if(aPluginClassName.trim().length()>0)
		{
			plugin = (ObjDetBasePlugin) pluginMgr.getMLInstance(aPluginClassName);
			if(plugin!=null)
			{
				if(!plugin.isPluginOK())
				{
					plugin = null;
				}
			}
		}
		return plugin;
	}
	//=========================================================
	
	private MLPluginFrameOutput doDetection(String aPluginClassName, ObjMLInputParam aObjMlInput)
	{
		ObjDetBasePlugin plugin = initPlugin(aPluginClassName);
		if(plugin!=null)
		{
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
			
			plugin.setConfidenceThreshold_Override(aObjMlInput.getConfidenceThreshold());
			plugin.setNMSThreshold_Override(aObjMlInput.getNmsThreshold());
			plugin.setDnnBackend_Override(aObjMlInput.getPrefferedDnnBackend());
			plugin.setDnnTarget_Override(aObjMlInput.getPrefferedDnnTarget());
			
			if(aObjMlInput.getObjOfInterest().length()>0)
			{
				plugin.setObjClassesOfInterest(aObjMlInput.getObjOfInterest().split("[\n\\,]"));
			}
			
			JSONObject jsonMLConfig = null;
			
			frameOutput = plugin.detect(matInputImg, jsonMLConfig);
			
		}
		return frameOutput;
	}
	
}