package hl.objml2.plugin;

import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

public class ObjDetDnnBasePlugin extends ObjDetBasePlugin{
		
	@Override
	protected boolean init()
	{
		NET_DNN = Dnn.readNet( getModelFileName());
		if(NET_DNN!=null)
		{
			if(super.init())
			{
				if(this.dnn_preferred_backend>-1)
					NET_DNN.setPreferableBackend(this.dnn_preferred_backend);

				if(this.dnn_preferred_target>-1)
					NET_DNN.setPreferableTarget(this.dnn_preferred_target);
				
				return true;
			}
		}
		return false;
	}
	
	public Net getDnnNet()
	{
		return this.NET_DNN;
	}

}