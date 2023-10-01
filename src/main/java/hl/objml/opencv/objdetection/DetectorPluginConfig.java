package hl.objml.opencv.objdetection;

public class DetectorPluginConfig {
	
	//
	private static final String DEF_PLUGIN_PROP_FILENAME 		= "objml-plugin.properties";
	private static final String DEF_PLUGIN_PROPKEY_PREFIX 		= "objml.";
	//
	private static final String DEF_PROPKEY_PLUGIN_IMPL_CLASSNAME 		= "mlmodel.detection.implementation.classname";
	private static final String DEF_PROPKEY_MLMODEL_NAME 				= "mlmodel.name";
	private static final String DEF_PROPKEY_MLMODEL_DETECT_FILENAME		= "mlmodel.detection.filename";
	//
	private String prop_filename 	= DEF_PLUGIN_PROP_FILENAME;
	private String propkey_prefix 	= DEF_PLUGIN_PROPKEY_PREFIX;
	
	private String propkey_pluginImplClassName 			= DEF_PROPKEY_PLUGIN_IMPL_CLASSNAME;
	private String propkey_pluginMLModelName 			= DEF_PROPKEY_MLMODEL_NAME;
	private String propkey_pluginMLModelDetectFileName 	= DEF_PROPKEY_MLMODEL_DETECT_FILENAME;
	//
	public DetectorPluginConfig()
	{
	}
	//
	public void reset()
	{
		this.prop_filename 							= DEF_PLUGIN_PROP_FILENAME;
		this.propkey_prefix 						= DEF_PLUGIN_PROPKEY_PREFIX;
		this.propkey_pluginImplClassName 			= DEF_PROPKEY_PLUGIN_IMPL_CLASSNAME;
		this.propkey_pluginMLModelName 				= DEF_PROPKEY_MLMODEL_NAME;
		this.propkey_pluginMLModelDetectFileName 	= DEF_PROPKEY_MLMODEL_DETECT_FILENAME;
	}
	public String getProp_filename() {
		return prop_filename;
	}
	public void setProp_filename(String prop_filename) {
		this.prop_filename = prop_filename;
	}
	public String getPropkey_prefix() {
		return propkey_prefix;
	}
	public void setPropkey_prefix(String propkey_prefix) {
		this.propkey_prefix = propkey_prefix;
	}
	public String getPropkey_pluginImplClassName() {
		return propkey_pluginImplClassName;
	}
	public void setPropkey_pluginImplClassName(String propkey_pluginImplClassName) {
		this.propkey_pluginImplClassName = propkey_pluginImplClassName;
	}
	public String getPropkey_pluginMLModelName() {
		return propkey_pluginMLModelName;
	}
	public void setPropkey_pluginMLModelName(String propkey_pluginMLModelName) {
		this.propkey_pluginMLModelName = propkey_pluginMLModelName;
	}
	public String getPropkey_pluginMLModelDetectFileName() {
		return propkey_pluginMLModelDetectFileName;
	}
	public void setPropkey_pluginMLModelDetectFileName(String propkey_pluginMLModelDetectFileName) {
		this.propkey_pluginMLModelDetectFileName = propkey_pluginMLModelDetectFileName;
	}
	
	
	
    
}