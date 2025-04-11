package hl.objml2;

import java.io.File;
import java.util.Map;
import hl.common.FileUtil;
import hl.objml2.plugin.MLPluginConfig;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.objml2.plugin.test.BaseTester;
import hl.opencv.util.OpenCvUtil;

public class TestPluginMgr {
	
	private static MLPluginConfig getCustomPluginConfig(String aPropFileName, String aPropPrefix)
	{
    	MLPluginConfig customPluginConfig = new MLPluginConfig();
    	
		/*** Custom configuration for properties ***/
    	customPluginConfig.setProp_filename(aPropFileName);
    	customPluginConfig.setPropkey_prefix(aPropPrefix);
    	
    	return customPluginConfig;
	}
	
	private static File[] getPluginJarsPath(File aPluginFolder)
	{
		File[] fPluginJars = new File[]{};
		
		if(aPluginFolder!=null && aPluginFolder.isDirectory())
		{
			fPluginJars =  FileUtil.getFilesWithExtensions(aPluginFolder, new String[]{".jar",".zip"});
    	
		}
    	System.out.println();
    	System.out.println("plugin bundles discovered : "+fPluginJars.length);
    	
    	return fPluginJars;
	}
	
    public static void main(String main[]) throws Exception
    {
    	OpenCvUtil.initOpenCV();
    	
    	/*** Init Plugin Mgr  ***/
    	MLPluginMgr mgr = new MLPluginMgr();
    	
    	/*** Register non-classpath bundle files  ***/
    	mgr.addPluginPaths(getPluginJarsPath(new File("./test/plugins")));
    	
    	/*** Init Custom Plugin Config  ***/
    	mgr.setCustomPluginConfig(getCustomPluginConfig("objml-plugin.properties", "objml."));
    	
    	/*** Perform javaClass scan for initial ***/
    	Map<String, String> mapPluginClassName = mgr.scanForPluginJavaClassName();
    	
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