package com.xzw.slidingable;

/**
 * @author xzwszl
 * 2015-03-30
 */
public interface ISliding {
	
	//push the view up
	final int ONLY_SLIDING_FROM_START = 1;
	
	//push the view down
	final int ONLY_SLIDING_FROM_END = 2;
	
	final int BOTH_SLIDING = 3;
	
	final int DISABLE_SLIDING = 0;
	//judge current sliding mode
	public int getMode();
	
}
