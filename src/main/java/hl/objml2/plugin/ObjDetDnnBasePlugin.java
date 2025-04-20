package hl.objml2.plugin;

import java.io.File;

import org.opencv.dnn.Dnn;

public class ObjDetDnnBasePlugin extends ObjDetBasePlugin {
	
	protected static String ENVKEY_OCL4DNN_CFG 			= "OPENCV_OCL4DNN_CONFIG_PATH";
	private static String MLMODEL_CONFIG_FILENAME 		= "mlmodel.detection.mlconfig.filename";
	
	@Override
	protected boolean init()
	{
		String sMLModelFile = getModelFileName();
		
		String sMLModelConfig = getMLModelConfigFilePath();
		
		if(sMLModelConfig!=null)
		{
			//with config
			NET_DNN = Dnn.readNet(sMLModelFile, sMLModelConfig);
		}
		else
		{
			NET_DNN = Dnn.readNet(sMLModelFile);
		}
		
		if(NET_DNN!=null)
		{
			if(super.init())
			{
				if(inferenceConf.getDnn_backend()>-1)
					NET_DNN.setPreferableBackend(inferenceConf.getDnn_backend());

				if(inferenceConf.getDnn_target()>-1)
					NET_DNN.setPreferableTarget(inferenceConf.getDnn_target());
				
				return true;
			}
		}
		else
		{
			System.err.println("NET_DNN failed to load.");
		}
		return false;
	}
	
	protected String getMLModelConfigFilePath()
	{
		String sMlModelConfig = props_model.getProperty(
				pluginCfgKey.getPropkey_prefix()+MLMODEL_CONFIG_FILENAME, null);
		
		if(sMlModelConfig!=null && sMlModelConfig.trim().length()>0)
		{
			File fileCaffeConfig = getMLFile(sMlModelConfig);
			
			if(fileCaffeConfig.isFile())
				return fileCaffeConfig.getAbsolutePath();
		}
		return null;
	}
	
	
	//
	protected void checkOcl4DnnConfig()
	{
		String sOCL4DNN = System.getenv(ENVKEY_OCL4DNN_CFG);
		
		if(sOCL4DNN==null || sOCL4DNN.trim().length()==0)
		{
			System.setProperty(ENVKEY_OCL4DNN_CFG, new File(".").getAbsolutePath());
		}
		
	}

}