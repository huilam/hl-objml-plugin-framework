package hl.objml2.plugin;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.plugin.IBasePlugin;

public interface IObjDetectionPlugin extends IBasePlugin {
	
	public static final String _KEY_OUTPUT_FRAME_DETECTIONS 	= "output_frame_detections";
	public static final String _KEY_OUTPUT_FRAME_DETECTION_META = "output_frame_detection_meta";
	public static final String _KEY_OUTPUT_FRAME_ANNOTATED_IMG 	= "output_frame_annotated_image";
	
	abstract public List<Mat> doInference(Mat aMatInput, JSONObject aCustomThresholdJson);
	
	abstract public Map<String,Object> parseDetections(
			List<Mat> aInferenceOutputMat, 
			Mat aMatInput, JSONObject aCustomThresholdJson);
	
	abstract public Properties prePropInit(Properties aProps);
	
}