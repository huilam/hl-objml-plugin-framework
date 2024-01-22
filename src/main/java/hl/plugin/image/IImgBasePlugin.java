package hl.plugin.image;

import java.util.Properties;

import hl.objml.opencv.objdetection.MLPluginConfig;

public interface IImgBasePlugin {
	
	public static final String _KEY_MAT_OUTPUT = "mat_output";

	abstract public void setPluginConfig(MLPluginConfig pluginConfig);
	abstract public void setPluginSource(String aPluginSource);
	
	abstract public boolean isPluginOK();
	abstract public boolean isValidateMLFileLoading();
	abstract public Properties getPluginProps();
	
	abstract public String getPluginSource();
	abstract public String getPluginName();
	abstract public String getPluginMLModelFileName();
	
    
}