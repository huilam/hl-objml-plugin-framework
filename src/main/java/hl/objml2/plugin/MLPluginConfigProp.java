package hl.objml2.plugin;

import java.util.Properties;

public class MLPluginConfigProp extends Properties {
	
	private static final long serialVersionUID = 1L;
	private MLPluginConfigKey pluginKeys = null;
	
	public MLPluginConfigProp()
	{
		setMLPluginConfigKey(new MLPluginConfigKey());
	}
	
	public MLPluginConfigProp(MLPluginConfigKey aMLPluginConfigKey)
	{
		setMLPluginConfigKey(aMLPluginConfigKey);
	}
	
	public void setMLPluginConfigKey(MLPluginConfigKey aMLPluginConfigKey)
	{
		pluginKeys = aMLPluginConfigKey;
	}
	
	//////
	///
	public String getMlModelName()
	{
		return getProperty(pluginKeys.propkey_pluginMLModelName);
	}
	public String getMlModelDesc()
	{
		return getProperty(pluginKeys.propkey_pluginMLModelDesc);
	}
	public String getMlModelLicense()
	{
		return getProperty(pluginKeys.propkey_pluginMLModelLicense);
	}
	public String getMlModelJavaClassName()
	{
		return getProperty(pluginKeys.propkey_pluginImplClassName);
	}
	public String getMlModelDetectFileName()
	{
		return getProperty(pluginKeys.propkey_pluginMLModelDetectFileName);
	}
	public String getMlModelDependencies()
	{
		return getProperty(pluginKeys.propkey_pluginMLModelDependencies);
	}
	public String getMlModelConfidenceScore()
	{
		return getProperty(pluginKeys.propkey_pluginConfidenceScore);
	}
	public String getMlModelNmsScore()
	{
		return getProperty(pluginKeys.propkey_pluginNmsScore);
	}
	public String getMlModelSupportedLabels()
	{
		return getProperty(pluginKeys.propkey_pluginSuppLabels);
	}
	public String getMlModelSupportedLabelPAFs()
	{
		return getProperty(pluginKeys.propkey_pluginSuppLabelPAFs);
	}
	public String getMlModelInputSize()
	{
		return getProperty(pluginKeys.propkey_pluginInputSize);
	}
	public String getDnnBackend()
	{
		return getProperty(pluginKeys.propkey_pluginDnnBackend);
	}
	public String getDnnTarget()
	{
		return getProperty(pluginKeys.propkey_pluginDnnTarget);
	}
	public String getMlModelSource()
	{
		return getProperty(pluginKeys.propkey_pluginMLModelSource);
	}
	/////
	public void setMlModelSource(String aMlModelSource)
	{
		this.setProperty(pluginKeys.propkey_pluginMLModelSource, aMlModelSource);
	}
}