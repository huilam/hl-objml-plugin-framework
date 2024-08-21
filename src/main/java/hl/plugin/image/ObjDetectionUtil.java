package hl.plugin.image;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ObjDetectionUtil {
	
	
	public static Mat annotateImage(final Mat aMatInput, final ObjDetection aDetectedObjs)
	{
		return annotateImage(aMatInput, aDetectedObjs, null);
	}
	
	public static Mat annotateImage(final Mat aMatInput, final ObjDetection aDetectedObjs, Map<String, Scalar> mapObjColors)
	{
		// Draw bounding boxes
		Mat matOutputImg = aMatInput.clone();
		
		if(mapObjColors==null)
			mapObjColors = new HashMap<String, Scalar>();

		for(String sObjClassName : aDetectedObjs.getObjClassNames())
		{
			JSONObject[] jsonDetectedObjs = aDetectedObjs.getDetectedObjByObjClassName(sObjClassName);
			for(JSONObject json : jsonDetectedObjs)
			{
				String objClassName 	= ObjDetection.getObjClassName(json);
				String objTrackingId 	= ObjDetection.getObjTrackingId(json);
				double objConfScore 	= ObjDetection.getConfidenceScore(json);
				Rect2d objBox 			= ObjDetection.getBoundingBox(json);
				
				Scalar objColor = mapObjColors.get(objClassName);
				if(objColor==null)
					objColor = (objTrackingId==null)? new Scalar(0, 255, 0): new Scalar(0, 0, 255);
						
            	Point ptXY1 	= new Point(objBox.x, objBox.y);
            	Point ptXY2 	= new Point(objBox.x + objBox.width, objBox.y + objBox.height);
	            Imgproc.rectangle(matOutputImg, ptXY1, ptXY2, objColor, 2);

	            String label 	= (objTrackingId!=null?objTrackingId:objClassName) + ": " + String.format("%.2f", objConfScore);
	            Imgproc.putText(matOutputImg, label, new Point(ptXY1.x, ptXY1.y - 10), 
	            		Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, objColor, 2);
			}
		
		}
        //
		return matOutputImg;
	}
	
	public static boolean updTrackingIdWithPrevDetections(JSONObject aCurObj, final ObjDetection aPrevObjs, double aThreshold)
	{
		Rect2d rectCurObj = ObjDetection.getBoundingBox(aCurObj);
		if(rectCurObj!=null && aPrevObjs!=null)
		{
			double dMaxScore 			= 0;
			String sMatchedTrackingId 	= null;
			//
			for(String aClassName : aPrevObjs.getObjClassNames())
			{
				JSONObject[] jsonPrevObjs = aPrevObjs.getDetectedObjByObjClassName(aClassName);
				for(JSONObject jsonPrev : jsonPrevObjs)
				{
					Rect2d rectPrev = ObjDetection.getBoundingBox(jsonPrev);
					//
					double dCurOverlapScore = calcIntersectionScore(rectCurObj, rectPrev);
					//double dCurRatioScore = calcWHRatioScore(rectCurObj, rectPrev);
					//
					double dCurScore = dCurOverlapScore;
					//
					if(dCurScore > dMaxScore)
					{
						String trackingIdPrev = ObjDetection.getObjTrackingId(jsonPrev);
						//
						sMatchedTrackingId = trackingIdPrev;
						dMaxScore = dCurScore;
						
						if(dCurScore==0.99)
							break;
					}
				}
			}
			
			if(dMaxScore > aThreshold)
			{
				ObjDetection.updObjTrackingId(aCurObj, sMatchedTrackingId);
				return true;
			}
		}
		return false;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	
    private static double calcIntersectionScore(Rect2d rect1, Rect2d rect2) 
    {
        // Calculate intersection rectangle
        double x1 = Math.max(rect1.x, rect2.x);
        double y1 = Math.max(rect1.y, rect2.y);
        double x2 = Math.min(rect1.x + rect1.width, rect2.x + rect2.width);
        double y2 = Math.min(rect1.y + rect1.height, rect2.y + rect2.height);

        // Calculate width and height of intersection
        double width 	= Math.max(0, x2 - x1);
        double height 	= Math.max(0, y2 - y1);

        double intersectArea = width * height;        
        if(intersectArea>0)
        {
        	// Calculate intersection score
            double maxIntersectArea = Math.min(rect1.width, rect2.width) * Math.min(rect1.height, rect2.height);
            return intersectArea/maxIntersectArea;
        }
        return 0;
    }
    
    private static double calcWHRatioScore(Rect2d rect1, Rect2d rect2) 
    {
        // Calculate w/h ratio
        double dRatio1 = (rect1.width / rect1.height);
        double dRatio2 = (rect2.width / rect2.height);
        
        return 1.0-Math.abs(dRatio1-dRatio2);
    }
	
	
	public static void main(String[] args)
	{
		Rect2d rect1 = new Rect2d(0,0,10,20);
		Rect2d rect2 = new Rect2d(8,18,11,22);
		
		double dIntersectionScore = calcIntersectionScore(rect1, rect2);
		double dWHRatioScore = calcWHRatioScore(rect1, rect2);
		double dScore = (dIntersectionScore*0.9)+(dWHRatioScore*0.1);
		
		System.out.println("dIntersectionScore="+dIntersectionScore);
		System.out.println("dWHRatioScore="+dWHRatioScore);
		System.out.println("dScore="+dScore);
	}

}