package hl.objml2.plugin;

import hl.objml2.plugin.base.PluginConfigKey;

public class MLPluginConfigKey extends PluginConfigKey {
	
	protected static String PROPKEY_MLMODEL_PREFIX 		= "mlmodel.";
	public String propkey_pluginMLModelName 			= PROPKEY_MLMODEL_PREFIX+"name";
	public String propkey_pluginMLModelDesc 			= PROPKEY_MLMODEL_PREFIX+"desc";
	public String propkey_pluginMLModelLicense 			= PROPKEY_MLMODEL_PREFIX+"license";
	public String propkey_pluginMLModelSource 			= PROPKEY_MLMODEL_PREFIX+"source";
	public String propkey_pluginDnnBackend				= PROPKEY_MLMODEL_PREFIX+"net.dnn.backend";
	public String propkey_pluginDnnTarget				= PROPKEY_MLMODEL_PREFIX+"net.dnn.target";
	///
	///
	protected static String PROPKEY_DET_PREFIX			= PROPKEY_MLMODEL_PREFIX+"detection.";
	public String propkey_pluginImplClassName 			= PROPKEY_DET_PREFIX+"implementation.classname";
	public String propkey_pluginConfidenceScore 		= PROPKEY_DET_PREFIX+"confidence-threshold";
	public String propkey_pluginNmsScore				= PROPKEY_DET_PREFIX+"nms-threshold";
	public String propkey_pluginInputSize				= PROPKEY_DET_PREFIX+"input-size";
	public String propkey_pluginSuppLabels				= PROPKEY_DET_PREFIX+"support-labels";
	public String propkey_pluginSuppLabelPAFs			= PROPKEY_DET_PREFIX+"support-labels.pafs";
	public String propkey_pluginMLModelDetectFileName 	= PROPKEY_DET_PREFIX+"filename";
	public String propkey_pluginMLModelDependencies		= PROPKEY_DET_PREFIX+"dependencies";

	//
	public MLPluginConfigKey()
	{
		setProp_filename("objml-plugin.properties");
		setPropkey_prefix("objml.");
		reset();
	}
	//
	public void reset()
	{	
		PROPKEY_MLMODEL_PREFIX = getPropkey_prefix()+"mlmodel.";
		PROPKEY_DET_PREFIX	   = PROPKEY_MLMODEL_PREFIX+"detection.";
		
		this.propkey_pluginMLModelName 			= PROPKEY_MLMODEL_PREFIX+"name";
		this.propkey_pluginMLModelDesc 			= PROPKEY_MLMODEL_PREFIX+"desc";
		this.propkey_pluginMLModelLicense 		= PROPKEY_MLMODEL_PREFIX+"license";
		this.propkey_pluginMLModelSource 		= PROPKEY_MLMODEL_PREFIX+"source";
		this.propkey_pluginDnnBackend			= PROPKEY_MLMODEL_PREFIX+"net.dnn.backend";
		this.propkey_pluginDnnTarget			= PROPKEY_MLMODEL_PREFIX+"net.dnn.target";
		///
		///
		this.propkey_pluginImplClassName 			= PROPKEY_DET_PREFIX+"implementation.classname";
		this.propkey_pluginConfidenceScore 			= PROPKEY_DET_PREFIX+"confidence-threshold";
		this.propkey_pluginNmsScore					= PROPKEY_DET_PREFIX+"nms-threshold";
		this.propkey_pluginInputSize				= PROPKEY_DET_PREFIX+"input-size";
		this.propkey_pluginSuppLabels				= PROPKEY_DET_PREFIX+"support-labels";
		this.propkey_pluginSuppLabelPAFs			= PROPKEY_DET_PREFIX+"support-labels.pafs";
		this.propkey_pluginMLModelDetectFileName 	= PROPKEY_DET_PREFIX+"filename";
		this.propkey_pluginMLModelDependencies		= PROPKEY_DET_PREFIX+"dependencies";
		
	}
}