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
		StringBuffer sbDnnBackEnd = new StringBuffer();
		sbDnnBackEnd.append(this.dnn_preferred_backend);
		switch(this.dnn_preferred_backend)
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
	
	public String getDnnTarget()
	{
		StringBuffer sbDnnTarget = new StringBuffer();
		sbDnnTarget.append(this.dnn_preferred_target);
		switch(this.dnn_preferred_target)
		{
			case Dnn.DNN_TARGET_CPU :
				sbDnnTarget.append(" (DNN_TARGET_CPU)");
				break;
			case Dnn.DNN_TARGET_OPENCL :
				sbDnnTarget.append(" (DNN_TARGET_OPENCL)");
				break;
			case Dnn.DNN_TARGET_CUDA :
				sbDnnTarget.append(" (DNN_TARGET_CUDA)");
				break;
			case Dnn.DNN_TARGET_NPU :
				sbDnnTarget.append(" (DNN_TARGET_NPU)");
				break;
			case Dnn.DNN_TARGET_VULKAN :
				sbDnnTarget.append(" (DNN_TARGET_VULKAN)");
				break;
			case Dnn.DNN_TARGET_FPGA :
				sbDnnTarget.append(" (DNN_TARGET_FPGA)");
				break;
			default:
		}
		return sbDnnTarget.toString();
	}

}