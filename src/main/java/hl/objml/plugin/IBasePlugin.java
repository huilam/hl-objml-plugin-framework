package hl.objml.plugin;

import java.util.Properties;

@Deprecated
public interface IBasePlugin {
	
	abstract public void setPluginConfig(PluginConfig pluginConfig);
	abstract public void setPluginSource(String aPluginSource);
	
	abstract public boolean isPluginOK();
	abstract public boolean isValidateMLFileLoading();
	abstract public Properties getPluginProps();
	
	abstract public String getPluginSource();
	abstract public String getPluginName();
	abstract public String getPluginMLModelFileName();
	
}