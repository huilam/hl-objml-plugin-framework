package hl.objml.opencv.objdetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import hl.common.PropUtil;
import hl.common.ZipUtil;
import hl.plugin.PluginMgr;
import hl.plugin.image.IMLDetectionPlugin;

public class MLPluginMgr extends PluginMgr {

	//
	private boolean isUnzipBundle 			= true;
	private MLPluginConfig pluginConfig 	= new MLPluginConfig();
	
	//
	public MLPluginMgr()
	{
		super();
		super.setCustomPluginMgr(MLPluginMgr.class);
	}

	//
	public void setCustomPluginConfig(MLPluginConfig aPluginConfig)
	{
		if(aPluginConfig==null)
		{
			this.pluginConfig = new MLPluginConfig();
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
	
	public IMLDetectionPlugin getDetectorInstance(String aPluginClassName)
	{
		IMLDetectionPlugin plugin = (IMLDetectionPlugin) getPluginInstance(aPluginClassName);
		if(plugin!=null)
		{
			plugin.setPluginConfig(pluginConfig);
			plugin.setPluginSource(getJavaClassSourcePath(plugin.getClass()));
		}
		return plugin;
	}
	
	public IMLDetectionPlugin getMLInstance(String aPluginClassName)
	{
		IMLDetectionPlugin plugin = (IMLDetectionPlugin) getPluginInstance(aPluginClassName);
		if(plugin!=null)
		{
			plugin.setPluginConfig(pluginConfig);
			plugin.setPluginSource(getJavaClassSourcePath(plugin.getClass()));
		}
		return plugin;
	}
	
	private String getJavaClassSourcePath(Class<?> aClass)
	{
		String sourcePath = null;
		ProtectionDomain pd = aClass.getProtectionDomain();
		if(pd!=null)
		{
			CodeSource cs = pd.getCodeSource();
			if(cs!=null)
			{
				try {
					sourcePath = URLDecoder.decode(cs.getLocation().getPath(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return sourcePath;
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