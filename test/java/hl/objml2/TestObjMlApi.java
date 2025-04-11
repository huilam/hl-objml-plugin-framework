package hl.objml2;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;

import hl.objml2.api.ObjMLApi;
import hl.objml2.api.ObjMLInputParam;
import hl.objml2.common.DetectedObj;
import hl.objml2.common.FrameDetectedObj;
import hl.objml2.plugin.MLPluginConfig;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.util.OpenCvUtil;

public class TestObjMlApi {
	
	
	// *************************
	// *************************
    public static void main(String main[]) throws Exception
    {
    	OpenCvUtil.initOpenCV();
    	
    	MLPluginConfig pluginConfig = 
    			UnitTestUtil.getCustomPluginConfig("objml-plugin.properties", "objml.");

    	File[] pluginFolders = 
    			UnitTestUtil.getPluginJarsPath(new File("./test/plugins"));
    	
    	/*** Init Plugin Mgr  ***/
    	MLPluginMgr pluginMgr = new MLPluginMgr();
    	pluginMgr.setCustomPluginConfig(pluginConfig);
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
    		System.out.println("    - "+aPluginName);
    		ObjDetBasePlugin plugin = objmlApi.initPlugin(aPluginName);
    		
  	   		ObjMLInputParam inputParam = new ObjMLInputParam();
  	   		Mat matImputImage = null;
  	   		try {
		   		for(File fileTestImg : testImages)
	        	{
		   			System.out.println("    - "+fileTestImg.getName());
		   			matImputImage = OpenCvUtil.loadImage(fileTestImg.getAbsolutePath());
		   			inputParam.setInput_image(matImputImage);
		   			
	    	   		FrameDetectedObj result = objmlApi.detectFrame(plugin, inputParam);
	    	   		List<DetectedObj> listDetectedObj = result.getAllDetectedObjs();
	    	   		int i = 0;
	    	   		for(DetectedObj obj : listDetectedObj)
	    	   		{
	    	   			i++;
	    	   			System.out.println("	"+i+". "+obj.getObj_classname()+":"+obj.getObj_conf_score());
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