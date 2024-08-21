package hl.plugin.image;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;

public class ObjDetection {
	
	protected final static String OBJCLASS_ID				= "obj_class_id";
	protected final static String OBJCLASS_NAME 			= "obj_class_name";
	protected final static String OBJCLASS_CONF_SCORE 		= "obj_conf_score";
	protected final static String OBJCLASS_BOUNDING_BOX 	= "obj_bounding_box";
	//
	protected final static String OBJCLASS_TRACKING_ID 		= "obj_tracking_id";
	protected final static String OBJCLASS_PREV_TRACKING_ID = "obj_prev_tracking_id";
	
	//protected final static String OBJCLASS_MAT 				= "obj_mat";
	//protected final static String OBJCLASS_SHAPE 			= "objclass_shape";
	
	private JSONObject jsonDetection = new JSONObject();

	
	///////////////////
	public static long getObjClassId(JSONObject json)
	{
		return json!=null?(long)getAttribute(json,OBJCLASS_ID):-1;
	}
	
	public static String getObjClassName(JSONObject json)
	{
		return json!=null?(String)getAttribute(json,OBJCLASS_NAME):null;
	}
	
	public static double getConfidenceScore(JSONObject json)
	{
		return json!=null?(double)getAttribute(json,OBJCLASS_CONF_SCORE):-1;
	}
	
	public static Rect2d getBoundingBox(JSONObject json)
	{
		return json!=null?(Rect2d)getAttribute(json,OBJCLASS_BOUNDING_BOX):null;
	}
	
	public static String getObjTrackingId(JSONObject json)
	{
		return json!=null?(String)getAttribute(json,OBJCLASS_TRACKING_ID):null;
	}
	
	public static boolean isNewObjTrackingId(JSONObject json)
	{
		String sPrevTrackingId = json.optString(OBJCLASS_PREV_TRACKING_ID, null);
		return (sPrevTrackingId!=null);
	}
	
	public static void updObjTrackingId(JSONObject json, String aTrackingId)
	{
		if(json!=null && aTrackingId!=null && aTrackingId.trim().length()>0)
		{
			String sPrevTrackingId = json.optString(OBJCLASS_TRACKING_ID, null);
			
			if(sPrevTrackingId!=null && !aTrackingId.equals(sPrevTrackingId))
			{
				json.put(OBJCLASS_PREV_TRACKING_ID, sPrevTrackingId);
			}
			json.put(OBJCLASS_TRACKING_ID, aTrackingId);
		}
	}
	
	/**
	public static Mat getObjMat(JSONObject json)
	{
		return json!=null?(Mat)getAttribute(json,OBJCLASS_MAT):null;
	}
	**/
	
	protected static Object getAttribute(JSONObject aJson, String aAttrName)
	{
		return aJson!=null?aJson.opt(aAttrName):null;
	}
	/////////////////////
	
	
	public void addAll(JSONObject aInputJson)
	{
		if(aInputJson!=null)
		{	
			for(String sObjClassName : aInputJson.keySet())
			{
				JSONArray jArrClassName = aInputJson.optJSONArray(sObjClassName);
				
				if(jArrClassName!=null && jArrClassName.length()>0)
				{
					for(int i=0; i<jArrClassName.length(); i++)
					{
						JSONObject jsonObj = jArrClassName.getJSONObject(i);
						long lObjClassId = jsonObj.optLong(OBJCLASS_ID, -1);
						double dObjClassConfScore = jsonObj.optDouble(OBJCLASS_CONF_SCORE, -1);
						Rect2d rObjClassRect2d = (Rect2d)jsonObj.opt(OBJCLASS_BOUNDING_BOX);
						//
						//Mat matObj = (Mat)jsonObj.opt(OBJCLASS_MAT);
						String sObjTrackingId = jsonObj.optString(OBJCLASS_TRACKING_ID, null);
						//
						addDetectedObj(lObjClassId, sObjClassName, dObjClassConfScore, rObjClassRect2d, 
								sObjTrackingId);
					}
				}
				
			
			}
		}
	}
	
	public long removeDetectedObjByObjClassName(String aObjClassName)
	{
		JSONArray jArrObjClass = (JSONArray) this.jsonDetection.remove(aObjClassName);
		if(jArrObjClass==null)
			jArrObjClass = new JSONArray();
		
		return jArrObjClass.length();
	}
		
	public JSONObject[] getDetectedObjByObjClassName(String aObjClassName)
	{
		JSONArray jArrObjClass = this.jsonDetection.optJSONArray(aObjClassName);
		if(jArrObjClass==null)
			jArrObjClass = new JSONArray();
		
		List<JSONObject> listDetectedObj = new ArrayList<JSONObject>();
		for(int i=0; i<jArrObjClass.length(); i++)
		{
			listDetectedObj.add((JSONObject)jArrObjClass.get(i));
		}
		
		return (JSONObject[]) listDetectedObj.toArray(new JSONObject[listDetectedObj.size()]);
	}
	
	public String[] getObjClassNames()
	{
		List<String> listClassName = new ArrayList<String>();
		for(String sClassName : this.jsonDetection.keySet())
		{
			listClassName.add(sClassName);
		}
		
		return (String[]) listClassName.toArray(new String[listClassName.size()]);
	}
	
	public void clearDetection()
	{
		this.jsonDetection.clear();
	}
	
	public long getTotalDetectionCount()
	{
		long lTotal = 0;
		for(String sKey : this.jsonDetection.keySet())
		{
			JSONArray jsonArray = this.jsonDetection.getJSONArray(sKey);
			lTotal += jsonArray.length();
			
		}
		return lTotal;
	}
	
	public long getDetectionCount(String aObjClassName)
	{
		JSONArray jArrObjClass = this.jsonDetection.optJSONArray(aObjClassName);
		if(jArrObjClass==null)
    	{
    		jArrObjClass = new JSONArray();
    	}
		return jArrObjClass.length();
	}
	
	public boolean addDetectedObj(long aObjClassId, String aObjClassName, double aConfScore, Rect aRect)
	{
		 return addDetectedObj(aObjClassId, aObjClassName, aConfScore, new Rect2d(aRect.x, aRect.y, aRect.width, aRect.height));
	}
	
	public boolean addDetectedObj(long aObjClassId, String aObjClassName, double aConfScore, Rect2d aRect2D)
    {
		return addDetectedObj(aObjClassId, aObjClassName, aConfScore, aRect2D, null);
    }
	
    public boolean addDetectedObj(long aObjClassId, String aObjClassName, double aConfScore, Rect2d aRect2D, 
    		String aTrackingId)
    {
    	JSONArray jArrObjClass = this.jsonDetection.optJSONArray(aObjClassName);
    	if(jArrObjClass==null)
    	{
    		jArrObjClass = new JSONArray();
    		this.jsonDetection.put(aObjClassName, jArrObjClass);
    	}
    	
    	JSONObject jsonObj = new JSONObject();
    	jsonObj.put(OBJCLASS_ID, aObjClassId);
    	jsonObj.put(OBJCLASS_NAME, aObjClassName);
    	jsonObj.put(OBJCLASS_CONF_SCORE, aConfScore);
    	jsonObj.put(OBJCLASS_BOUNDING_BOX, aRect2D);
    	//
    	if(aTrackingId!=null && aTrackingId.trim().length()>0)
    	{
    		jsonObj.put(OBJCLASS_TRACKING_ID, aTrackingId);
    	}
    	//
    	/**
    	if(aObjMat!=null && !aObjMat.empty())
    	{
    		jsonObj.put(OBJCLASS_MAT, aObjMat);
    	}
    	**/
    	//
    	jsonObj = addDetectedObj_Extended(jsonObj);
    	jArrObjClass.put(jsonObj);
    
    	return true;
    }
	
	public JSONObject toJson()
	{
		return this.jsonDetection;
	}
	
	public String toString()
	{
		return this.jsonDetection.toString();
	}
	
	//
	
	protected JSONObject addDetectedObj_Extended(JSONObject aJson)
	{
		return aJson;
	}
	
	/**
	public static void main(String[] args)
	{
		ObjDetection objs = new ObjDetection();
		objs.addDetectedObj(0, "person", 0.52, new Rect2d(0,10,20,40));
		objs.addDetectedObj(0, "person", 0.78, new Rect2d(11,100,60,40));
		
		objs.addDetectedObj(2, "cat", 0.77, new Rect2d(100,145,20,30));
		objs.addDetectedObj(33, "bicycle", 0.37, new Rect2d(211,430,300,50));
		
		ObjDetection objs2 = new ObjDetection();
		objs2.addAll(objs.toJson());
		
		System.out.println("ClassNames="+ String.join(",", objs2.getObjClassNames()));
		System.out.println("Total="+objs2.getTotalDetectionCount());
		System.out.println();
		System.out.println("person="+objs2.getDetectionCount("person"));
		System.out.println("bicycle="+objs2.getDetectionCount("bicycle"));
		System.out.println("cat="+objs2.getDetectionCount("cat"));
		System.out.println("dog="+objs2.getDetectionCount("dog"));
		
		JSONObject[] persons = objs2.getDetectedObjByObjClassName("person");
		for(JSONObject json : persons)
		{
			System.out.println(json);
		}
		
		long lRemoved =  objs2.removeDetectedObjByObjClassName("person");
		System.out.println("removed = "+lRemoved);
		persons = objs2.getDetectedObjByObjClassName("person");
		System.out.println("person = "+persons.length);
		
	}
	**/
}