package hl.objml2.plugin;

import java.util.Properties;

import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

public class ObjDetDnnBasePlugin extends ObjDetBasePlugin{
	
	protected static String PROPKEY_DNN_BACKEND 	= "objml.mlmodel.net.dnn.backend";
	protected static String PROPKEY_DNN_TARGET 		= "objml.mlmodel.net.dnn.target";
	//
	protected Net NET_DNN 				= null;
	private int dnn_preferred_backend	= Dnn.DNN_BACKEND_DEFAULT;
	private int dnn_preferred_target	= Dnn.DNN_TARGET_CPU;
	//
		
	protected boolean init()
	{
		
		NET_DNN = Dnn.readNet( getModelFileName());
		
		if(NET_DNN!=null)
		{
			super.init();
			
			Properties prop = getPluginProps();
			//
			String sDnnBackend = prop.getProperty(PROPKEY_DNN_BACKEND, "-1");
			if(isNumeric(sDnnBackend))
			{
				this.dnn_preferred_backend = Integer.parseInt(sDnnBackend);
			}
			//
			String sDnnTarget = prop.getProperty(PROPKEY_DNN_TARGET, "-1");
			if(isNumeric(sDnnTarget))
			{
				this.dnn_preferred_target = Integer.parseInt(sDnnTarget);
			}
			//
			NET_DNN.setPreferableBackend(this.dnn_preferred_backend);
			NET_DNN.setPreferableTarget(this.dnn_preferred_target);
			
		}
		
		return (NET_DNN!=null);
	}
	
	private static boolean isNumeric(String str) {
	    try {
	        Double.parseDouble(str);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	
	public Net getDnnNet()
	{
		return this.NET_DNN;
	}
	
	public String getDnnBackend()
	{
		return ""+this.dnn_preferred_backend;
	}
	
	public String getDnnTarget()
	{
		return ""+this.dnn_preferred_target;
	}

}