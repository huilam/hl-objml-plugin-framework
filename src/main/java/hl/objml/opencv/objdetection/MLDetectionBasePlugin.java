package hl.objml.opencv.objdetection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import hl.common.PropUtil;
import hl.opencv.util.OpenCvUtil;

public class MLDetectionBasePlugin {
	
	//
	protected MLPluginConfig pluginConfig = new MLPluginConfig();
	protected Class<?> thisclass 		= null;
	protected Properties props_model 	= null;
	protected String _model_filename 	= null;
	protected String _plugin_source 	= null;
	
	public void setPluginConfig(MLPluginConfig aPluginConfig)
	{
		this.pluginConfig = aPluginConfig;
	}
	
	public void setPluginSource(String aPluginSource)
	{
		this._plugin_source = aPluginSource;
	}
	
	protected boolean isPluginOK(Class<?> aClass)
	{
		return isPluginOK(aClass, this.pluginConfig.getProp_filename());
	}
	
	public boolean isValidateMLFileLoading()
	{
		return true;
	}
	
	protected boolean isPluginOK(Class<?> aClass, String aPropFileName)
	{
		if(aClass==null)
			return false;
		
		OpenCvUtil.initOpenCV();
		
		thisclass = aClass;
		
		props_model = getPluginPropsByFileName(aPropFileName);
		if(props_model!=null)
		{
			props_model = updatePluginProps(props_model);
			
			_model_filename = props_model.getProperty(getPropModelDetectFileName());
			
			if(isValidateMLFileLoading())
			{
				//Because OpenCv DNN can only accept extracted file location
				File fModelFile = null;
				fModelFile = getMLModelFile(_model_filename);
				
				if(fModelFile!=null && fModelFile.exists())
				{
					String sModelName = getModelName();
					if(sModelName!=null && sModelName.trim().length()>0)
					{
						_model_filename = fModelFile.getAbsolutePath();
						return true;
					}
				}
				
				if(fModelFile==null || !fModelFile.exists())
				{
					System.err.println("Failed to load MLModel - "+_model_filename);
				}
				
				fModelFile = null;
			}
		}
		
		
		return false;
	}
	
	//Override this method to add/remove/update properties
	protected Properties updatePluginProps(Properties aProperties)
	{
		return aProperties;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	protected String getPropModelDetectFileName()
	{
		return
			this.pluginConfig.getPropkey_prefix()
			+ this.pluginConfig.getPropkey_pluginMLModelDetectFileName();
	}
	
	protected String getPropModelName()
	{
		return
			this.pluginConfig.getPropkey_prefix()
			+ this.pluginConfig.getPropkey_pluginMLModelName();
	}
	
	protected File getMLModelFile(String aModelFilePath)
	{
		File fileMlModel = new File(aModelFilePath);
		if(!fileMlModel.isFile())
		{
			//try to search as file
			fileMlModel = new File(getResPath()+"/"+aModelFilePath);
		}
		
		if(!fileMlModel.isFile())
		{
			//look into resources
			File fileTmp = extractModelFileFromJarAsTemp(fileMlModel);
			
			if(fileTmp!=null && fileTmp.isFile())
			{
				_model_filename = fileTmp.getAbsolutePath();
				return fileTmp;
			}
		}
		
		return fileMlModel;
	}
	
	private String searchResource(File aResFile)
	{
		String sResPath = aResFile.getAbsolutePath().replace("\\", "//");
		
		String[] sResAttempt = new String[]{sResPath, "/"+sResPath, aResFile.getName()};
		
		for(String sResName : sResAttempt)
		{
			URL urlRes = thisclass.getResource(sResName);
			if(urlRes!=null)
			{
				return sResName;
			}
		}
		return null;
	}
	
	private File extractModelFileFromJarAsTemp(File file)
	{	
		//try load from jar/zip
		String sResFileName = searchResource(file);
		
		if(sResFileName==null)
		{
			System.err.println("Failed to locale resource -"+file.getAbsolutePath());
			return null;
		}
		
		List<File> listFiles = new ArrayList<File>();
		File fileCache = null;
		
		if(file.isDirectory())
		{
			listFiles.addAll(Arrays.asList(file.listFiles()));

			try {
				File fileTmp = File.createTempFile(file.getName()+"/empty", ".tmp");
				
				//Folder
				fileCache = fileTmp.getParentFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else 
		{
			//single file
			listFiles.add(file);
		}
			
		InputStream in 		= null;
		try {
			long lCopiedBytes = 0;
			long lTotalCopied = 0;
			for(File f : listFiles)
			{
				File folderPluginTmp  = new File(".");
				String sPluginSrcPath = getPluginSource();
				if(sPluginSrcPath!=null)
				{
					folderPluginTmp = new File(sPluginSrcPath+".dir");
				}
				
				File fileTmp = null;
				if(folderPluginTmp.exists() && folderPluginTmp.list().length>0)
				{
					fileTmp = new File(folderPluginTmp.getAbsolutePath()+"/"+sResFileName);
					fileTmp.mkdirs();
				}
				
				
				if(fileTmp==null)
				{
					String sFileExt 	= "";
					String sFileName 	= f.getName();
					int iExtPos 		= sFileName.lastIndexOf('.');
					if(iExtPos>=0)
					{
						sFileExt = sFileName.substring(iExtPos);
						sFileName = sFileName.substring(0, iExtPos);
					}
					fileTmp = File.createTempFile(sFileName, sFileExt);
				}
				
				//System.out.println("Extract - "+sFileName);
				
				if(in!=null)
					in.close();
				
				in = thisclass.getResourceAsStream(sResFileName);

				Path pathTmp = Paths.get(fileTmp.getAbsolutePath());
				lCopiedBytes += Files.copy(in, pathTmp, StandardCopyOption.REPLACE_EXISTING);
				
				if(lCopiedBytes>0)
				{
					lTotalCopied++;
					if(fileCache==null)
						fileCache = fileTmp;
				}
			}
			
			if(lTotalCopied == listFiles.size())
				return fileCache;
		
		} catch (IOException e) {
			e.printStackTrace();
		}finally
		{
			try {
				if(in!=null)
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
	}
	
	public Properties getPluginProps()
	{
		if(props_model==null)
			props_model = getPluginPropsByFileName(null);
		
		return props_model;
	}
	
	public String getPluginSource()
	{
		return this._plugin_source;
	}
	
	private Properties getPluginPropsByFileName(String aPropFileName)
	{
		if(props_model!=null && props_model.size()>0)
			return props_model;
		
		Properties propPlugin = new Properties();
		
		if(aPropFileName==null)
			aPropFileName = this.pluginConfig.getProp_filename();
		
		String sPluginPropPath = getResPath()+"/"+aPropFileName;
		try {
			propPlugin = PropUtil.loadProperties(thisclass, sPluginPropPath);
			
			if(propPlugin!=null && propPlugin.size()>0)
				props_model = propPlugin;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return propPlugin;
	}
	
	public String getPluginName() {
		return getModelName();
	}
	
	protected String getModelName()
	{
		return props_model.getProperty(getPropModelName());
	}
	
	public String getPluginMLModelFileName()
	{
		return getModelFileName();
	}
	
	protected String getModelFileName()
	{
		if(_model_filename==null || _model_filename.trim().length()==0)
		{
			_model_filename = props_model.getProperty(getPropModelDetectFileName());
		}
		return _model_filename;
	}
	
	protected String getResPath()
	{
		return "/"+thisclass.getPackageName().replaceAll("\\.", "/");
	}

	//
	public static Mat getCvMatFromFile(File aImgFile)
	{
		return getCvMatFromFile(aImgFile, Imgcodecs.IMREAD_UNCHANGED);
	}
	
	protected static Mat getCvMatFromFile(File aImgFile,  int aIMREAD_Type)
	{
		Mat mat = Imgcodecs.imread(aImgFile.getAbsolutePath(), aIMREAD_Type);
		OpenCvUtil.removeAlphaChannel(mat);
		return mat;
	}

	public Map<String,Object> match(Mat aImageFile, JSONObject aCustomThresholdJson, Map<String,Object> aMatchingTargetList)
	{
		return null;
	}
	
	public Map<String,Object> extract(Mat aImageFile, JSONObject aCustomThresholdJson)
	{
		return null;
	}
    
}