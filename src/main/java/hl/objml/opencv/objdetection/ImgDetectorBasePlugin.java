package hl.objml.opencv.objdetection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
			if(fModelFile!=null && fModelFile.isFile())
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
		
		if(fModelFile==null)
		{
			System.err.println("props_model is NULL - "+aPropFileName);
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
	
	private File extractFileFromJarAsTemp(File file)
	{	
		//try load from jar
		
		InputStream in 		= null;
		
		try {
			in = thisclass.getResourceAsStream(file.getAbsolutePath());
			if(in==null)
				in = thisclass.getResourceAsStream("/"+file.getAbsolutePath());
			
			if(in==null)
				return null;
			
			String sFileExt = "";
			String sFileName = file.getName();
			int iExtPos = sFileName.lastIndexOf('.');
			if(iExtPos>=0)
			{
				sFileExt = sFileName.substring(iExtPos);
			}
			File fileTmp = File.createTempFile(sFileName, sFileExt);
			Path pathTmp = Paths.get(fileTmp.getAbsolutePath());
			long lCopied = Files.copy(in, pathTmp, StandardCopyOption.REPLACE_EXISTING);
			if(lCopied>0)
				return fileTmp;
		
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
		return Imgcodecs.imread(aImgFile.getAbsolutePath(), aIMREAD_Type);
	}
    
}