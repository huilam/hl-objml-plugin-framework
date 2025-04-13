package hl.objml2;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import hl.objml2.plugin.MLPluginConfig;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.objml2.plugin.test.BaseTester;
import hl.opencv.util.OpenCvUtil;

public class TestPluginMgr {
	
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
    	MLPluginMgr mgr = new MLPluginMgr();
    	
       	/*** Init Custom Plugin Config  ***/
    	mgr.setCustomPluginConfig(pluginConfig);
 
    	/*** Register non-classpath bundle files  ***/
    	mgr.addPluginPaths(pluginFolders);
    	
    	/*** Perform javaClass scan for initial ***/
    	Map<String, Properties> mapPluginClassName = mgr.scanForPluginJavaClassName();
    	
    	/*** Optional Manual Register Java Class Name that in classpath ***/
    	//listPluginClassName.add(Upscale.class.getName());
	    
    	//////
    	File imgFolder = new File("./test/images"); 
    	
		BaseTester test = new BaseTester();
		test.setTestImageFolder(imgFolder.getAbsolutePath());
		test.setOutputImageExtension("png");
		//////
		
		System.out.println();
		int iCount = 1;
    	for(String sPluginClassName : mapPluginClassName.keySet())
    	{
    		System.out.println();
    		System.out.print("#"+(iCount++));
 			/*** Get plugin instance ***/
    		ObjDetBasePlugin mlplugin = (ObjDetBasePlugin) mgr.getMLInstance(sPluginClassName);
	    	
    		/*** Perform plugin unit-test ***/
    		test.testDetector(mlplugin);
    	}
	
    }
    
}