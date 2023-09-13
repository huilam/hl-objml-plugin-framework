package hl.objml.opencv.objdetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import hl.common.PropUtil;
import hl.objml.opencv.TestPlugins;

public class DetectorPluginMgr {
	
	private static final String DEF_PLUGIN_PROP_FILENAME 	= "objml-plugin.properties";
	private static String DEF_PLUGIN_PROPKEY_PREFIX 		= "objml.";
	
	private static String _PLUGIN_PROP_FILENAME 			= DEF_PLUGIN_PROP_FILENAME;
	private static String _PLUGIN_PROPKEY_IMPL_CLASSNAME_ 	= "mlmodel.detection.implementation.classname";
	
	private static Object objLock 				= new Object();
	private static DetectorPluginMgr instance 	= null;
	
	private URLClassLoader classLoaderPlugin 	= null;
	private List<File> listPluginSources 		= new ArrayList<File>();
	
	//
	private DetectorPluginMgr()
	{
	}
	
	public static DetectorPluginMgr getInstance()
	{
		synchronized (objLock)
		{
			if(instance==null)
			{
				instance = new DetectorPluginMgr();
			}
		}
		return instance;
	}
	
	public void setPluginPropFileName(String aPropfileName)
	{
		this._PLUGIN_PROP_FILENAME = aPropfileName;
	}
	
	public void setPluginPropKeyPrefix(String aPropPrefix)
	{
		this.DEF_PLUGIN_PROPKEY_PREFIX = aPropPrefix;
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
				if(sFileName.endsWith(".jar") || sFileName.endsWith(".zip"))
				{
					this.listPluginSources.add(f);
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
	
	public List<String> scanForPluginJavaClassName()
	{
		return searchPluginClasses(this.classLoaderPlugin, this.listPluginSources);
	}
	
	public IImgDetectorPlugin getDetectorInstance(String aPluginClassName)
	{
		IImgDetectorPlugin plugin = null;
		try {
			Class<?> classDetector = this.classLoaderPlugin.loadClass(aPluginClassName);
	    	Constructor<?> constructor = classDetector.getDeclaredConstructor();
	    	plugin = (IImgDetectorPlugin) constructor.newInstance();
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
    			TestPlugins.class.getClassLoader());
    	//
	}
	
	private List<String> searchPluginClasses(URLClassLoader aPluginClassLoader, List<File> aPluginSources) 
	{
		List<String> listPluginClasses = new ArrayList<String>();
		
		for(File f : aPluginSources)
    	{
			Properties prop = null;
			//zip or jar file
			if(f.isFile())
			{
	    		ZipInputStream jar = null;
	    		
	    		try {
		    		jar = new ZipInputStream(new FileInputStream(f));
					ZipEntry entry = null;
					while((entry = jar.getNextEntry())!=null)
					{
						if(entry.getName().toLowerCase().indexOf("/"+_PLUGIN_PROP_FILENAME)>-1)
						{
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
			}
			else
			{
	    	    //classpath 
				File fileProp = searchAllSubFolders(f, _PLUGIN_PROP_FILENAME);
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
					listPluginClasses.add(sPlugClassName);
					System.out.println(" [+] "+sPlugClassName);
				}
			}
    	}
		return listPluginClasses;
	}
	
	private String getPropImplClassNameKey()
	{
		return DEF_PLUGIN_PROPKEY_PREFIX + _PLUGIN_PROPKEY_IMPL_CLASSNAME_;
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
	
	private File searchAllSubFolders(File aFolder, String aTargetFileName)
	{
		File fileTarget = null;
		
		for(File f: aFolder.listFiles())
		{
			if(f.isDirectory())
			{
				fileTarget = searchAllSubFolders(f, aTargetFileName);
				if(fileTarget!=null)
					return fileTarget;
			}
			else
			{
				if(f.getName().equalsIgnoreCase(aTargetFileName))
				{
					return f;
				}
			}
		}
		
		
		return fileTarget;
	}
    
}