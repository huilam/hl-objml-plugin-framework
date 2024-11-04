package hl.objml2.plugin;

import hl.objml2.plugin.base.PluginConfig;

public class MLPluginConfig extends PluginConfig{
	//
	private static final String DEF_PROPKEY_PLUGIN_IMPL_CLASSNAME 	= "mlmodel.detection.implementation.classname";
	private static final String DEF_PROPKEY_MLMODEL_NAME 			= "mlmodel.name";
	private static final String DEF_PROPKEY_MLMODEL_DETECT_FILENAME	= "mlmodel.detection.filename";
	private static final String DEF_PROPKEY_MLMODEL_DEPENDENCIES	= "mlmodel.detection.dependencies";
	//
	private String propkey_pluginImplClassName 			= DEF_PROPKEY_PLUGIN_IMPL_CLASSNAME;
	private String propkey_pluginMLModelName 			= DEF_PROPKEY_MLMODEL_NAME;
	private String propkey_pluginMLModelDetectFileName 	= DEF_PROPKEY_MLMODEL_DETECT_FILENAME;
	private String propkey_pluginMLModelDependencies	= DEF_PROPKEY_MLMODEL_DEPENDENCIES;
	//
	public MLPluginConfig()
	{
		setDefaultPluginPropFileName("objml-plugin.properties");
		setDefaultPropKeyPrefix("objml.");
		super.reset();
	}
	//
	public void reset()
	{
		super.reset();
		this.propkey_pluginImplClassName 			= DEF_PROPKEY_PLUGIN_IMPL_CLASSNAME;
		this.propkey_pluginMLModelName 				= DEF_PROPKEY_MLMODEL_NAME;
		this.propkey_pluginMLModelDetectFileName 	= DEF_PROPKEY_MLMODEL_DETECT_FILENAME;
		this.propkey_pluginMLModelDependencies		= DEF_PROPKEY_MLMODEL_DEPENDENCIES;
	}
	//
	public String getPropkey_pluginImplClassName() {
		return propkey_pluginImplClassName;
	}
	public void setPropkey_pluginImplClassName(String propkey_pluginImplClassName) {
		this.propkey_pluginImplClassName = propkey_pluginImplClassName;
	}
	//
	public String getPropkey_pluginMLModelName() {
		return propkey_pluginMLModelName;
	}
	public void setPropkey_pluginMLModelName(String propkey_pluginMLModelName) {
		this.propkey_pluginMLModelName = propkey_pluginMLModelName;
	}
	//
	public String getPropkey_pluginMLModelDetectFileName() {
		return propkey_pluginMLModelDetectFileName;
	}
	public void setPropkey_pluginMLModelDetectFileName(String propkey_pluginMLModelDetectFileName) {
		this.propkey_pluginMLModelDetectFileName = propkey_pluginMLModelDetectFileName;
	}
	//
	public String getPropkey_pluginMLModelDependencies() {
		return propkey_pluginMLModelDependencies;
	}
	public void setPropkey_pluginMLModelDependencies(String propkey_pluginMLModelDependencies) {
		this.propkey_pluginMLModelDependencies = propkey_pluginMLModelDependencies;
	}
}