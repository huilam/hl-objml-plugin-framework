package hl.objml2.plugin;

import java.io.File;

import org.opencv.dnn.Dnn;

public class ObjDetDnnBasePlugin extends ObjDetBasePlugin{
	
	protected static String ENVKEY_OCL4DNN_CFG = "OPENCV_OCL4DNN_CONFIG_PATH";
	
	@Override
	protected boolean init()
	{
		NET_DNN = Dnn.readNet( getModelFileName());
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