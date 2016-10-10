package com.ctao.bubbledrag.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class CircleImageView extends ImageView {

	protected static final String TAG = CircleImageView.class.getSimpleName();

	private Paint mBitmapPaint;
	private RectF mRect;
	
	public CircleImageView(Context context) {
		this(context, null);
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (getDrawable() == null) {
			return;
		}

		setupRoundePaint();
		RectF rect = getRectF();
		int rx = getMeasuredWidth()/2;
		int ry = getMeasuredHeight()/2;
		System.out.println("--"+rx+", "+ry);
		canvas.drawRoundRect(rect, rx, ry, mBitmapPaint);
	}
	
	
	private void setupRoundePaint() {
		if (getDrawable() == null) {
			return;
		}
		
		Bitmap bitmap = getBitmapFromDrawable(getDrawable());
		if(bitmap == null){
			return;
		}
		
		BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		if(getScaleType() != ScaleType.FIT_XY){
            Log.w(TAG,String.format("Now scale type just support fitXY,other type invalid"));
        }
		
        //now scale type just support fitXY
        //todo support all scale type
        Matrix mMatrix = new Matrix();
        mMatrix.setScale(getWidth() * 1.0f / bitmap.getWidth(), getHeight() * 1.0f / bitmap.getHeight());
        bitmapShader.setLocalMatrix(mMatrix);
        
		if(mBitmapPaint == null){
			mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mBitmapPaint.setDither(false);
		}
        
		mBitmapPaint.setShader(bitmapShader);
	}
	
	protected RectF getRectF(){
		if(mRect == null){
			mRect = new RectF(getPaddingLeft(), getPaddingTop(), getRight() - getLeft() - getPaddingRight(),
					getBottom() - getTop() - getPaddingBottom());
		}
		return mRect;
	}
	
	public static Bitmap getBitmapFromDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}
		try {
			Bitmap bitmap;
			if (drawable instanceof ColorDrawable) {
				bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
			} else {
				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			}
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
			return bitmap;
		} catch (OutOfMemoryError e) {
			return null;
		}
	}
}
