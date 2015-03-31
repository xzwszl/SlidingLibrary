package com.xzw.slidingable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;

public abstract class SlidingBase<T extends View> extends LinearLayout implements ISliding{
	
	private final int SLIDING_FROM_START = 1;
	private final int SLIDING_FROM_END = 2;
	private final int SLIDING_UNSET = 0;
	protected final float FACTOR = 2.0f;
	
	protected final long mAnimationDuration = 250;
	
	private boolean mIsBeingDragged = false;
	private float mInitialMotionY;
	private float mInitialMotionX;
	private float mLastMotionY;
	private float mLastMotionX;
	private int mTouchSlop;
	protected T mSlidingRootView;
	protected int maxRange;
	private int mCurrentState = SLIDING_UNSET;
	
	private int mMode = ISliding.BOTH_SLIDING;
	
	
	public SlidingBase(Context context) {
		super(context);
		init(context);
	}
	

	public SlidingBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	
	private void init(Context context) {
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		
		if (null != mSlidingRootView) removeAllViews(); 
		mSlidingRootView = createView(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		addView(mSlidingRootView, params);
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		maxRange = dm.heightPixels / 2;
	}
	
	@Override
	public int getMode() {
		return mMode;
	}
	
	public void setMode(int mode) {
		mMode = mode;
	}
	
	
	public boolean isSliding() {
		return mCurrentState > SLIDING_UNSET;
	}
	public boolean canSlidingFromStart() {
		return mMode == ISliding.ONLY_SLIDING_FROM_START 
				|| mMode == ISliding.BOTH_SLIDING;
	}
	
	public boolean canSlidingFromEnd() {
		return mMode == ISliding.ONLY_SLIDING_FROM_END
				|| mMode == ISliding.BOTH_SLIDING;
	}

	public boolean canGetAction() {
		switch (mMode) {
		case ISliding.DISABLE_SLIDING:
			return false;
		case ISliding.ONLY_SLIDING_FROM_START:
			return isReadySlidingFromStart();
		case ISliding.ONLY_SLIDING_FROM_END:
			return isReadySlidingFromEnd();
		case ISliding.BOTH_SLIDING:
			return isReadySlidingFromStart()||isReadySlidingFromEnd();

		default:
			return false;
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		
		if (mMode == ISliding.DISABLE_SLIDING)  return false;
		
		int action = event.getAction();
		
		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			mIsBeingDragged = false;
			return false;
		}
		if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) return true;
		
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			
			if (canGetAction()) {
				mInitialMotionX = event.getX();
				mInitialMotionY = event.getY();
				mLastMotionX = mInitialMotionX;
				mLastMotionY = mInitialMotionY;
				
				mIsBeingDragged = false;
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			
			if (canGetAction()) {
				float x = event.getX();
				float y = event.getY();
				
				float deltaX = x - mLastMotionX;
				float deltaY = y - mLastMotionY;
				float absDiff = Math.abs(deltaY);
				
				if (absDiff > mTouchSlop && absDiff > Math.abs(deltaX)) {
					
					if (deltaY > 1.0f && canSlidingFromStart() && isReadySlidingFromStart()) {
						
						mLastMotionX = x;
						mLastMotionY = y;
						mIsBeingDragged = true;
						mCurrentState = SLIDING_FROM_START;
					} else if (deltaY < -1.0f && canSlidingFromEnd() && isReadySlidingFromEnd()) {
						
						mLastMotionX = x;
						mLastMotionY = y;
						mIsBeingDragged = true;
						mCurrentState = SLIDING_FROM_END;
					}
				}
			}
		}
		
		return mIsBeingDragged;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (mMode == ISliding.DISABLE_SLIDING)  return false;
		
		int action = event.getAction();
		
		if (action == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
			return false;
		}
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			
			if (canGetAction()) {
				mInitialMotionX = event.getX();
				mInitialMotionY = event.getY();
				mLastMotionX = mInitialMotionX;
				mLastMotionY = mInitialMotionX;
				
				return true;
			}
		case MotionEvent.ACTION_MOVE:
			if (mIsBeingDragged) {
				mLastMotionX = event.getX();
				mLastMotionY = event.getY();
				handleSliding();
			return true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			
			if (mIsBeingDragged) {
				releaseSliding(mCurrentState);
				mIsBeingDragged = false;
				return true;
			}
		}
		
		return false;
	}
	
	
	private void handleSliding() {
		
		int newValue = 0;
		if (mCurrentState == SLIDING_FROM_START) {
			newValue = Math.round(Math.min(0, (mInitialMotionY - mLastMotionY) / getFactor()));
		} else if (mCurrentState == SLIDING_FROM_END) {
			newValue = Math.round(Math.max(0, (mInitialMotionY - mLastMotionY) / getFactor()));
		}
		sliding(newValue);
	}
	
	/**
	 * judge the view Can sliding from top
	 * @return
	 */
	abstract boolean isReadySlidingFromStart();
	
	abstract boolean isReadySlidingFromEnd();
	
	abstract void releaseSliding(int state);
	
	abstract void sliding(int  newValue);
	
	abstract T createView(Context context);
	
	abstract public float getFactor();
	
}
