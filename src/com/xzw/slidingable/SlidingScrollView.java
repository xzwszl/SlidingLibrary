package com.xzw.slidingable;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ScrollView;

public class SlidingScrollView extends SlidingBase<ScrollView>{
	
	private Interpolator interpolator = new Interpolator() {
		
		@Override
		public float getInterpolation(float f) {
			return  Math.min(1.0f, 1.0f - (float)Math.pow(f,2));
		}
	};

	private SlidingRunnable mSlidingRunnable;
	
	private View mContentView;
	
	public SlidingScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SlidingScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	boolean isReadySlidingFromStart() {
		return mSlidingRootView.getScrollY() == 0;
	}

	@Override
	boolean isReadySlidingFromEnd() {
		
		View view = mSlidingRootView.getChildAt(0);
		
		if (null == view) return false;
		
		return mSlidingRootView.getScrollY() >= view.getHeight() - mSlidingRootView.getHeight();
	}

	@Override
	void releaseSliding(int state) {
		startRealse();
	}

	@Override
	void sliding(int newValue) {
		scrollTo(0, newValue);
	}

	@Override
	ScrollView createView(Context context) {
		ScrollView scrollView = new InternalScrollView(context);
		return scrollView;
	}
	
	public void setContentView(View view) {
		if (null == mContentView) {
			mSlidingRootView.removeAllViews();
		}
		
		mContentView = view;
//		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		mSlidingRootView.addView(view);
	}
	
	
	private class SlidingRunnable implements Runnable{
		
		long mStartTime;
		boolean isFinished;
		int mScrollY;
		
		public SlidingRunnable() {
		
		}

		@Override
		public void run() {
			
			if (!isFinished) {
				float r = ((float)(SystemClock.currentThreadTimeMillis() - mStartTime)) / mAnimationDuration;
				float ratio = interpolator.getInterpolation(r);
				
				if (ratio <= 1.0f && ratio > 0.0f) {
					scrollTo(0, (int)(ratio * mScrollY));
					post(this);
				}  else {
					scrollTo(0,0);
					isFinished = true;
				}
			}
		}
		
		public void abortAnimation(){
			removeCallbacks(this);
			isFinished = true;
		}
		
	}
	
	public void startRealse() {
		
		if (null == mSlidingRunnable) {
			mSlidingRunnable = new SlidingRunnable();
		} else {
			mSlidingRunnable.abortAnimation();
		}
		mSlidingRunnable.isFinished = false;
		mSlidingRunnable.mStartTime = SystemClock.currentThreadTimeMillis();
		mSlidingRunnable.mScrollY = getScrollY();
		post(mSlidingRunnable);
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private class InternalScrollView extends ScrollView {

		public InternalScrollView(Context context) {
			super(context);
		}
		
		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
				int scrollY, int scrollRangeX, int scrollRangeY,
				int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
			// TODO Auto-generated method stub
			return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
						scrollRangeY, maxOverScrollX, (int) (maxRange / (FACTOR + 1)), isTouchEvent);
		
			
//			 scrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
//						scrollRangeY, maxOverScrollX, maxRange, isTouchEvent);
//			return returnValue;
		}
		
//		public void scrollBy(int deltaX, int deltaY, int scrollX,
//				int scrollY, int scrollRangeX, int scrollRangeY,
//				int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
//			
//			if (!isTouchEvent && getMode() > 0) {
//				int newValue = scrollY + deltaY;
//				if (newValue < 0) {
//					scrollTo(0, newValue + getScrollY());
//				} else if (newValue > 0) {
//					scrollTo(0, newValue + getScrollY() - getRange());
//				}
//			}
//		}
	}
	
	/**
	 * @return
	 * get the scrollRange of inner scrollView
	 */
	public int getRange() {
		
		View view = mSlidingRootView.getChildAt(0);
		
		if (null != view) {
			
			return view.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop());
		}
		
		return 0;
	}

	@Override
	public float getFactor() {
		
		int scrollY = Math.abs(getScrollY());
		if (scrollY >= maxRange) return Float.MAX_VALUE;
		
		return (float)maxRange / (maxRange - scrollY) * FACTOR;
	}
	
	
}
