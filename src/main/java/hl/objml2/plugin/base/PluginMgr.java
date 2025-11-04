package hl.objml2.plugin.base;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class PluginMgr {

	//
	private static Object objLock 				= new Object();
	private static PluginMgr instance 			= null;
	private Class<?> classPluginMgr 			= PluginMgr.class;
	//
	protected URLClassLoader classLoaderPlugin 	= null;
	protected List<File> listPluginSources 		= new ArrayList<File>();
	
	protected static String[] DEF_PLUGIN_BUNDLE_EXT = new String[] {"jar", "zip"};
	protected String[] pluginBundleExtensions 		= DEF_PLUGIN_BUNDLE_EXT;
	
	public static String Verson = "1.0";
	//
	public PluginMgr()
	{
		classLoaderPlugin = initPluginsClassLoader(new File[]{});
	}
	
	protected static PluginMgr getInstance()
	{
		synchronized (objLock)
		{
			if(instance==null)
			{
				instance = new PluginMgr();
				instance.classLoaderPlugin = instance.initPluginsClassLoader(new File[]{});
			}
		}
		return instance;
	}
	
	public void setCustomPluginMgr(Class<?> aPluginMgrClass)
	{
		this.classPluginMgr = aPluginMgrClass;
	}
	
	protected void setPluginBundleExtension(String[] aBundleExts)
	{
		if(aBundleExts==null)
		{
			this.pluginBundleExtensions = DEF_PLUGIN_BUNDLE_EXT;
		}
		else
		{
			this.pluginBundleExtensions = aBundleExts;
		}
	}
	
	public void addPluginPath(File aPluginSource)
	{
		addPluginPaths(new File[] {aPluginSource});
	}
	
	public void addPluginPaths(File[] aPluginSource)
	{
		for(File f : aPluginSource)
		{
			if(f.isFile())
			{
				String sFileName = f.getName().toLowerCase();
				
				for(String sExt : pluginBundleExtensions)
				{
					if(sFileName.endsWith(sExt))
					{
						this.listPluginSources.add(f);
					}
				}
			}
			else if(f.isDirectory())
			{
				for(File f2 : f.listFiles())
				{
					//First subfolders of the "classpath"
					if(f2.isDirectory())
					{
						this.listPluginSources.add(f2);
					}
				}
			}
		}
		
		File[] pluginPaths = listPluginSources.toArray(new File[listPluginSources.size()]);
		
		this.classLoaderPlugin = initPluginsClassLoader(pluginPaths);
	}
	
	public void clearPluginPaths()
	{
		this.listPluginSources.clear();
	}
	
	protected Object getPluginInstance(String aPluginClassName)
	{
		Object plugin = null;
		try {
			Class<?> classDetector = this.classLoaderPlugin.loadClass(aPluginClassName);
	    	Constructor<?> constructor = classDetector.getDeclaredConstructor();
	    	plugin = constructor.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return plugin;
	}
	
	/////////////////////////////////////////////////////////////////////
	private URLClassLoader initPluginsClassLoader(File[] aPluginSources)
	{
		//
    	List<URL> listSourceURL = new ArrayList<URL>();
    	for(File f : aPluginSources)
    	{
    		try {
    			listSourceURL.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				//ignore
				e.printStackTrace();
			}
    	}
    	return new URLClassLoader(
    			listSourceURL.toArray(new URL[listSourceURL.size()]),
    			classPluginMgr.getClassLoader());
    	//
	}
	
	protected List<File> searchAllSubFolders(File aFolder, String aTargetFileName)
	{
		List<File> listFileTarget = new ArrayList<>();
		
		for(File f: aFolder.listFiles())
		{
			if(f.isDirectory())
			{
				listFileTarget.addAll(searchAllSubFolders(f, aTargetFileName));
			}
			else
			{
				if(f.getName().equalsIgnoreCase(aTargetFileName))
				{
					listFileTarget.add(f);
				}
			}
		}
		
		return listFileTarget;
	}
    
}