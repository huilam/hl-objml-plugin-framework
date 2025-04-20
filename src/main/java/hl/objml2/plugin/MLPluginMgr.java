package hl.objml2.plugin;

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
import hl.objml2.plugin.base.PluginMgr;

public class MLPluginMgr extends PluginMgr {

	//
	protected static String PROPKEY_PLUGIN_SOURCE 	= "objml.mlmodel.source";
	
	private boolean isUnzipBundle 				= true;
	private MLPluginConfigKey pluginConfigKey 	= new MLPluginConfigKey();
	
	//
	public MLPluginMgr()
	{
		super();
		super.setCustomPluginMgr(MLPluginMgr.class);
	}

	//
	public void setCustomPluginConfigKey(MLPluginConfigKey aPluginConfigKey)
	{
		if(aPluginConfigKey==null)
		{
			this.pluginConfigKey = new MLPluginConfigKey();
		}
		else
		{
			this.pluginConfigKey = aPluginConfigKey;
		}
	}
	
	public void setPluginPropFileName(String aPropfileName)
	{
		this.pluginConfigKey.setProp_filename(aPropfileName);
	}
	
	///////////////
	
	public Map<String, Properties> scanForPluginJavaClassName()
	{
		return scanForPluginJavaClassName(this.pluginConfigKey.getProp_filename());
	}
	
	public Map<String, Properties> scanForPluginJavaClassName(String aPluginPropFileName)
	{
		return searchPluginClasses(
				super.classLoaderPlugin, super.listPluginSources, aPluginPropFileName);
	}
	
	public IObjDetectionPlugin getMLInstance(String aPluginClassName)
	{
		IObjDetectionPlugin plugin = (IObjDetectionPlugin) getPluginInstance(aPluginClassName);
		if(plugin!=null)
		{
			plugin.setPluginConfigKey(pluginConfigKey);
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
	
	private Map<String, Properties> searchPluginClasses(
			URLClassLoader aPluginClassLoader, List<File> aPluginSources, 
			final String aPluginPropFileName) 
	{
		Map<String, Properties> mapPluginClasses = new HashMap<String, Properties>();
		
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
				prop.put(PROPKEY_PLUGIN_SOURCE, f.getName());
				
				String sPlugClassName = getPluginClassNameFromProp(prop);
				if(sPlugClassName!=null)
				{
					mapPluginClasses.put(sPlugClassName, prop);
					System.out.println(" [+] "+sPlugClassName+" ("+f.getName()+")");
				}
			}
    	}
		return mapPluginClasses;
	}
	
	private String getPropImplClassNameKey()
	{
		return 
			this.pluginConfigKey.getPropkey_prefix() + 
			this.pluginConfigKey.getPropkey_pluginImplClassName();
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