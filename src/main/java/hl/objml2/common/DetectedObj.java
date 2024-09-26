package hl.objml2.common;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.imgproc.Imgproc;

public class DetectedObj {
	
	////
	protected final static String OBJCLASS_ID				= "obj_class_id";
	protected final static String OBJCLASS_NAME 			= "obj_class_name";
	protected final static String OBJCLASS_CONF_SCORE 		= "obj_conf_score";
	protected final static String OBJCLASS_SHAPE_POINTS 	= "obj_shape_points";
	protected final static String OBJCLASS_TRACKING_ID 		= "obj_tracking_id";
	////
	protected final static String OBJSHAPE_X				= "x";
	protected final static String OBJSHAPE_Y				= "y";
	
	///////////////////
	private int obj_classid 		= -1;
	private String obj_classname 	= null;
	private double obj_conf_score 	= 0;
	
	private Rect2d obj_bounding_box 	= null;
	private MatOfPoint obj_shape_points = null;
	//
	private String obj_trackingid 		= null;
	private String obj_tmp_trackingid 	= null;
	///////////////////
	
	
	public DetectedObj(int aObjClassId, String aObjClassName, Rect2d aBoundingRect, double aConfScore)
	{
		MatOfPoint matPoints = new MatOfPoint();
		if(aBoundingRect!=null)
		{
			Point[] points = new Point[4];
			points[0] = new Point(aBoundingRect.x, aBoundingRect.y);
			points[1] = new Point(aBoundingRect.x + aBoundingRect.width, aBoundingRect.y);
			points[2] = new Point(aBoundingRect.x + aBoundingRect.width, aBoundingRect.y + aBoundingRect.height);
			points[3] = new Point(aBoundingRect.x,  aBoundingRect.y + aBoundingRect.height);
			matPoints.fromArray(points);
		}
		
		init(aObjClassId, aObjClassName, matPoints, aConfScore);
	}
	
	public DetectedObj(int aObjClassId, String aObjClassName, List<Point> aShapePointList, double aConfScore)
	{
		MatOfPoint matPoints = new MatOfPoint();
		if(aShapePointList!=null)
		{
			matPoints.fromList(aShapePointList);
		}
		init(aObjClassId, aObjClassName, matPoints, aConfScore);
	}
	
	public DetectedObj(int aObjClassId, String aObjClassName, MatOfPoint aMatShapePoint, double aConfScore)
	{
		init(aObjClassId, aObjClassName, aMatShapePoint, aConfScore);
	}
	
	private void init(int aObjClassId, String aObjClassName, MatOfPoint aMatShapePoint, double aConfScore)
	{
		setObj_classid(aObjClassId);
		setObj_classname(aObjClassName);
		setObj_conf_score(aConfScore);
		setObj_shape_points(aMatShapePoint);
	}
	
	///////////////////
	
	public int getObj_classid() {
		return obj_classid;
	}
	public void setObj_classid(int obj_classid) {
		this.obj_classid = obj_classid;
	}
	public String getObj_classname() {
		return obj_classname;
	}
	public void setObj_classname(String obj_classname) {
		this.obj_classname = obj_classname;
	}
	public double getObj_conf_score() {
		return obj_conf_score;
	}
	public void setObj_conf_score(double obj_conf_score) {
		this.obj_conf_score = obj_conf_score;
	}
	public MatOfPoint getObj_shape_points() {
		return obj_shape_points;
	}
	public void setObj_shape_points(MatOfPoint obj_shapepoints) {
		this.obj_shape_points = obj_shapepoints;
		
		//calc bounding box
		if(this.obj_shape_points!=null)
		{
			Rect rect = Imgproc.boundingRect(getObj_shape_points());
			this.obj_bounding_box = new Rect2d(rect.x, rect.y, rect.width, rect.height);
		}
		else
		{
			this.obj_bounding_box = null;
		}
	}
	
	public void setObj_bounding_box(Rect2d r) {
		
		if(r!=null)
		{
			Point pt1 = new Point(r.x, r.y);
			Point pt2 = new Point(r.x+r.width, r.y);
			Point pt3 = new Point(r.x+r.width, r.y+r.height);
			Point pt4 = new Point(r.x, r.y+r.height);
			//
			MatOfPoint mp = new MatOfPoint();
			mp.fromArray(new Point[] {pt1, pt2, pt3, pt4});
			setObj_shape_points(mp);
		}
	}
	
	public Rect2d getObj_bounding_box() {
		
		return this.obj_bounding_box;
	}
	//
	public String getObj_trackingid() {
		return obj_trackingid;
	}
	public void setObj_trackingid(String obj_trackingid) {
		this.obj_trackingid = obj_trackingid;
	}
	public String getObj_tmp_trackingid() {
		return obj_tmp_trackingid;
	}
	public void setObj_tmp_trackingid(String obj_tmp_trackingid) {
		this.obj_tmp_trackingid = obj_tmp_trackingid;
	}
	

	////////////////////////////////////////////////
	////////////////////////////////////////////////
	
	public DetectedObj fromJson(JSONObject aJson)
	{
		if(aJson==null || aJson.isEmpty())
			return null;
		//
		setObj_classid(aJson.optInt(OBJCLASS_ID, -1));
		setObj_classname(aJson.optString(OBJCLASS_NAME, null));
		setObj_conf_score(aJson.optDouble(OBJCLASS_CONF_SCORE, 0));
		//
		JSONArray jArrShape = aJson.optJSONArray(OBJCLASS_SHAPE_POINTS);
		if(jArrShape!=null && !jArrShape.isEmpty())
		{
			List<Point> listPoints = new ArrayList<>();
			for(int p=0 ; p<jArrShape.length(); p++)
			{
				JSONObject jsonPt = jArrShape.optJSONObject(p);
				if(jsonPt!=null)
				{
					double x 	= jsonPt.optLong(OBJSHAPE_X, -1);
					double y 	= jsonPt.optLong(OBJSHAPE_Y, -1);
					//
					if(x>-1 && y>-1)
					{
						listPoints.add(new Point(x,y));
					}
					
				}
			}
			if(listPoints.size()>0)
			{
				MatOfPoint mp = new MatOfPoint();
				mp.fromList(listPoints);
				setObj_shape_points(mp);
			}
		}
		//
		return this;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(OBJCLASS_ID, getObj_classid());
		json.put(OBJCLASS_NAME, getObj_classname());
		json.put(OBJCLASS_CONF_SCORE, getObj_conf_score());
		
		MatOfPoint mp = getObj_shape_points();
		if(mp!=null && !mp.empty())
		{
			JSONArray jArrPoints = new JSONArray();
			List<Point> listPoints = mp.toList();
			for(Point p : listPoints)
			{
				JSONObject jsonPt = new JSONObject();
				jsonPt.put(OBJSHAPE_X, p.x);
				jsonPt.put(OBJSHAPE_Y, p.y);
				jArrPoints.put(jsonPt);
			}
			json.put(OBJCLASS_SHAPE_POINTS, jArrPoints);
		}
		//
		if(getObj_trackingid()!=null)
		{
			json.put(OBJCLASS_TRACKING_ID, getObj_trackingid());
		}
		
		return json;
	}

	////////////////////////////////////////////////
	public String toString()
	{
		return toJson().toString();
	}
}