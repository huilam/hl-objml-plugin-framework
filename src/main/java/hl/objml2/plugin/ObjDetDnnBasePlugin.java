package hl.objml2.plugin;

import java.io.File;

import org.opencv.dnn.Dnn;

public class ObjDetDnnBasePlugin extends ObjDetBasePlugin {
	
	protected static String ENVKEY_OCL4DNN_CFG 			= "OPENCV_OCL4DNN_CONFIG_PATH";
	private static String MLMODEL_CAFFE_FILEEXT 		= ".caffemodel";
	private static String MLMODEL_CAFFE_CONFIG_FILENAME = "mlmodel.caffe.config.filename";
	
	@Override
	protected boolean init()
	{
		String sMLModelFile = getModelFileName();
		
		if(sMLModelFile.endsWith(MLMODEL_CAFFE_FILEEXT))
		{
			String sCaffeConfig = props_model.getProperty(
					pluginConfig.getPropkey_prefix()+MLMODEL_CAFFE_CONFIG_FILENAME,"");
			
			if(sCaffeConfig.trim().length()==0)
			{
				System.err.println("[CAFFE ML] mlmodel.caffe.config.filename is empty");
			}
			else
			{
				File fileCaffeConfig = getMLModelFile(sCaffeConfig);
				
				if(fileCaffeConfig.isFile())
				{
					NET_DNN = Dnn.readNetFromCaffe(fileCaffeConfig.getAbsolutePath(), sMLModelFile);
				}
				else
				{
					System.err.println("[CAFFE ML] mlmodel.caffe.config.filename NOT Found - "+fileCaffeConfig.getAbsolutePath());
				}
				
			}
		}
		else
		{
			NET_DNN = Dnn.readNet(sMLModelFile);
		}
		
		
		if(NET_DNN!=null)
		{
			if(super.init())
			{
				if(this.dnn_preferred_backend>-1)
					NET_DNN.setPreferableBackend(this.dnn_preferred_backend);

				if(this.dnn_preferred_target>-1)
					NET_DNN.setPreferableTarget(this.dnn_preferred_target);
				
				return true;
			}
		}
		else
		{
			System.err.println("NET_DNN failed to load.");
		}
		return false;
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