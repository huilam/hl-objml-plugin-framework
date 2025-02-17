package hl.objml2.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DetectedObjUtil {
	
	
	public static Mat annotateImage(final Mat aMatInput, final FrameDetectedObj aDetectedObjs)
	{
		return annotateImage(aMatInput, aDetectedObjs, null);
	}
	
	public static Mat annotateImage(final Mat aMatInput, final FrameDetectedObj aDetectedObjs, Map<String, Scalar> mapObjColors)
	{
		return annotateImage(aMatInput, aDetectedObjs, mapObjColors, true);
	}
	
	public static Mat annotateImage(final Mat aMatInput, final FrameDetectedObj aDetectedObjs, Map<String, Scalar> mapObjColors, boolean withLabel)
	{
		return annotateImage(aMatInput, aDetectedObjs, mapObjColors, withLabel, true);
	}
	
	public static Mat annotateImage(final Mat aMatInput, final FrameDetectedObj aDetectedObjs, Map<String, Scalar> mapObjColors, boolean withLabel, boolean isPointsConnected)
	{
		// Draw bounding boxes
		Mat matOutputImg = aMatInput.clone();
		
		if(mapObjColors==null)
			mapObjColors = new HashMap<String, Scalar>();

		int iThickness = 2;
		
		for(String sObjClassName : aDetectedObjs.getObjClassNames())
		{
			List<MatOfPoint> listShapes = new ArrayList<>();
			List<DetectedObj> listDetectedObjs = aDetectedObjs.getDetectedObjByObjClassName(sObjClassName);
			for(DetectedObj o : listDetectedObjs)
			{
				String objClassName 	= o.getObj_classname();
				String objTrackingId 	= o.getObj_trackingid();
				double objConfScore 	= o.getObj_conf_score();
				MatOfPoint objShape 	= o.getObj_shape_points();
				//
				boolean isNewTrackingId = o.getObj_tmp_trackingid()==null;
				
				Scalar objColor = mapObjColors.get(objClassName);
				if(objColor==null)
					objColor = isNewTrackingId? new Scalar(0, 255, 0): new Scalar(0, 0, 255);

				if(objShape.toArray().length==1)
				{
					Point pt = objShape.toArray()[0];
					Imgproc.circle(matOutputImg, pt, 2, objColor, Imgproc.FILLED, Imgproc.LINE_AA, 0);
				}
				else
				{
					listShapes.clear();
					listShapes.add(objShape);
		            Imgproc.polylines(matOutputImg, listShapes, isPointsConnected, objColor, iThickness);
				}

	            if(withLabel)
	            {
	            	Rect2d objBox 	= o.getObj_bounding_box();
	            	Point ptXY1 	= new Point(objBox.x, objBox.y);
	            	//Point ptXY2 	= new Point(objBox.x + objBox.width, objBox.y + objBox.height);
	            	
		            String label 	= (objTrackingId!=null?objTrackingId:objClassName) + ": " + String.format("%.2f", objConfScore);
		            Imgproc.putText(matOutputImg, label, new Point(ptXY1.x, ptXY1.y - 10), 
		            		Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, objColor, 2);
	            }
			}
		
		}
        //
		return matOutputImg;
	}
	
	public static boolean updTrackingIdWithPrevDetections(DetectedObj aCurObj, final FrameDetectedObj aPrevObjs, double aThreshold)
	{
		Rect2d rectCurObj = aCurObj.getObj_bounding_box();
		if(rectCurObj!=null && aPrevObjs!=null)
		{
			double dMaxScore 			= 0;
			String sMatchedTrackingId 	= null;
			//
			for(String aClassName : aPrevObjs.getObjClassNames())
			{
				List<DetectedObj> listObjClasses = aPrevObjs.getDetectedObjByObjClassName(aClassName);
				for(DetectedObj objPrev : listObjClasses)
				{
					Rect2d rectPrev = objPrev.getObj_bounding_box();
					//
					double dCurOverlapScore = calcIntersectionScore(rectCurObj, rectPrev);
					//
					double dCurScore = dCurOverlapScore;
					//
					if(dCurScore > dMaxScore)
					{
						String trackingIdPrev = objPrev.getObj_trackingid();
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
				aCurObj.setObj_tmp_trackingid(aCurObj.getObj_trackingid());
				aCurObj.setObj_trackingid(sMatchedTrackingId);
				return true;
			}
		}
		return false;
	}
	
	public static Map<Point, Float> getTopDetectionsFor2DMat(
	    		int iTopN, final Mat matOutput,final double aConfidenceThreshold)
    {
    	if (matOutput.empty() || matOutput.channels() != 1) 
    	{
    	    System.err.println("Error: matOutput must be a single-channel matrix!");
    	    return null;
    	}
		
		Map<Point, Float> mapTopNDetections = new HashMap<Point, Float> ();
		
		if(iTopN<=0)
			iTopN = Integer.MAX_VALUE;
		
		Mat matTmpOutput = null;
		
		try {
	    	matTmpOutput = matOutput.clone();
	    	
	    	for(int n=0; n<iTopN; n++)
	    	{
	    		// Use OpenCV's minMaxLoc() to find highest confidence
	            Core.MinMaxLocResult mmr = Core.minMaxLoc(matTmpOutput);
	            float confidence = (float) normalizeConfidenceScore(mmr.maxVal);
	            
	            if(confidence>aConfidenceThreshold)
	            {
	                int x = (int) mmr.maxLoc.x; 
	                int y = (int) mmr.maxLoc.y;
	                
	                mapTopNDetections.put(new Point(x,y), Float.valueOf(confidence));
	                matTmpOutput.put(y, x, -1);
	            }
	            else
	            {
	            	break;
	            }
	    	}
		}
		finally
		{
			if(matTmpOutput!=null) 
				matTmpOutput.release();
		}
		return mapTopNDetections;
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
    
    protected static double normalizeConfidenceScore(double rawConfidence)
    {
    	return rawConfidence > 1.0 ? (1.0 / (1.0 + Math.exp(-rawConfidence))) : rawConfidence;
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