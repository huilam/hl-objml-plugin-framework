package hl.plugin.image;

import java.util.Map;
import org.opencv.core.Mat;

public interface IImgMatcherPlugin extends IImgBasePlugin {
	
	abstract public Map<String,Object> matchImage(Mat aImageFile, double aMatchingThreshold, Map<String,Object> aMatchingTargetList);
    
}