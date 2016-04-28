package com.hewz.plugins.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import android.widget.FrameLayout;
import android.widget.ImageView;


public class MaskView extends ImageView {  
    private static final String TAG = "MaskView";
    private Paint mLinePaint;  
    private Paint mAreaPaint;  
    private Rect mCenterRect = null;  
    private Context mContext;  
  
    public MaskView(Context context){
    	super(context);
    	FrameLayout.LayoutParams params= new FrameLayout.LayoutParams(
    			FrameLayout.LayoutParams.MATCH_PARENT, 
    			FrameLayout.LayoutParams.MATCH_PARENT);
    	this.setLayoutParams(params);
    	mContext = context;          
        widthScreen = DisplayUtil.getScreenMetrics(mContext).x;  
        heightScreen = DisplayUtil.getScreenMetrics(mContext).y;  
    }
  
    public MaskView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub  
        initPaint();  
        mContext = context;          
        widthScreen = DisplayUtil.getScreenMetrics(mContext).x;  
        heightScreen = DisplayUtil.getScreenMetrics(mContext).y;  
    }  
  
    private void initPaint(){  
        //draw transparent area outline in the center 
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);  
        mLinePaint.setColor(Color.BLUE);  
        mLinePaint.setStyle(Style.STROKE);  
        mLinePaint.setStrokeWidth(5f);  
        mLinePaint.setAlpha(30);  
  
        //draw the shadowy area
        mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  
        mAreaPaint.setColor(Color.GRAY);  
        mAreaPaint.setStyle(Style.FILL);  
        mAreaPaint.setAlpha(80);            
    }  
    public void setCenterRect(Rect r){  
        Log.d(TAG, "setCenterRect w:"+r.width()+" h:"+r.height());  
        this.mCenterRect = r;  
        postInvalidate();  
    }  
    public void clearCenterRect(Rect r){  
        this.mCenterRect = null;  
    }  
  
    int widthScreen, heightScreen;  
    @Override  
    protected void onDraw(Canvas canvas) {  
        // TODO Auto-generated method stub  
        Log.i(TAG, "onDraw...");  
        if(mCenterRect == null)  
            return;  
        //draw the shadowy area
        canvas.drawRect(0, 0, widthScreen, mCenterRect.top, mAreaPaint);  
        canvas.drawRect(0, mCenterRect.bottom + 1, widthScreen, heightScreen, mAreaPaint);  
        canvas.drawRect(0, mCenterRect.top, mCenterRect.left - 1, mCenterRect.bottom+1, mAreaPaint);  
        canvas.drawRect(mCenterRect.right + 1, mCenterRect.top, widthScreen, mCenterRect.bottom+1, mAreaPaint);  
  
        //draw the transparent area
        canvas.drawRect(mCenterRect, mLinePaint);  
        super.onDraw(canvas);  
    }  
  
}  
