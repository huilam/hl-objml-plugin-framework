package hl.objml.opencv.objdetection;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public interface IImgDetectorPlugin {
	
	public static final String _KEY_MAT_OUTPUT = "mat_output";

	abstract public boolean isPluginOK();
	abstract public Properties getPluginProps();
	
	abstract public String getPluginName();
	abstract public String getPluginMLModelFileName();
	
	abstract public Map<String,Object> detectImage(File aImageFile);
    
}