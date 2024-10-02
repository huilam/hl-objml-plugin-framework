package hl.objml2.plugin;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opencv.core.Mat;
import org.opencv.dnn.Net;

import hl.plugin.IBasePlugin;

public interface IObjDetectionPlugin extends IBasePlugin {
	
	public static final String _KEY_OUTPUT_FRAME_DETECTIONS 	= "output_frame_detections";
	public static final String _KEY_OUTPUT_FRAME_DETECTION_META = "output_frame_detection_meta";
	public static final String _KEY_OUTPUT_FRAME_ANNOTATED_IMG 	= "output_frame_annotated_image";
	
	abstract public List<Mat> doInference(Mat aMatInput, Net aDnnNet);
	
	abstract public Map<String,Object> parseDetections(Mat aMatInput, List<Mat> aInferenceOutputMat);
	
	abstract public Properties prePropInit(Properties aProps);
	
}