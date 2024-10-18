package hl.objml2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import hl.common.FileUtil;
import hl.objml2.plugin.MLPluginConfig;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.objml2.plugin.test.BaseTester;
import hl.opencv.util.OpenCvUtil;

public class TestPlugins {
	
    public static void main(String main[]) throws Exception
    {
    	OpenCvUtil.initOpenCV();
    	
    	File pluginsFolder =  new File("./test/resources/plugins");
    	File[] fPluginJars =  FileUtil.getFilesWithExtensions(pluginsFolder, new String[]{".jar",".zip"});
    	
    	System.out.println();
    	System.out.println("plugin bundles discovered : "+fPluginJars.length);
    	
    	/*** Custom configuration for properties ***/
    	MLPluginConfig customPluginConfig = new MLPluginConfig();
    	customPluginConfig.setProp_filename("objml-plugin.properties");
    	customPluginConfig.setPropkey_prefix("objml.");
    	
    	/*** Init Plugin Mgr  ***/
    	MLPluginMgr mgr = new MLPluginMgr();
    	
    	/*** Init Custom Plugin Config  ***/
    	mgr.setCustomPluginConfig(customPluginConfig);
    	
    	/*** Register non-classpath bundle files  ***/
    	mgr.addPluginPaths(fPluginJars);
    	
    	/*** Perform javaClass scan for initial ***/
    	Map<String, String> mapPluginClassName = mgr.scanForPluginJavaClassName();
    	
    	/*** Optional Manual Register Java Class Name that in classpath ***/
    	//listPluginClassName.add(Upscale.class.getName());
	    
    	//////
    	File imgFolder = new File("./test/resources/images"); 
    	
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