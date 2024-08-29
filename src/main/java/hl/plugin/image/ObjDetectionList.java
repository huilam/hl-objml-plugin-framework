package hl.plugin.image;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Rect2d;
import org.opencv.dnn.Dnn;

public class ObjDetectionList {
	
	private List<Integer> listObjClassId 	= new ArrayList<>();
	private List<Float> listObjConfidence 	= new ArrayList<>();
	private List<Rect2d> listBoundingBox 	= new ArrayList<>();

	public void addDetectedObjToList(int aObjClassId, float aObjConfScore, Rect2d aObjRectBox)
	{
		this.listObjClassId.add(aObjClassId);
		this.listObjConfidence.add(aObjConfScore);
		this.listBoundingBox.add(aObjRectBox);
	}
	
	public void addObjDetectionList(ObjDetectionList aObjDetectionList)
	{
		this.listObjClassId.addAll(aObjDetectionList.getObjClassIdList());
		this.listObjConfidence.addAll(aObjDetectionList.getConfidenceScoreList());
		this.listBoundingBox.addAll(aObjDetectionList.getBoundingBoxList() );
	}
	
	public void clear()
	{
		this.listObjClassId.clear();
		this.listObjConfidence.clear();
		this.listBoundingBox.clear();
	}
	
	public List<Integer> getObjClassIdList()
	{
		return this.listObjClassId;
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
				listNMSResult.addDetectedObjToList(
						listObjClassId.get(i), 
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
        return indices.toArray();
	}
	
	public static void main(String[] args)
	{
	}
}