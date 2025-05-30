package hl.objml2.plugin.test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opencv.core.Mat;
import hl.common.FileUtil;
import hl.objml2.common.FrameDetectedObj;
import hl.objml2.common.FrameDetectionMeta;
import hl.objml2.plugin.IObjDetectionPlugin;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.util.OpenCvUtil;

public class BaseTester {
	
	private String 	 DEF_FOLDER_IMAGE 			= "./test/images/";
	private String[] DEF_IMG_FILE_EXTS 			= new String[]{".bmp", ".png",".jpeg",".jpg"};
	private String 	 DEF_OUTPUT_IMG_FORMAT_EXT 	= "png";
	
	private String 	 FOLDER_IMAGE = DEF_FOLDER_IMAGE;
	private String[] IMGFILE_EXTS = DEF_IMG_FILE_EXTS;
	
	public void setOutputImageExtension(String aImgExt)
	{
		this.DEF_OUTPUT_IMG_FORMAT_EXT = aImgExt;
	}
	
	public void setTestImageFolder(String aImageFolder)
	{
		setTestImageFolder(aImageFolder, null);
	}
	
	public void setTestImageFolder(String aImageFolder, String[] aImgExts)
	{
		this.FOLDER_IMAGE = aImageFolder;
		this.IMGFILE_EXTS = aImgExts;
		if(aImgExts==null || aImgExts.length==0)
		{
			this.IMGFILE_EXTS = DEF_IMG_FILE_EXTS;
		}
	}
	
	public File[] getTestImageFiles()
	{
		File folderImages = new File(FOLDER_IMAGE);
		
		if(folderImages.isDirectory())
		{
			return FileUtil.getFilesWithExtensions(
					folderImages, 
					IMGFILE_EXTS);
		}
		else
		{
			return new File[] {};
		}
		
	}
	
	public String saveImage(
			String aPluginName,
			Mat aMatImage, File aOutputFolder, String aOrigImgFileName)
	{
		if(!aOutputFolder.exists()) 
			aOutputFolder.mkdirs();
		
		String sOutputFileName = aPluginName+"_"+aOrigImgFileName;
		
		sOutputFileName += "."+DEF_OUTPUT_IMG_FORMAT_EXT;
	
		boolean isSaved = OpenCvUtil.saveImageAsFile(aMatImage, aOutputFolder.getAbsolutePath()+"/"+sOutputFileName);
		
		if(isSaved)
			return aOutputFolder.getName()+"/"+sOutputFileName;
		return null;
	}
	
	public FrameDetectedObj testDetector(IObjDetectionPlugin aDetector)
	{
		FrameDetectedObj frameObjs 	= null;
		
		
		OpenCvUtil.initOpenCV();
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(aDetector.isPluginOK())
		{
			System.out.println(" Detector : "+aDetector.getPluginName()+" ("+aDetector.getPluginMLModelFileName()+")");
			System.out.println("   isPluginOK : "+aDetector.isPluginOK());

			Properties prop = aDetector.getPluginProps();
			
			int iKeyPrefix = "objml.mlmodel.".length();
			for(Object oKey : prop.keySet())
			{
				String sVal = prop.getProperty(oKey.toString());
				if(sVal!=null && sVal.trim().length()>0)
				{
					sVal = sVal.replace("\n", " ");
					
					if(sVal.length()>60) sVal = sVal.substring(0, 60)+" ... (truncated)";
					
					String sKey = oKey.toString().substring(iKeyPrefix);
					System.out.println("     - prop:"+sKey+" : "+sVal);
				}
			}
			
			File fileFolder = new File("./test/images/output/"+System.currentTimeMillis());
			
			int i = 1;
			
			ObjDetBasePlugin pluginDetector = (ObjDetBasePlugin) aDetector;
			
			for(File fImg : getTestImageFiles())
			{
				System.out.println();
				System.out.print("    "+(i++)+". Perform test on "+fImg.getName()+" ... ");
				
				Mat matImg = ObjDetBasePlugin.getCvMatFromFile(fImg);
				
				long lInferenceStart = System.currentTimeMillis();
				
				List<Mat> listInferOutput = pluginDetector.doInference(matImg, pluginDetector.getDnnNet());
				
				MLPluginFrameOutput frameOutput = pluginDetector.parseDetections(matImg, listInferOutput);
				
				long lInferenceEnd = System.currentTimeMillis();
				
				Map<String, Object> mapResult = frameOutput.getDetectionOutputMap();
				if(mapResult!=null)
				{
					long lInferenceMs =  lInferenceEnd-lInferenceStart;
					
					
					System.out.println();
					System.out.println("     - Inference Model File : "+new File(aDetector.getPluginMLModelFileName()).getName());
					System.out.println("     - Inference Input Size : "+pluginDetector.getImageInputSize().toString());
					System.out.println("     - Inference Confidence Threshold : "+pluginDetector.getConfidenceThreshold());
					System.out.println("     - Inference NMS Threshold : "+pluginDetector.getNMSThreshold());
					System.out.println("     - Inference Backend    : "+pluginDetector.getDnnBackendDesc());
					System.out.println("     - Inference Target     : "+pluginDetector.getDnnTargetDesc());
					System.out.println("     - Inference Time       : "+lInferenceMs+ " ms");
					
					System.out.println("     - Inference Outputs    : ");
					int outputIdx=0;
					for(Mat matOutput : listInferOutput)
					{
						String sMatDims = matOutput.toString();
						int iPos = sMatDims.indexOf(",");
						if(iPos>-1)
						{
							sMatDims = sMatDims.substring(0, iPos) +" ]";
						}
						
					System.out.println("        "+(outputIdx++)+" : "+sMatDims);	
					}
					
			
					frameObjs 		= (FrameDetectedObj) mapResult.get(ObjDetBasePlugin._KEY_OUTPUT_FRAME_DETECTIONS);
					if(frameObjs!=null)
					{
						//
						System.out.println("     - ObjClass Names : "+String.join(",", frameObjs.getObjClassNames()));
						System.out.println("     - Total Detection : "+frameObjs.getFrame_total_detection());
					}
					else
					{
						FrameDetectionMeta meta = (FrameDetectionMeta) mapResult.get(ObjDetBasePlugin._KEY_OUTPUT_FRAME_DETECTION_META);
						if(meta!=null)
						{
							System.out.println("     - Total Detection : 1");
						}
						else
						{
							System.out.println("     - Total Detection : (No Detection Data)");
						}
					}
		
					Mat matOutput = (Mat) mapResult.get(ObjDetBasePlugin._KEY_OUTPUT_FRAME_ANNOTATED_IMG);
					if(matOutput!=null && !matOutput.empty())
					{
						String savedFileName = 
								saveImage(aDetector.getPluginName(), 
								matOutput, 
								fileFolder, fImg.getName());
						
						if(savedFileName!=null)
							System.out.println("     - [saved] "+savedFileName);
					}
				}
				else
				{
					System.out.println("     - No result found.");
				}
			}
			
		}	
		
		return frameObjs;
	}
	
}