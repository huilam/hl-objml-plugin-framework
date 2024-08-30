package hl.objml2.common;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Rect2d;
import org.opencv.dnn.Dnn;

import hl.opencv.util.OpenCvUtil;

public class ObjDetectionList {
	
	private List<Integer> listObjClassId 	= new ArrayList<>();
	private List<String> listObjClassName 	= new ArrayList<>();
	private List<Float> listObjConfidence 	= new ArrayList<>();
	private List<Rect2d> listBoundingBox 	= new ArrayList<>();

	public void addDetectedObjToList(int aObjClassId, String aObjClassName, float aObjConfScore, Rect2d aObjRectBox)
	{
		if(aObjClassName==null)
			aObjClassName = "undefined";
		
		this.listObjClassId.add(aObjClassId);
		this.listObjClassName.add(aObjClassName);
		this.listObjConfidence.add(aObjConfScore);
		this.listBoundingBox.add(aObjRectBox);
	}
	
	public void addObjDetectionList(ObjDetectionList aObjDetectionList)
	{
		this.listObjClassId.addAll(aObjDetectionList.getObjClassIdList());
		this.listObjClassName.addAll(aObjDetectionList.getObjClassNameList());
		this.listObjConfidence.addAll(aObjDetectionList.getConfidenceScoreList());
		this.listBoundingBox.addAll(aObjDetectionList.getBoundingBoxList() );
	}
	
	public void clear()
	{
		this.listObjClassId.clear();
		this.listObjClassName.clear();
		this.listObjConfidence.clear();
		this.listBoundingBox.clear();
	}
	
	public List<Integer> getObjClassIdList()
	{
		return this.listObjClassId;
	}
	
	public List<String> getObjClassNameList()
	{
		return this.listObjClassName;
	}
		
	public List<Float> getConfidenceScoreList()
	{
		return this.listObjConfidence;
	}
	
	public List<Rect2d> getBoundingBoxList()
	{
		return this.listBoundingBox;
	}
	
	
	////	
	
	public ObjDetectionList performNMSBoxes(float aConfidenceThreshold, float aNmsThreshold)
	{
		ObjDetectionList listNMSResult = new ObjDetectionList();
		
		List<Integer> listObjClassId 	= this.listObjClassId;
		List<Float> listObjConfidence 	= this.listObjConfidence;
		List<Rect2d> listObjRectBoxes 	= this.listBoundingBox;
		
		int[] iIndexes = performNMSBoxes(
				listObjClassId, 
				listObjConfidence, 
				listObjRectBoxes,
				aConfidenceThreshold, aNmsThreshold);
		
		if(iIndexes!=null)
		{
			for(int i=0; i<iIndexes.length; i++)
			{
				int iObjClassId = listObjClassId.get(i);
				String sObjClassName = listObjClassName.get(i);
				
				listNMSResult.addDetectedObjToList(
						iObjClassId, 
						sObjClassName,
						listObjConfidence.get(i),
						listObjRectBoxes.get(i));
			}
		}
		
		return listNMSResult;
	}
	
	private int[] performNMSBoxes(
			List<Integer> aListObjClassId, List<Float> aListObjConfidence, List<Rect2d> aListBoxes, 
			float aConfidenceThreshold, float aNmsThreshold)
	{
        MatOfInt indices = new MatOfInt();
        if(aListBoxes!=null && aListBoxes.size()>0)
        {
	        // Apply Non-Maximum Suppression
	        MatOfRect2d boxesMat = new MatOfRect2d();
	        boxesMat.fromList(aListBoxes);
	        
	        MatOfFloat confidencesMat = new MatOfFloat();
	        confidencesMat.fromList(aListObjConfidence);
	        
	        Dnn.NMSBoxes(boxesMat, confidencesMat, aConfidenceThreshold, aNmsThreshold, indices);
        }
        
        if(!indices.empty())
        	return indices.toArray();
        else
        	return new int[]{};
	}
	
	public static void main(String[] args)
	{
	}
}