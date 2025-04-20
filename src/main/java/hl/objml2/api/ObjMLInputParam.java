package hl.objml2.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.opencv.core.Mat;

public class ObjMLInputParam {
	
	private Mat input_image = null;
	private Map<String,String> input_param_map = new HashMap<String,String>();
	
	private static String DETECTION_CONF_THRESHOLD 	= "detection.confidence-threshold";
	private static String DETECTION_NMS_THRESHOLD 	= "detection.nms-threshold";
	private static String DETECTION_OBJ_OF_INTEREST = "detection.obj-of-interest";
	private static String PREFERRED_DNN_TARGET		= "net.dnn.target";
	private static String PREFERRED_DNN_BACKEND		= "net.dnn.backend";
	
	//=====================
	public Mat getInput_image() {
		return input_image;
	}
	public void setInput_image(Mat input_image) {
		this.input_image = input_image;
	}
	//=====================
	public double getConfidenceThreshold() {
		return getParamValueAsDouble(DETECTION_CONF_THRESHOLD);
	}
	
	public void setConfidenceThreshold(double aConfidenceScore) {
		this.input_param_map.put(DETECTION_CONF_THRESHOLD, String.valueOf(aConfidenceScore));
	}
	//=====================
	public double getNmsThreshold() {
		return getParamValueAsDouble(DETECTION_NMS_THRESHOLD);
	}
	
	public void setNmsThreshold(double aNmsScore) {
		this.input_param_map.put(DETECTION_NMS_THRESHOLD, String.valueOf(aNmsScore));
	}
	//=====================
	public int getPrefferedDnnTarget() {
		return (int)getParamValueAsLong(this.input_param_map.get(PREFERRED_DNN_TARGET));
	}
	
	public void setPrefferedDnnTarget(long aDnnTarget) {
		this.input_param_map.put(PREFERRED_DNN_TARGET, String.valueOf(aDnnTarget));
	}
	//=====================
	public int getPrefferedDnnBackend() {
		return (int)getParamValueAsLong(this.input_param_map.get(PREFERRED_DNN_BACKEND));
	}
	
	public void setPrefferedDnnBackend(long aDnnBackend) {
		this.input_param_map.put(PREFERRED_DNN_BACKEND, String.valueOf(aDnnBackend));
	}

	//=====================
	public String getObjOfInterest() {
		return this.input_param_map.getOrDefault(DETECTION_OBJ_OF_INTEREST, "");
	}
	
	public void setObjOfInterest(String[] aObjOfInterestList) {
		this.input_param_map.put(DETECTION_OBJ_OF_INTEREST, Arrays.toString(aObjOfInterestList));
	}	
	//=====================
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	private long getParamValueAsLong(String aParamName)
	{
		return (long)(getParamValueAsDouble(aParamName));
	}
	private double getParamValueAsDouble(String aParamName)
	{
		double dVal = 0;
		String sVal = getInputParam(aParamName);
		if(sVal!=null && sVal.trim().length()>0)
		{
			try {
				dVal = Double.parseDouble(sVal);
			}catch(NumberFormatException ex)
			{
				//do nothing
			}
		}
		return dVal;
	}
	//////////
	public void clearParams() {
		input_param_map.clear();
	}
	public String getInputParam(String aParamName) {
		return input_param_map.get(aParamName);
	}
	public void addInputParam(String aParamName, String aParamVal) {
		input_param_map.put(aParamName, aParamVal);
	}
	public void removeInputParam(String aParamName) {
		input_param_map.remove(aParamName);
	}
	
}