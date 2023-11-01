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
import java.util.Properties;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import hl.common.PropUtil;
import hl.opencv.util.OpenCvUtil;

public class ImgDetectorBasePlugin {
	
	//
	protected DetectorPluginConfig pluginConfig = new DetectorPluginConfig();
	protected Class<?> thisclass 		= null;
	protected Properties props_model 	= null;
	protected String _model_filename 	= null;
	
	public void setPluginConfig(DetectorPluginConfig aPluginConfig)
	{
		this.pluginConfig = aPluginConfig;
	}
	
	protected boolean isPluginOK(Class<?> aClass)
	{
		return isPluginOK(aClass, this.pluginConfig.getProp_filename());
	}
	
	protected boolean isPluginOK(Class<?> aClass, String aPropFileName)
	{
		if(aClass==null)
			return false;
		
		OpenCvUtil.initOpenCV();
		
		File fModelFile = null;
		
		thisclass = aClass;
		
		props_model = getPluginProps(aPropFileName);
		if(props_model!=null)
		{
			_model_filename = getResPath()+"/"+props_model.getProperty(getPropModelDetectFileName());
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
			
			fModelFile = null;
		}
		
		if(fModelFile==null || !fModelFile.exists())
		{
			System.err.println("Failed to load MLModel - "+_model_filename);
		}
		return false;
	}
	
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
			File fileTmp = extractFileFromJarAsTemp(fileMlModel);
			
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
	
	private File extractFileFromJarAsTemp(File file)
	{	
		//try load from jar
		
		InputStream in 		= null;
		
		try {
			
			String sResFileName = searchResource(file);;
			
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
				
				File fileTmp = File.createTempFile(file.getName()+"/empty", ".tmp");
				//Folder
				fileCache = fileTmp.getParentFile();
			}
			else 
			{
				//single file
				listFiles.add(file);
			}
			
			
			long lCopiedBytes = 0;
			long lTotalCopied = 0;
			for(File f : listFiles)
			{
				String sFileExt 	= "";
				String sFileName 	= f.getName();
				int iExtPos 		= sFileName.lastIndexOf('.');
				if(iExtPos>=0)
				{
					sFileExt = sFileName.substring(iExtPos);
				}
				File fileTmp = File.createTempFile(sFileName, sFileExt);
				
				System.out.println("Extract - "+sFileName);
				
				if(in!=null)
					in.close();
				
				in = thisclass.getResourceAsStream(sResFileName);
				/**
				if(in==null)
					in = thisclass.getResourceAsStream(f.getAbsolutePath());
				
				if(in==null)
					in = thisclass.getResourceAsStream("/"+f.getAbsolutePath());
				**/
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
	
	protected Properties getPluginProps()
	{
		return getPluginProps(null);
	}
	
	protected Properties getPluginProps(String aPropFileName)
	{
		if(props_model!=null && props_model.size()>0)
			return props_model;
		
		Properties propPlugin = new Properties();
		
		if(aPropFileName==null)
			aPropFileName = this.pluginConfig.getProp_filename();
		
		String sPluginPropPath = getResPath()+"/"+aPropFileName;
		try {
			propPlugin = PropUtil.loadProperties(thisclass, sPluginPropPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return propPlugin;
	}
	
	protected String getModelName()
	{
		return props_model.getProperty(getPropModelName());
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

	protected Mat getCvMatFromFile(File aImgFile)
	{
		return getCvMatFromFile(aImgFile, Imgcodecs.IMREAD_UNCHANGED);
	}
	
	protected Mat getCvMatFromFile(File aImgFile,  int aIMREAD_Type)
	{
		Mat mat = Imgcodecs.imread(aImgFile.getAbsolutePath(), aIMREAD_Type);
		OpenCvUtil.removeAlphaChannel(mat);
		return mat;
	}
    
}