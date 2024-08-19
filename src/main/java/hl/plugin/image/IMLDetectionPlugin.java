package hl.plugin.image;

import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.plugin.IBasePlugin;

public interface IMLDetectionPlugin extends IBasePlugin {
	
	public static final String _KEY_MAT_OUTPUT 			= "mat_output";
	public static final String _KEY_TOTAL_DETECTION		= "total_detection_count";
	public static final String _KEY_THRESHOLD_DETECTION = "threshold_detection";
	public static final String _KEY_THRESHOLD_NMS 		= "threshold_nms";
	//
	public static final String _KEY_MATCHING_THRESHOLD 	= "threshold_matching";

	abstract public Map<String,Object> detect(Mat aImageFile, JSONObject aCustomThresholdJson);
	abstract public Map<String,Object> match(Mat aImageFile, JSONObject aCustomThresholdJson, Map<String,Object> aMatchingTargetList);
	abstract public Map<String,Object> extract(Mat aImageFile, JSONObject aCustomThresholdJson);
	
}