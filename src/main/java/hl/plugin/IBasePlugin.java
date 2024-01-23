package hl.plugin;

import java.util.Properties;
import hl.objml.opencv.objdetection.MLPluginConfig;

public interface IBasePlugin {
	
	abstract public void setPluginConfig(MLPluginConfig pluginConfig);
	abstract public void setPluginSource(String aPluginSource);
	
	abstract public boolean isPluginOK();
	abstract public boolean isValidateMLFileLoading();
	abstract public Properties getPluginProps();
	
	abstract public String getPluginSource();
	abstract public String getPluginName();
	abstract public String getPluginMLModelFileName();
	
}