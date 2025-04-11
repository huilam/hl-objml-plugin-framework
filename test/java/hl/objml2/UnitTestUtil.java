package hl.objml2;

import java.io.File;
import hl.common.FileUtil;
import hl.objml2.plugin.MLPluginConfig;

public class UnitTestUtil  {
	
	protected static MLPluginConfig getCustomPluginConfig(String aPropFileName, String aPropPrefix)
	{
    	MLPluginConfig customPluginConfig = new MLPluginConfig();
    	
		/*** Custom configuration for properties ***/
    	customPluginConfig.setProp_filename(aPropFileName);
    	customPluginConfig.setPropkey_prefix(aPropPrefix);
    	
    	return customPluginConfig;
	}

	protected static File[] getPluginJarsPath(File aPluginFolder)
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
	
	protected static File[] getTestImageFiles(String aFolder, String[] aImgExts)
	{
		File folderImages = new File(aFolder);
		
		if(folderImages.isDirectory())
		{
			return FileUtil.getFilesWithExtensions(
					folderImages, 
					aImgExts);
		}
		else
		{
			return new File[] {};
		}
	}
    
}