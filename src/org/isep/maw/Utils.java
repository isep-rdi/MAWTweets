package org.isep.maw;

import org.apache.storm.Constants;
import org.apache.storm.tuple.Tuple;

public class Utils {
	
	static public boolean isTickTuple(Tuple tuple) {
		 return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
		            && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
		    }

	public static int max(Integer[] window) {
		Integer r = window[0];
		
		for(int i=1; i< window.length; i++)
			r = r > window[i] ? r : window[i];
		
		return r;
	}
}
