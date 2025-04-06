package hl.objml.api;

import java.util.HashMap;
import java.util.Map;
import org.opencv.core.Mat;

public class ObjMLInputParam {
	
	private Mat input_image = null;
	private Map<String,String> input_param_map = new HashMap<String,String>();
	
	private static String CONFIDENCE_THRESHOLD = "confidence-threshold";
	
	//=====================
	public Mat getInput_image() {
		return input_image;
	}
	public void setInput_image(Mat input_image) {
		this.input_image = input_image;
	}
	//=====================
	public double getConfidenceThreshold() {
		return getParamValueAsDouble(CONFIDENCE_THRESHOLD);
	}
	
	public void setConfidenceThreshold(double aConfidenceScore) {
		this.input_param_map.put(CONFIDENCE_THRESHOLD, String.valueOf(aConfidenceScore));
	}
	//=====================
	
	//////////
	private double getParamValueAsDouble(String aParamName)
	{
		double dVal = 0;
		String sVal = getInputParam(CONFIDENCE_THRESHOLD);
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