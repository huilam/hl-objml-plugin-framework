package hl.objml2.common;

public class FrameDetectionMeta {

	//
	private String objml_plugin_name 	= null;
	private String objml_model_name 	= null;
	private double confidence_threshold = 0;
	private double nms_threshold 		= 0;
	//
	private String source_name			= null;
	private long start_time_ms			= 0;
	private long elapsed_time_ms		= 0;
	//
	
	public String getObjml_plugin_name() {
		return objml_plugin_name;
	}
	public void setObjml_plugin_name(String objml_plugin_name) {
		this.objml_plugin_name = objml_plugin_name;
	}
	public String getObjml_model_name() {
		return objml_model_name;
	}
	public void setObjml_model_name(String objml_model_name) {
		this.objml_model_name = objml_model_name;
	}
	public double getConfidence_threshold() {
		return confidence_threshold;
	}
	public void setConfidence_threshold(double confidence_threshold) {
		this.confidence_threshold = confidence_threshold;
	}
	public double getNms_threshold() {
		return nms_threshold;
	}
	public void setNms_threshold(double nms_threshold) {
		this.nms_threshold = nms_threshold;
	}
	//
	public String getSource_name() {
		return source_name;
	}
	public void setSource_name(String source_name) {
		this.source_name = source_name;
	}
	public long getStart_time_ms() {
		return start_time_ms;
	}
	public void setStart_time_ms(long start_time_ms) {
		this.start_time_ms = start_time_ms;
	}
	public long getElapsed_time_ms() {
		return elapsed_time_ms;
	}
	public void setElapsed_time_ms(long elapsed_time_ms) {
		this.elapsed_time_ms = elapsed_time_ms;
	}
	
	
}