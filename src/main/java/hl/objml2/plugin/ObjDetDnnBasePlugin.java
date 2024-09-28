package hl.objml2.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

public class ObjDetDnnBasePlugin extends ObjDetBasePlugin{
	
	//
	protected Net NET_DNN 						= null;
	/////////
	
	private boolean isRegObjsOfInterest = false;
	protected List<String> obj_classes_of_interest 		= new ArrayList<String>();
	//
	protected Map<String, String> mapObjClassMapping 	= new HashMap<String, String>();
	
	/////
	public void addObjClassMapping(String aOrgObjClassName, String aNewObjClassName)
	{
		this.mapObjClassMapping.put(aOrgObjClassName, aNewObjClassName);
	}
	
	public void clearObjClassesMapping()
	{
		this.mapObjClassMapping.clear();
	}
	
	public String getMappedObjClass(String aOrgObjClassName)
	{
		String sMappedClassName = this.mapObjClassMapping.get(aOrgObjClassName);
		if(sMappedClassName==null)
			sMappedClassName = aOrgObjClassName;
		return sMappedClassName;
	}
	/////

	public String[] getObjClassesOfInterest()
	{
		return (String[]) this.obj_classes_of_interest.toArray();
	}
	
	public void clearObjClassesOfInterest()
	{
		this.obj_classes_of_interest.clear();
		isRegObjsOfInterest = false;
	}
	
	public boolean addObjClassOfInterest(String[] aObjClassNames)
	{
		if(aObjClassNames!=null && aObjClassNames.length>0)
		{
			isRegObjsOfInterest = true;
			for(int i=0; i<aObjClassNames.length; i++)
			{
				this.obj_classes_of_interest.add(aObjClassNames[i].toLowerCase());
			}
			return isRegObjsOfInterest;
		}
		
		return false;
	}
	
	public boolean isObjClassOfInterest(String aObjClassName)
	{
		if(!isRegObjsOfInterest)
			return true;
		
		return this.obj_classes_of_interest.contains(aObjClassName.toLowerCase());
	}
	
	protected boolean init()
	{
		NET_DNN = Dnn.readNet( getModelFileName());
		
		if(NET_DNN!=null)
		{
			super.init();
		}
		
		return (NET_DNN!=null);
	}
	
	protected Net getDnn()
	{
		return this.NET_DNN;
	}

}