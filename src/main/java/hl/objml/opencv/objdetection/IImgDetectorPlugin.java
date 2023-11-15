package hl.objml.opencv.objdetection;

import java.util.Map;
import java.util.Properties;

import org.opencv.core.Mat;

public interface IImgDetectorPlugin {
	
	public static final String _KEY_MAT_OUTPUT = "mat_output";

	abstract public void setPluginConfig(DetectorPluginConfig pluginConfig);
	
	abstract public boolean isPluginOK();
	abstract public Properties getPluginProps();
	
	abstract public String getPluginName();
	abstract public String getPluginMLModelFileName();
	
	abstract public Map<String,Object> detectImage(Mat aImageFile);
    
}