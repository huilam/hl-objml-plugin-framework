package hl.objml2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import hl.common.FileUtil;
import hl.objml2.plugin.IObjDetectionPlugin;
import hl.objml2.plugin.MLPluginConfig;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
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
        		sOutputFolder+"/"+aInputImgFile.getName()+"_"+aModelName+"_"+System.currentTimeMillis()+".png";
        
        boolean saved = Imgcodecs.imwrite(sOutputFileName, aOutputImage);
        
        if(saved)
        {
        	System.out.println("       - [Saved] Output :"+sOutputFileName);
        }
      
        return saved;
	}
	
	private static String removeDotPath(String aPath)
	{
		String sTrimPath = aPath;
		if(aPath!=null)
		{
			int iPos = aPath.indexOf(".");
			if(iPos>0)
			{
				sTrimPath = aPath.substring(iPos);
			}
		}
		return sTrimPath;
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
    	MLPluginConfig customPluginConfig = new MLPluginConfig();
    	customPluginConfig.setProp_filename("objml-plugin.properties");
    	customPluginConfig.setPropkey_prefix("objml.");
    	
    	/*** Init Plugin Mgr  ***/
    	MLPluginMgr mgr = new MLPluginMgr();
    	
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
	    	IObjDetectionPlugin detector = mgr.getMLInstance(sPluginClassName);
	    	
	    	int iFileCount = 0;
	    	for(File fileImg : fileImgs)
	    	{
	    		iFileCount++;
	    		long lStartMs = System.currentTimeMillis();
	    		
	    		/*** Perform plugin detection ***/
	    		Mat matImage = ObjDetBasePlugin.getCvMatFromFile(fileImg);
	    		Size sizeImg = matImage.size();
	    		
	    		List<Mat> listOutput = detector.doInference(matImage, null);
	    		Map<String, ?> mapResult = detector.parseDetections(listOutput, matImage, null);
	    		long lDetectionMs = (System.currentTimeMillis()-lStartMs);
	    		
System.out.println();		
System.out.println(iPlugID+"."+iFileCount+" Image File : "+fileImg.getName()+" ("+sizeImg+")");  		
System.out.println("     - PluginName : "+detector.getPluginName());
System.out.println("     - isPluginOK : "+detector.isPluginOK());
System.out.println("     - ModelFile : "+removeDotPath(detector.getPluginMLModelFileName()));
System.out.println("     - Detection Elapsed : "+lDetectionMs+" ms");
System.out.println("     - Result : "+mapResult.size()); 

	    		if(mapResult.size()>0)
	    		{
	    			/*** Saved output from plugin detection  ***/
		    		Mat matOutput = (Mat) mapResult.get(ObjDetBasePlugin._KEY_OUTPUT_FRAME_ANNOTATED_IMG);
					savedOutputImage(sTestCycle, detector.getPluginName(), fileImg, matOutput);
	    		}
	    	}
    	}
	
    }
    
    
}