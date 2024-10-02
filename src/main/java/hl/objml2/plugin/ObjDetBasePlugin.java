package hl.objml2.plugin;

import java.awt.image.BufferedImage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import hl.common.ImgUtil;
import hl.common.PropUtil;
import hl.opencv.util.OpenCvUtil;
import hl.plugin.PluginConfig;

public class ObjDetBasePlugin implements IObjDetectionPlugin{
	
	
	protected static String PROPKEY_NMS_THRESHOLD 			= "objml.mlmodel.detection.nms-threshold";
	protected static String PROPKEY_CONFIDENCE_THRESHOLD 	= "objml.mlmodel.detection.confidence-threshold";
	protected static String PROPKEY_INPUT_IMGSIZE 			= "objml.mlmodel.detection.input-size";
	protected static String PROPKEY_SUPPORTED_LABELS 		= "objml.mlmodel.detection.support-labels";
	
	////////////////////
	protected double DEF_CONFIDENCE_THRESHOLD	= 0;
	protected double DEF_NMS_THRESHOLD			= 0;
	protected Size DEF_INPUT_SIZE				= new Size(0,0);
	protected List<String>OBJ_CLASSESS			= new ArrayList<>();
	////////////////////
	protected MLPluginConfig pluginConfig 	= new MLPluginConfig();
	protected Class<?> thisclass 		= null;
	protected Properties props_model 	= null;
	protected String _model_filename 	= null;
	protected String _plugin_source 	= null;
	/////////////////////
	protected Net NET_DNN 				= null;
	////////////////////
	private boolean isRegObjsOfInterest 				= false;
	protected List<String> obj_classes_of_interest 		= new ArrayList<String>();
	protected Map<String, String> mapObjClassMapping 	= new HashMap<String, String>();
	
	
	public void setPluginConfig(PluginConfig aPluginConfig)
	{
		this.pluginConfig = (MLPluginConfig) aPluginConfig;
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
			props_model = prePropInit(props_model);
			
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
		
		if(mat!=null)
		{
			//if opencv loader failed, try java image loader
			if(mat.width()==0)
			{
				System.err.println("OpenCv Imgcodecs loader failed. Switching to Java image loader. "+aImgFile.getName());
				try {
					BufferedImage img = ImgUtil.loadImage(aImgFile.getAbsolutePath());
					if(img!=null)
					{
						mat = OpenCvUtil.bufferedImg2Mat(img);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			OpenCvUtil.removeAlphaChannel(mat);
		}
		return mat;
	}

	public Map<String, Object> detect(Mat aImageFile, JSONObject aCustomThresholdJson) 
	{	
		if(isPluginOK())
		{	
			List<Mat> listOutput = doInference(aImageFile, this.NET_DNN);		
			return parseDetections(aImageFile, listOutput);
		}
		return null;
	}
	
	protected boolean init()
	{
		Properties prop = prePropInit(getPluginProps());
		
		String sSupporedLabels = (String) prop.get(PROPKEY_SUPPORTED_LABELS);
		if(sSupporedLabels!=null && sSupporedLabels.trim().length()>0)
		{
			String[] objs = sSupporedLabels.split("\n");
			OBJ_CLASSESS = new ArrayList<>(Arrays.asList(objs));
		}
		//
		String sConfThreshold = (String) prop.get(PROPKEY_CONFIDENCE_THRESHOLD);
		if(sConfThreshold!=null && sConfThreshold.trim().length()>0)
		{
			try {
				DEF_CONFIDENCE_THRESHOLD = Float.parseFloat(sConfThreshold);
			}catch(NumberFormatException ex)
			{
				ex.printStackTrace();
			}
		}
		//
		String sNMSThreshold = (String) prop.get(PROPKEY_NMS_THRESHOLD);
		if(sNMSThreshold!=null && sNMSThreshold.trim().length()>0)
		{
			try {
				DEF_NMS_THRESHOLD = Float.parseFloat(sNMSThreshold);
			}catch(NumberFormatException ex)
			{
				ex.printStackTrace();
			}
		}
		//
		String sInputImageSize = (String) prop.get(PROPKEY_INPUT_IMGSIZE);
		if(sInputImageSize!=null && sInputImageSize.trim().length()>0)
		{

			String sSeparator = "x";
			if(sInputImageSize.indexOf(sSeparator)==-1)
				sSeparator = ",";
			
			double dWidth = 0;
			double dHeight = 0;
			String[] sSize = sInputImageSize.split(sSeparator);
			if(sSize.length>0)
			{
				try {
					dWidth 	= Double.parseDouble(sSize[0]);
					dHeight = dWidth;
					if(sSize.length>1)
					{
						dHeight = Double.parseDouble(sSize[1]);
					}
				}
				catch(NumberFormatException ex)
				{
					ex.printStackTrace();
				}
				DEF_INPUT_SIZE = new Size(dWidth,dHeight);
			}
		}
		return true;
	}
	

	public String[] getObjClassesOfInterest()
	{
		return (String[]) this.obj_classes_of_interest.toArray();
	}
	
	public void clearObjClassesOfInterest()
	{
		this.obj_classes_of_interest.clear();
		isRegObjsOfInterest = false;
	}
	
	public boolean addObjClassOfInterest(String[] aObjClassNames)
	{
		if(aObjClassNames!=null && aObjClassNames.length>0)
		{
			isRegObjsOfInterest = true;
			for(int i=0; i<aObjClassNames.length; i++)
			{
				this.obj_classes_of_interest.add(aObjClassNames[i].toLowerCase());
			}
			return isRegObjsOfInterest;
		}
		
		return false;
	}
	
	public boolean isObjClassOfInterest(String aObjClassName)
	{
		if(!isRegObjsOfInterest)
			return true;
		
		return this.obj_classes_of_interest.contains(aObjClassName.toLowerCase());
	}
	
	/////
	public void addObjClassMapping(String aOrgObjClassName, String aNewObjClassName)
	{
		this.mapObjClassMapping.put(aOrgObjClassName, aNewObjClassName);
	}
	
	public void clearObjClassesMapping()
	{
		this.mapObjClassMapping.clear();
	}
	
	public String getMappedObjClass(String aOrgObjClassName)
	{
		String sMappedClassName = this.mapObjClassMapping.get(aOrgObjClassName);
		if(sMappedClassName==null)
			sMappedClassName = aOrgObjClassName;
		return sMappedClassName;
	}
	/////
	///////////

	@Override
	public boolean isPluginOK() {
		boolean isOK = isPluginOK(getClass());
		if(!isOK)
		{
			System.err.println("Failed to load/init properties file ! "+this.pluginConfig.getProp_filename());
			return false;
		}
		
		isOK = init();
		if(!isOK)
		{
			System.err.println("Failed to DNN ! "+getModelFileName());
			return false;
		}
		return isOK;
	}
	
	/////
	
	
	@Override
	public List<Mat> doInference(Mat aImageFile, Net aDnnNet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> parseDetections(Mat aMatInput, List<Mat> aInferenceOutputMat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties prePropInit(Properties aProps) {
		
		return aProps;
	}


    
}