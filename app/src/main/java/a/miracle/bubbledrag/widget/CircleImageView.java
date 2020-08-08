package a.miracle.bubbledrag.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * see https://github.com/A-Miracle/CustomView
 */
public class CircleImageView extends ImageView {

    private Paint mBitmapPaint;
    private float mRadius;

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
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setDither(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth != 0 || measuredHeight != 0) {
            int size = Math.min(measuredWidth, measuredHeight);
            mRadius = size / 2.0f;
            setMeasuredDimension(size, size);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!setupBitmapPaint()) {
            return;
        }
        canvas.drawRoundRect(getRectF(), mRadius, mRadius, mBitmapPaint);
        mBitmapPaint.setShader(null);
    }

    protected boolean setupBitmapPaint() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return false;
        }

        Bitmap bitmap = getBitmapFromDrawable(drawable);
        if (bitmap == null) {
            return false;
        }

        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapShader.setLocalMatrix(getImageMatrix());
        mBitmapPaint.setShader(bitmapShader);
        return true;
    }

    protected RectF getRectF() {
        return new RectF(getPaddingLeft(), getPaddingTop(), getRight() - getLeft() - getPaddingRight(),
                getBottom() - getTop() - getPaddingBottom());
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
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
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
