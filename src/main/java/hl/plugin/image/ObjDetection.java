package hl.plugin.image;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Rect2d;

public class ObjDetection {
	
	public final static String OBJCLASS_ID				= "objclass_id";
	public final static String OBJCLASS_NAME 			= "objclass_name";
	public final static String OBJCLASS_CONF_SCORE 		= "objclass_conf_score";
	public final static String OBJCLASS_BOUNDING_BOX 	= "objclass_bounding_box";
	//public final static String OBJCLASS_SHAPE 			= "objclass_shape";
	
	private JSONObject jsonDetection = new JSONObject();

	
	public void clearDetection()
	{
		jsonDetection.clear();
	}
	
	public long getTotalDetectionCount()
	{
		long lTotal = 0;
		for(String sKey : jsonDetection.keySet())
		{
			JSONArray jsonArray = jsonDetection.getJSONArray(sKey);
			lTotal += jsonArray.length();
			
		}
		return lTotal;
	}
	
	public long getDetectionCount(String aObjClassName)
	{
		JSONArray jArrObjClass = jsonDetection.optJSONArray(aObjClassName);
		if(jArrObjClass==null)
    	{
    		jArrObjClass = new JSONArray();
    	}
		return jArrObjClass.length();
	}
	
    public boolean addDetectedObj(long aObjClassId, String aObjClassName, double aConfScore, Rect2d aRect2D)
    {
    	JSONArray jArrObjClass = jsonDetection.optJSONArray(aObjClassName);
    	if(jArrObjClass==null)
    	{
    		jArrObjClass = new JSONArray();
    		jsonDetection.put(aObjClassName, jArrObjClass);
    	}
    	
    	JSONObject jsonObj = new JSONObject();
    	jsonObj.put(OBJCLASS_ID, aObjClassId);
    	jsonObj.put(OBJCLASS_NAME, aObjClassName);
    	jsonObj.put(OBJCLASS_CONF_SCORE, aConfScore);
    	jsonObj.put(OBJCLASS_BOUNDING_BOX, aRect2D);
	
    	jArrObjClass.put(jsonObj);
    
    	return true;
    }
	
	public JSONObject toJson()
	{
		return jsonDetection;
	}
	
	/**
	public static void main(String[] args)
	{
		ObjDetection objs = new ObjDetection();
		objs.addDetectedObj(0, "person", 0.52, new Rect2d(0,10,20,40));
		objs.addDetectedObj(0, "person", 0.78, new Rect2d(11,100,60,40));
		
		objs.addDetectedObj(2, "cat", 0.77, new Rect2d(100,145,20,30));
		objs.addDetectedObj(33, "bicycle", 0.37, new Rect2d(211,430,300,50));
		
		
		System.out.println("Total="+objs.getTotalDetectionCount());
		System.out.println("person="+objs.getDetectionCount("person"));
		System.out.println("bicycle="+objs.getDetectionCount("bicycle"));
		System.out.println("cat="+objs.getDetectionCount("cat"));
		System.out.println("dog="+objs.getDetectionCount("dog"));
		
	}
	**/
}