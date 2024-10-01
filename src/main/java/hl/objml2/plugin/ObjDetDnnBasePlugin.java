package hl.objml2.plugin;

import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

public class ObjDetDnnBasePlugin extends ObjDetBasePlugin{
	
	//
	protected Net NET_DNN 						= null;
	//
		
	protected boolean init()
	{
		NET_DNN = preDnnInit(NET_DNN);
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
	

	public Net preDnnInit(Net aDnnNet) 
	{
		return aDnnNet;
	}

}