package hl.plugin.image;

import java.util.Map;
import org.opencv.core.Mat;

public interface IImgDetectorPlugin extends IImgBasePlugin {
	
	abstract public Map<String,Object> detectImage(Mat aImageFile);
    
}