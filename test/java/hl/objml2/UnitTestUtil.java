package hl.objml2;

import java.io.File;
import hl.common.FileUtil;
import hl.objml2.plugin.MLPluginConfigKey;

public class UnitTestUtil  {
	
	protected static MLPluginConfigKey getCustomPluginConfigKey(String aPropFileName, String aPropPrefix)
	{
		MLPluginConfigKey customPluginConfig = new MLPluginConfigKey();
    	
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
    	System.out.println("Scanning "+aPluginFolder.getAbsolutePath()+" ... bundles discovered : "+fPluginJars.length);
    	System.out.println();
    	
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