package hl.objml.opencv.objdetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import hl.common.PropUtil;
import hl.common.ZipUtil;
import hl.plugin.PluginMgr;

public class DetectorPluginMgr extends PluginMgr {

	//
	private boolean isUnzipBundle 				= false;
	private DetectorPluginConfig pluginConfig 	= new DetectorPluginConfig();
	
	//
	public DetectorPluginMgr()
	{
		super();
		super.setCustomPluginMgr(DetectorPluginMgr.class);
	}

	//
	public void setCustomPluginConfig(DetectorPluginConfig aPluginConfig)
	{
		if(aPluginConfig==null)
		{
			this.pluginConfig = new DetectorPluginConfig();
		}
		else
		{
			this.pluginConfig = aPluginConfig;
		}
	}
	
	public void setPluginPropFileName(String aPropfileName)
	{
		this.pluginConfig.setProp_filename(aPropfileName);
	}
	
	///////////////
	
	public Map<String, String> scanForPluginJavaClassName()
	{
		return scanForPluginJavaClassName(this.pluginConfig.getProp_filename());
	}
	
	public Map<String, String> scanForPluginJavaClassName(String aPluginPropFileName)
	{
		return searchPluginClasses(
				super.classLoaderPlugin, super.listPluginSources, aPluginPropFileName);
	}
	
	public IImgDetectorPlugin getDetectorInstance(String aPluginClassName)
	{
		IImgDetectorPlugin plugin = (IImgDetectorPlugin) getPluginInstance(aPluginClassName);
		plugin.setPluginConfig(pluginConfig);
		return plugin;
		
		
	}
	
	/////////////////////////////////////////////////////////////////////
	
	private Map<String, String> searchPluginClasses(
			URLClassLoader aPluginClassLoader, List<File> aPluginSources, 
			final String aPluginPropFileName) 
	{
		Map<String, String> mapPluginClasses = new HashMap<String, String>();
		
		for(File f : aPluginSources)
    	{
			Properties prop = null;
			//zip or jar file
			if(f.isFile())
			{
				boolean isUnzip = false;
				
	    		ZipInputStream jar = null;
	    		
	    		try {
		    		jar = new ZipInputStream(new FileInputStream(f));
					ZipEntry entry = null;
					while((entry = jar.getNextEntry())!=null)
					{
						if(entry.getName().toLowerCase().indexOf("/"+aPluginPropFileName)>-1)
						{
							if(isUnzipBundle)
							{
								isUnzip = true;
								break;
							}
							prop = new Properties();
							prop.load(aPluginClassLoader.getResourceAsStream(entry.getName()));
						}
					}
	    		} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally
	    		{
					try {
						if(jar!=null)
							jar.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    		
	    		if(isUnzip)
	    		{
	    			File unzipFolder = new File(f.getAbsoluteFile()+".dir");
	    			
	    			if(!unzipFolder.exists())
	    			{
		    			unzipFolder.mkdirs();
		    			try {
							ZipUtil.unZip(f.getAbsolutePath(), unzipFolder.getAbsolutePath());
							f = unzipFolder;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
	    			else
	    			{
	    				f = unzipFolder;
	    			}
	    		}
			}
			
			
			if(f.isDirectory())
			{
	    	    //classpath 
				File fileProp = searchAllSubFolders(f, aPluginPropFileName);
				if(fileProp!=null)
				{
					try {
						prop = PropUtil.loadProperties(fileProp.getAbsolutePath());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			if(prop!=null && prop.size()>0)
			{
				String sPlugClassName = getPluginClassNameFromProp(prop);
				if(sPlugClassName!=null)
				{
					mapPluginClasses.put(sPlugClassName, f.getAbsolutePath());
					System.out.println(" [+] "+sPlugClassName+" ("+f.getName()+")");
				}
			}
    	}
		return mapPluginClasses;
	}
	
	private String getPropImplClassNameKey()
	{
		return 
			this.pluginConfig.getPropkey_prefix() + 
			this.pluginConfig.getPropkey_pluginImplClassName();
	}
	
	private String getPluginClassNameFromProp(Properties prop)
	{
		String sPlugClassName = prop.getProperty(getPropImplClassNameKey());
		
		try {
			
			Class.forName(sPlugClassName, false, classLoaderPlugin);
			//try init class
			Class<?> classPlugin = classLoaderPlugin.loadClass(sPlugClassName);
			
			if(classPlugin!=null)
				return sPlugClassName;
			
		} catch (ClassNotFoundException e) {
			// ignore
			e.printStackTrace();
		}
		return null;
	}
	
}