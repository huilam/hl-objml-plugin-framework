package hl.objml2.plugin.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	
	private boolean isSaveOutputMatAsFile = false; 
	private BufferedWriter outputFile = null; 
	
	
	public void setIsAutoSaveOutputMatAsFile(boolean aIsSaved)
	{
		isSaveOutputMatAsFile = aIsSaved;
	}
	
	public boolean getIsAutoSaveOutputMatAsFile()
	{
		return isSaveOutputMatAsFile;
	}
	
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
			prnln(" Detector : "+aDetector.getPluginName()+" ("+aDetector.getPluginMLModelFileName()+")");
			prnln("   isPluginOK : "+aDetector.isPluginOK());

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
					prnln("     - prop:"+sKey+" : "+sVal);
				}
			}
			
			String sExecutionTS = ""+System.currentTimeMillis();
			
			File fileFolder = new File("./test/images/output/"+sExecutionTS);
			fileFolder.mkdirs();
			
			int i = 1;
			
			ObjDetBasePlugin pluginDetector = (ObjDetBasePlugin) aDetector;
			
			
			String sModelFileName = new File(aDetector.getPluginMLModelFileName()).getName();
			File fOutputText = new File(fileFolder.getAbsolutePath()+"/"+sExecutionTS+"_"+sModelFileName+".txt");
			try {
				outputFile = new BufferedWriter(new FileWriter(fOutputText));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			for(File fImg : getTestImageFiles())
			{
				prnln();
				prn("    "+(i++)+". Perform test on "+fImg.getName()+" ... ");
				
				Mat matImg = ObjDetBasePlugin.getCvMatFromFile(fImg);
				
				long lInferenceStart = System.currentTimeMillis();
				
				List<Mat> listInferOutput = pluginDetector.doInference(matImg, pluginDetector.getDnnNet());
				
				MLPluginFrameOutput frameOutput = pluginDetector.parseDetections(matImg, listInferOutput);
				
				long lInferenceEnd = System.currentTimeMillis();
				
				Map<String, Object> mapResult = frameOutput.getDetectionOutputMap();
				if(mapResult!=null)
				{
					long lInferenceMs =  lInferenceEnd-lInferenceStart;
					
					
					prnln();
					prnln("     - Inference Model File : "+sModelFileName);
					prnln("     - Inference Input Size : "+matImg.size()+" -> "+pluginDetector.getImageInputSize().toString());
					prnln("     - Inference Confidence Threshold : "+pluginDetector.getConfidenceThreshold());
					prnln("     - Inference NMS Threshold : "+pluginDetector.getNMSThreshold());
					prnln("     - Inference Backend    : "+pluginDetector.getDnnBackendDesc());
					prnln("     - Inference Target     : "+pluginDetector.getDnnTargetDesc());
					prnln("     - Inference Time       : "+lInferenceMs+ " ms");
					
					prnln("     - Inference Outputs    : ");
					int outputIdx=0;
					for(Mat matOutput : listInferOutput)
					{
						String sMatDims = matOutput.toString();
						int iPos = sMatDims.indexOf(",");
						if(iPos>-1)
						{
							sMatDims = sMatDims.substring(0, iPos) +" ]";
						}
						
					prnln("        "+(outputIdx++)+" : "+sMatDims);	
					}
					
			
					frameObjs 		= (FrameDetectedObj) mapResult.get(ObjDetBasePlugin._KEY_OUTPUT_FRAME_DETECTIONS);
					if(frameObjs!=null)
					{
						//
						prnln("     - ObjClass Names : "+String.join(",", frameObjs.getObjClassNames()));
						prnln("     - Total Detection : "+frameObjs.getFrame_total_detection());
					}
					else
					{
						FrameDetectionMeta meta = (FrameDetectionMeta) mapResult.get(ObjDetBasePlugin._KEY_OUTPUT_FRAME_DETECTION_META);
						if(meta!=null)
						{
							prnln("     - Total Detection : 1");
						}
						else
						{
							prnln("     - Total Detection : (No Detection Data)");
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
							prnln("     - [saved] "+savedFileName);
						
						if(getIsAutoSaveOutputMatAsFile())
						{
							for(String sOutputKey : mapResult.keySet())
							{
								Object objOutput = mapResult.get(sOutputKey);
								if(!sOutputKey.contentEquals(ObjDetBasePlugin._KEY_OUTPUT_FRAME_ANNOTATED_IMG))
								{
									if(objOutput instanceof Mat)
									{
										String savedFileName2 = 
												saveImage(aDetector.getPluginName(), 
														  (Mat)objOutput, 
														  fileFolder, fImg.getName()+"_"+sOutputKey);
										if(savedFileName2!=null)
											prnln("     - [saved] "+savedFileName);
									}
								}
								
							}
						}
					}
					
					
				}
				else
				{
					prnln("     - No result found.");
				}
			}
			
			if(outputFile!=null)
			{
				try {
					outputFile.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					outputFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				outputFile = null;
			}
		}	
		
		return frameObjs;
	}
	
	private void prn(String aPrintText)
	{
		System.out.print(aPrintText);
		if(outputFile!=null)
		{
			try {
				outputFile.write(aPrintText);
			} catch (IOException e) {
				outputFile = null;
				e.printStackTrace();
			}
		}
	}
	
	private void prnln(String aPrintText)
	{
		prn(aPrintText+"\n");
	}
	
	private void prnln()
	{
		prnln("");
	}
}