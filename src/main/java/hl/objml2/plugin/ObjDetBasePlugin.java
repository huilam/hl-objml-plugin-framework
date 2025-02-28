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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import hl.common.ImgUtil;
import hl.common.PropUtil;
import hl.objml2.plugin.base.PluginConfig;
import hl.opencv.util.OpenCvUtil;

public class ObjDetBasePlugin implements IObjDetectionPlugin {
	
	protected static String PROPKEY_NMS_THRESHOLD 			= "objml.mlmodel.detection.nms-threshold";
	protected static String PROPKEY_CONFIDENCE_THRESHOLD 	= "objml.mlmodel.detection.confidence-threshold";
	protected static String PROPKEY_INPUT_IMGSIZE 			= "objml.mlmodel.detection.input-size";
	protected static String PROPKEY_SUPPORTED_LABELS 		= "objml.mlmodel.detection.support-labels";
	protected static String PROPKEY_SUPPORTED_LABEL_PAFS 	= "objml.mlmodel.detection.support-labels.pafs";
	
	protected static String PROPKEY_DNN_BACKEND 			= "objml.mlmodel.net.dnn.backend";
	protected static String PROPKEY_DNN_TARGET 				= "objml.mlmodel.net.dnn.target";
	
	////////////////////
	protected List<String>OBJ_CLASSESS			= new ArrayList<>();
	protected List<int[]> OBJ_PAF_LIST			= new ArrayList<int[]>();
	////////////////////
	protected MLPluginConfig pluginConfig 	= new MLPluginConfig();
	protected Class<?> thisclass 		= null;
	protected Properties props_model 	= null;
	protected String _model_filename 	= null;
	protected String _plugin_source 	= null;
	/////////////////////
	protected Net NET_DNN 				= null;
	protected int dnn_preferred_backend	= Dnn.DNN_BACKEND_DEFAULT;
	protected int dnn_preferred_target	= Dnn.DNN_TARGET_CPU;	
	protected double dnn_confidence_threshold = 0;
	protected double dnn_nms_threshold 	= 0;
	protected Size dnn_input_size 		= new Size(0,0);
	
	protected int override_dnn_preferred_backend		= -1;
	protected int override_dnn_preferred_target			= -1;	
	protected double override_dnn_confidence_threshold 	= -1;
	
	private static Pattern patt_paf 	= Pattern.compile("\\s*([0-9]+)\\-([0-9]+)\\s*"); 
	
	////////////////////
	private boolean isRegObjsOfInterest 				= false;
	protected List<String> obj_classes_of_interest 		= new ArrayList<String>();
	protected Map<String, String> mapObjClassMapping 	= new HashMap<String, String>();
	//
	private boolean isInited = false;
	
	private void setDnnConfigOverride()
	{
		if(this.override_dnn_preferred_backend>-1)
		{
			this.dnn_preferred_backend = this.override_dnn_preferred_backend;
		}
		
		if(this.override_dnn_preferred_target>-1)
		{
			this.dnn_preferred_target = this.override_dnn_preferred_target;
		}
		
		if(this.override_dnn_confidence_threshold>-1)
		{
			this.dnn_confidence_threshold = this.override_dnn_confidence_threshold;
		}
	}
	
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
		if(isInited)
			return true;
		
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
						isInited = true;
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
		File fileMlModel = getMLFile(aModelFilePath);
				
		
		if(fileMlModel!=null && fileMlModel.isFile())
		{
			_model_filename = fileMlModel.getAbsolutePath();
		}
		
		return fileMlModel;
	}
	
	protected File getMLFile(String aModelFilePath)
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
	
	protected File extractModelFileFromJarAsTemp(File file)
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
		
		//
		String sDnnBackend = prop.getProperty(PROPKEY_DNN_BACKEND);
		if(isNumeric(sDnnBackend))
		{
			if(this.dnn_preferred_backend>-1)
				this.dnn_preferred_backend = Integer.parseInt(sDnnBackend);
		}
		//
		String sDnnTarget = prop.getProperty(PROPKEY_DNN_TARGET);
		if(isNumeric(sDnnTarget))
		{
			if(this.dnn_preferred_target>-1)
				this.dnn_preferred_target = Integer.parseInt(sDnnTarget);
		}
		//
		String sConfThreshold = (String) prop.get(PROPKEY_CONFIDENCE_THRESHOLD);
		if(sConfThreshold!=null && sConfThreshold.trim().length()>0)
		{
			try {
				this.dnn_confidence_threshold = Double.parseDouble(sConfThreshold);
			}catch(NumberFormatException ex)
			{
				ex.printStackTrace();
			}
		}
		//
		String sSupporedLabels = (String) prop.get(PROPKEY_SUPPORTED_LABELS);
		if(sSupporedLabels!=null && sSupporedLabels.trim().length()>0)
		{
			String[] objs = sSupporedLabels.split("\n");
			OBJ_CLASSESS = new ArrayList<>(Arrays.asList(objs));
			
			//
			String sLabelPAFs = (String) prop.get(PROPKEY_SUPPORTED_LABEL_PAFS);
			if(sLabelPAFs!=null && sLabelPAFs.trim().length()>0)
			{
				String[] sPAFList = sLabelPAFs.split("\n");
				
				OBJ_PAF_LIST =  new ArrayList<int[]>();
				
				for(String sPAF : sPAFList)
				{
					Matcher m = patt_paf.matcher(sPAF);
					if(m.find())
					{
						int iP1 = Integer.valueOf(m.group(1));
						int iP2 = Integer.valueOf(m.group(2));
						//
						OBJ_PAF_LIST.add(new int[] {iP1, iP2});
					}
				}
			}
			
		}
		//
		String sNMSThreshold = (String) prop.get(PROPKEY_NMS_THRESHOLD);
		if(sNMSThreshold!=null && sNMSThreshold.trim().length()>0)
		{
			try {
				this.dnn_nms_threshold = Double.parseDouble(sNMSThreshold);
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
				this.dnn_input_size = new Size(dWidth,dHeight);
			}
		}
		
		setDnnConfigOverride();
		
		return true;
	}
	
	private static boolean isNumeric(String str) {
	    try {
	        Double.parseDouble(str);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
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
	///////////
	public double getConfidenceThreshold()
	{
		return this.dnn_confidence_threshold;
	}
	
	public double setConfidenceThreshold_Override(double aConfidenceThreshold)
	{
		this.override_dnn_confidence_threshold = aConfidenceThreshold;
		return this.override_dnn_confidence_threshold;
	}
	
	public double getNMSThreshold()
	{
		return this.dnn_nms_threshold;
	}
	
	public Size getImageInputSize()
	{
		return this.dnn_input_size;
	}
	
	public String[] getSupportedObjectLabels()
	{
		return OBJ_CLASSESS.toArray(new String[OBJ_CLASSESS.size()]);
	}
	
	public int setDnnBackendByText_Override(String aDnnTargetText)
	{
		int iDnnBackendId = -1;
		switch (aDnnTargetText)
		{
			case "DNN_BACKEND_DEFAULT":
				iDnnBackendId = Dnn.DNN_BACKEND_DEFAULT;
				break;
			case "DNN_BACKEND_OPENCV":
				iDnnBackendId = Dnn.DNN_BACKEND_OPENCV;
				break;
			case "DNN_BACKEND_CUDA":
				iDnnBackendId = Dnn.DNN_BACKEND_CUDA;
				break;
			case "DNN_BACKEND_INFERENCE_ENGINE":
				iDnnBackendId = Dnn.DNN_BACKEND_INFERENCE_ENGINE;
				break;
			case "DNN_BACKEND_VKCOM":
				iDnnBackendId = Dnn.DNN_BACKEND_VKCOM;
				break;
			case "DNN_BACKEND_WEBNN":
				iDnnBackendId = Dnn.DNN_BACKEND_WEBNN;
				break;
			case "DNN_BACKEND_CANN":
				iDnnBackendId = Dnn.DNN_BACKEND_CANN;
				break;
			case "DNN_BACKEND_TIMVX":
				iDnnBackendId = Dnn.DNN_BACKEND_TIMVX;
				break;
			default:
				iDnnBackendId = -1;
		}
		
		
		return setDnnBackend_Override(iDnnBackendId);
	}
	
	public int setDnnBackend_Override(int iDnnBackendId)
	{
		this.override_dnn_preferred_backend = iDnnBackendId;
		return this.override_dnn_preferred_backend;
	}
	
	public int getDnnBackend()
	{
		return this.dnn_preferred_backend;
	}

	public String getDnnBackendDesc()
	{
		StringBuffer sbDnnBackEnd = new StringBuffer();
		sbDnnBackEnd.append(getDnnBackend());
		switch(getDnnBackend())
		{
			case Dnn.DNN_BACKEND_DEFAULT :
				sbDnnBackEnd.append(" (DNN_BACKEND_DEFAULT)");
				break;
			case Dnn.DNN_BACKEND_OPENCV :
				sbDnnBackEnd.append(" (DNN_BACKEND_OPENCV)");
				break;
			case Dnn.DNN_BACKEND_CUDA :
				sbDnnBackEnd.append(" (DNN_BACKEND_CUDA)");
				break;
			case Dnn.DNN_BACKEND_INFERENCE_ENGINE :
				sbDnnBackEnd.append(" (DNN_BACKEND_INFERENCE_ENGINE)");
				break;
			case Dnn.DNN_BACKEND_HALIDE :
				sbDnnBackEnd.append(" (DNN_BACKEND_HALIDE)");
				break;
			case Dnn.DNN_BACKEND_CANN :
				sbDnnBackEnd.append(" (DNN_BACKEND_CANN)");
				break;
			case Dnn.DNN_BACKEND_TIMVX :
				sbDnnBackEnd.append(" (DNN_BACKEND_TIMVX)");
				break;
			case Dnn.DNN_BACKEND_VKCOM :
				sbDnnBackEnd.append(" (DNN_BACKEND_VKCOM)");
				break;
			case Dnn.DNN_BACKEND_WEBNN :
				sbDnnBackEnd.append(" (DNN_BACKEND_WEBNN)");
				break;
			default:
		}
		return sbDnnBackEnd.toString();
	}
	
	
	public int setDnnTargetByText_Override(String aDnnTargetText)
	{
		int iDnnTargetId = -1;
		switch (aDnnTargetText)
		{
			case "DNN_TARGET_CPU":
				iDnnTargetId = Dnn.DNN_TARGET_CPU;
				break;
			case "DNN_TARGET_CPU_FP16":
				iDnnTargetId = Dnn.DNN_TARGET_CPU_FP16;
				break;
				
			case "DNN_TARGET_CUDA":
				iDnnTargetId = Dnn.DNN_TARGET_CUDA;
				break;
				
			case "DNN_TARGET_CUDA_FP16":
				iDnnTargetId = Dnn.DNN_TARGET_CUDA_FP16;
				break;
				
			case "DNN_TARGET_OPENCL":
				iDnnTargetId = Dnn.DNN_TARGET_OPENCL;
				break;
				
			case "DNN_TARGET_OPENCL_FP16":
				iDnnTargetId = Dnn.DNN_TARGET_OPENCL_FP16;
				break;
				
			case "DNN_TARGET_VULKAN":
				iDnnTargetId = Dnn.DNN_TARGET_VULKAN;
				break;
				
			case "DNN_TARGET_FPGA":
				iDnnTargetId = Dnn.DNN_TARGET_FPGA;
				break;
				
			case "DNN_TARGET_NPU":
				iDnnTargetId = Dnn.DNN_TARGET_NPU;
				break;
				
			case "DNN_TARGET_MYRIAD":
				iDnnTargetId = Dnn.DNN_TARGET_MYRIAD;
				break;
				
			case "DNN_TARGET_HDDL":
				iDnnTargetId = Dnn.DNN_TARGET_HDDL;
				break;
				
			default:
				iDnnTargetId = -1;
		}
		
		return setDnnTarget_Override(iDnnTargetId);
	}
	
	public int setDnnTarget_Override(int iDnnTargetId)
	{
		this.override_dnn_preferred_target = iDnnTargetId;
		return this.override_dnn_preferred_target;
	}
	
	public int getDnnTarget()
	{
		return this.dnn_preferred_target;
	}
	
	public String getDnnTargetDesc()
	{
		StringBuffer sbDnnTarget = new StringBuffer();
		sbDnnTarget.append(getDnnTarget());
		switch(getDnnTarget())
		{
			case Dnn.DNN_TARGET_CPU :
				sbDnnTarget.append(" (DNN_TARGET_CPU)");
				break;
			case Dnn.DNN_TARGET_CPU_FP16 :
				sbDnnTarget.append(" (DNN_TARGET_CPU_FP16)");
				break;
			case Dnn.DNN_TARGET_OPENCL :
				sbDnnTarget.append(" (DNN_TARGET_OPENCL)");
				break;
			case Dnn.DNN_TARGET_OPENCL_FP16 :
				sbDnnTarget.append(" (DNN_TARGET_OPENCL_FP16)");
				break;
			case Dnn.DNN_TARGET_CUDA :
				sbDnnTarget.append(" (DNN_TARGET_CUDA)");
				break;
			case Dnn.DNN_TARGET_CUDA_FP16 :
				sbDnnTarget.append(" (DNN_TARGET_CUDA_FP16)");
				break;
			case Dnn.DNN_TARGET_NPU :
				sbDnnTarget.append(" (DNN_TARGET_NPU)");
				break;
			case Dnn.DNN_TARGET_MYRIAD :
				sbDnnTarget.append(" (DNN_TARGET_MYRIAD)");
				break;
			case Dnn.DNN_TARGET_VULKAN :
				sbDnnTarget.append(" (DNN_TARGET_VULKAN)");
				break;
			case Dnn.DNN_TARGET_FPGA :
				sbDnnTarget.append(" (DNN_TARGET_FPGA)");
				break;
			case Dnn.DNN_TARGET_HDDL :
				sbDnnTarget.append(" (DNN_TARGET_HDDL)");
				break;
			default:
		}
		return sbDnnTarget.toString();
	}
	
	///////////

	@Override
	public boolean isPluginOK() {
		
		if(isInited)
			return true;
		
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
	
	public Net getDnnNet()
	{
		return this.NET_DNN;
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