package hl.objml2;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Mat;

import hl.objml2.api.ObjMLApi;
import hl.objml2.api.ObjMLInputParam;
import hl.objml2.common.DetectedObj;
import hl.objml2.common.FrameDetectedObj;
import hl.objml2.common.FrameDetectionMeta;
import hl.objml2.plugin.MLPluginConfigKey;
import hl.objml2.plugin.MLPluginConfigProp;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.util.OpenCvUtil;

public class TestObjMlApi {
	
	
	// *************************
	// *************************
    public static void main(String main[]) throws Exception
    {
    	OpenCvUtil.initOpenCV();
    	
    	MLPluginConfigKey pluginConfigKey = 
    			UnitTestUtil.getCustomPluginConfigKey("objml-plugin.properties", "objml.");

    	File[] pluginFolders = 
    			UnitTestUtil.getPluginJarsPath(new File("./test/plugins"));
    	
    	/*** Init Plugin Mgr  ***/
    	MLPluginMgr pluginMgr = new MLPluginMgr();
    	pluginMgr.setCustomPluginConfigKey(pluginConfigKey);
    	pluginMgr.addPluginPaths(pluginFolders);
    	
       	/** List all available test images **/
    	File[] testImages = UnitTestUtil.getTestImageFiles("./test/images", new String[] {"png","JPG"});
    	System.out.println("Available Test Images : "+testImages.length);
 
    	/** List all available plugins **/
    	ObjMLApi objmlApi = new ObjMLApi(pluginMgr);
    	List<String> listPluginNames = objmlApi.listPluginClassNames();
    	System.out.println("Available Plugins : "+listPluginNames.size());
    	for(String aPluginName : listPluginNames)
    	{
    		ObjDetBasePlugin plugin = objmlApi.initPlugin(aPluginName);
    		MLPluginConfigProp propPlugin 	=  plugin.getPluginProps();
    		
    		System.out.println("    - "+propPlugin.getMlModelName()+" ("+propPlugin.getMlModelLicense()+")");
    		System.out.println("    - Source :"+propPlugin.getMlModelSource());
    		System.out.println("    - ConfidenceScore :"+propPlugin.getMlModelConfidenceScore());
    		System.out.println("    - NmsScore :"+propPlugin.getMlModelNmsScore());
    		String sObjLabels = propPlugin.getMlModelSupportedLabels();
    		if(sObjLabels!=null)
    		{
    			sObjLabels = sObjLabels.replace("\n", ",");
    		}
    		System.out.println("    - ObjLabels :"+sObjLabels);
    		System.out.println("    - DnnBackend :"+propPlugin.getDnnBackend());
    		System.out.println("    - DnnTarget :"+propPlugin.getDnnTarget());
 
    		
  	   		ObjMLInputParam inputParam = new ObjMLInputParam();
  	   		Mat matImputImage = null;
  	   		try {
		   		for(File fileTestImg : testImages)
	        	{
		   			System.out.println("        - Test Image ="+fileTestImg.getName());
		   			matImputImage = OpenCvUtil.loadImage(fileTestImg.getAbsolutePath());
		   			inputParam.setInput_image(matImputImage);
		   			inputParam.setConfidenceThreshold(0.1);
		   			
		   			MLPluginFrameOutput result = objmlApi.detectFrame(plugin, inputParam);

		   			FrameDetectionMeta meta = result.getFrameDetectionMeta();
System.out.println("meta="+meta);
		   			
		   			FrameDetectedObj frameObjs = result.getFrameDetectedObj();
	    	   		List<DetectedObj> listDetectedObj = frameObjs.getAllDetectedObjs();
	    	   		int i = 0;
	    	   		for(DetectedObj obj : listDetectedObj)
	    	   		{
	    	   			i++;
	    	   			System.out.println("	    "+i+". "+obj.getObj_classname()+":"+obj.getObj_conf_score());
	    	   		}
	    	   		
	        	}
  	   		}finally
  	   		{
  	   			if(matImputImage!=null)
  	   				matImputImage.release();
  	   		}
    	   	System.out.println();
    	}
    	
    }
    
    
}