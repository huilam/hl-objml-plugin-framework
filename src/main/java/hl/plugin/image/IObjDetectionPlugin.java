package hl.plugin.image;

import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.plugin.IBasePlugin;

public interface IObjDetectionPlugin extends IBasePlugin {
	
	public static final String _KEY_OUTPUT_ANNOTATED_MAT 	= "output_annotated_mat";
	public static final String _KEY_OUTPUT_DETECTION_JSON 	= "output_detection_json";
	public static final String _KEY_OUTPUT_TOTAL_COUNT		= "output_total_count";
	
	public static final String _KEY_THRESHOLD_DETECTION 	= "threshold_detection";
	public static final String _KEY_THRESHOLD_NMS 			= "threshold_nms";

	abstract public Map<String,Object> detect(Mat aImageFile, JSONObject aCustomThresholdJson);
	
}