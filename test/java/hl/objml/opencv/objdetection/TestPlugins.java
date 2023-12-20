package hl.objml.opencv.objdetection;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import hl.common.FileUtil;
import hl.opencv.util.OpenCvUtil;

public class TestPlugins {
	
	private static boolean savedOutputImage(String aSubfolderName, String aModelName, File aInputImgFile, Mat aOutputImage)
	{
		if(aOutputImage==null || aOutputImage.empty())
		{
			System.out.println("       - [Skipped] No Result - "+aModelName+" "+aInputImgFile.getName());
			return false;
		}
		
		String sOutputFolder = aInputImgFile.getParent()+"/output/"+aSubfolderName;
		new File(sOutputFolder).mkdirs();
		
        String sOutputFileName = 
        		sOutputFolder+"/"+aInputImgFile.getName()+"_"+aModelName+"_"+System.currentTimeMillis()+".jpg";
        
        boolean saved = Imgcodecs.imwrite(sOutputFileName, aOutputImage);
        
        if(saved)
        {
        	System.out.println("       - [Saved] Output :"+sOutputFileName);
        }
      
        return saved;
	}
	
    public static void main(String main[]) throws Exception
    {
    	OpenCvUtil.initOpenCV();
    	
    	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHMMSSsss");
    	String sTestCycle = df.format(new Date(System.currentTimeMillis()));
    	
    	File imgFolder = new File("./test/resources/images"); 
    	
    	File[] fileImgs = FileUtil.getFilesWithExtensions(imgFolder, new String[] {".jpg"});
   
    	File pluginsFolder =  new File("./test/resources/plugins");
    	File[] fPluginJars =  FileUtil.getFilesWithExtensions(pluginsFolder, new String[]{".jar",".zip"});
    	
    	System.out.println();
    	System.out.println("plugin bundles discovered : "+fPluginJars.length);
    	
    	/*** Custom configuration for properties ***/
    	DetectorPluginConfig customPluginConfig = new DetectorPluginConfig();
    	customPluginConfig.setProp_filename("objml-plugin.properties");
    	customPluginConfig.setPropkey_prefix("objml.");
    	
    	/*** Init Plugin Mgr  ***/
    	DetectorPluginMgr mgr = new DetectorPluginMgr();
    	
    	/*** Init Custom Plugin Config  ***/
    	mgr.setCustomPluginConfig(customPluginConfig);
    	
    	/*** Register non-classpath bundle files  ***/
    	mgr.addPluginPaths(fPluginJars);
    	
    	/*** Perform javaClass scan for initial ***/
    	Map<String, String> mapPluginClassName = mgr.scanForPluginJavaClassName();
    	
    	/*** Manual Register Java Class Name that in classpath ***/
    	//listPluginClassName.add(Upscale.class.getName());
	    	
    	int iPlugID = 0;
    	for(String sPluginClassName : mapPluginClassName.keySet())
    	{
    		if(sPluginClassName==null)
    			continue;
    		iPlugID++;
    		
 			/*** Get plugin instance ***/
	    	IImgDetectorPlugin detector = mgr.getDetectorInstance(sPluginClassName);
	    	
	    	int iFileCount = 0;
	    	for(File fileImg : fileImgs)
	    	{
	    		iFileCount++;
	    		long lStartMs = System.currentTimeMillis();
	    		
	    		/*** Perform plugin detection ***/
	    		Mat matImage = OpenCvUtil.loadImage(fileImg.getAbsolutePath());
	    		Map<String, ?> mapResult = detector.detectImage(matImage);
	    		long lDetectionMs = (System.currentTimeMillis()-lStartMs);
	    		
	    		Size sizeImg = new Size();
	    		Mat matImg = null;
	    		try {
	    			matImg = OpenCvUtil.loadImage(fileImg.getAbsolutePath());
	    			sizeImg = matImg.size();
	    		}
	    		finally
	    		{
	    			if(matImg!=null)
	    				matImg.release();
	    		}
System.out.println();		
System.out.println(iPlugID+"."+iFileCount+" Image File : "+fileImg.getName()+" ("+sizeImg+")");  		
System.out.println("     - PluginName : "+detector.getPluginName());
System.out.println("     - isPluginOK : "+detector.isPluginOK());
System.out.println("     - ModelFile : "+detector.getPluginMLModelFileName());
System.out.println("     - Detection Elapsed : "+lDetectionMs+" ms");
System.out.println("     - Result : "+mapResult.size()); 

	    		if(mapResult.size()>0)
	    		{
	    			/*** Saved output from plugin detection  ***/
		    		Mat matOutput = (Mat) mapResult.get(IImgDetectorPlugin._KEY_MAT_OUTPUT);
					savedOutputImage(sTestCycle, detector.getPluginName(), fileImg, matOutput);
	    		}
	    	}
    	}
	
    }
    
    
}