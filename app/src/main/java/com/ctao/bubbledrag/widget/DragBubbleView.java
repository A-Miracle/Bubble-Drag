package com.ctao.bubbledrag.widget;



import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.CycleInterpolator;

import com.ctao.bubbledrag.AppManager;
import com.ctao.bubbledrag.R;
import com.ctao.bubbledrag.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 可拖拽气泡
 * Created by A Miracle on 2016/3/24.
 */
public class DragBubbleView extends View{

    /**Path*/				        private Path path = new Path();
    /**Paint*/				        private Paint mPaint;
    /**固定圆缩放比例*/			    private float ratio = 1;
    /**定点圆缩放比例最小值*/		    private float ratioMin = 0.2f;
    /**是否需要执行onDraw方法*/	    private boolean needDraw;
    /**固定圆的圆心x坐标*/			    private float mCircleX;
    /**固定圆的圆心y坐标*/			    private float mCircleY;
    /**移动圆形半径*/			        private int mMoveRadius;
    /**移动圆和固定圆圆心之间的距离*/	private float curDistance;
    /**固定圆和移动圆的圆心之间距离的限值*/private float mMaxDistance;
    /**固定圆的圆心x坐标*/			    private float mMoveX;
    /**固定圆的圆心y坐标*/			    private float mMoveY;
    /**动画开始*/				        private boolean animStart;
    /**动画帧的个数*/			        private int animNumber = 5;
    /**动画播放的当前帧*/			    private int curAnimNumber;
    /**动画帧的宽度*/			        private int animWidth;
    /**动画帧的高度*/			        private int animHeight;
    /**爆炸动画*/				        private Bitmap[] explosionAnim;
    /**移动图片*/				        private Bitmap mBitmap;
    /**原始View*/				    private View mOriginalView;
    /**气泡爆炸完成结束回调*/		    private OnFinishListener mFinishListener;
    /**气泡爆炸完成结束回调集合*/		private HashMap<String, OnFinishListener> mListenerMaps;

    // 是否响应按键事件，如果一个气泡已经在响应，其它气泡就不响应，同一界面始终最多只有一个气泡响应按键
    private static boolean isTouchable = true;

    /**气泡爆炸完成结束回调*/
    public void setOnFinishListener(OnFinishListener finishListener) {
        mFinishListener = finishListener;
    }

    /**
     * Add 气泡爆炸完成结束回调
     * @param tag 唯一标识, @see handoverTouch(View v, MotionEvent event) 等等于 v.getTag()
     * @param finishListener
     * @deprecated
     */
    public void addOnFinishListener(String tag, OnFinishListener finishListener) {
        if(mListenerMaps == null){
            mListenerMaps = new HashMap<>();
        }
        if(Utils.isNotEmpty(tag) && finishListener != null){
            mListenerMaps.put(tag, finishListener);
        }
    }

    /**
     * remove 气泡爆炸完成结束回调
     * @param tag 唯一标识, @see handoverTouch(View v, MotionEvent event) 等等于 v.getTag()
     */
    public void removeOnFinishListener(String tag) {
        if(mListenerMaps != null && Utils.isNotEmpty(tag)){
            mListenerMaps.remove(tag);
        }
    }

    /**
     * clear 气泡爆炸完成结束回调, 不包括setOnFinishListener设置的
     */
    public void clearOnFinishListener() {
        if(mListenerMaps != null){
            mListenerMaps.clear();
            mListenerMaps = null;
        }
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public void setMaxDistance(float maxDistance) {
        mMaxDistance = maxDistance;
    }

    public DragBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DragBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragBubbleView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        // 设置绘制flag的paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mMaxDistance = Utils.converDip2px(getContext(), 86);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(needDraw){
            if (mMoveX != 0 && mMoveY != 0) {
                if (ratio >= ratioMin) {
                    // 画固定圆
                    canvas.drawCircle(mCircleX, mCircleY, mMoveRadius * ratio, mPaint);
                    //画连线
                    drawLinePath(canvas);
                }
                // 画移动圆(图片)
                canvas.drawCircle(mMoveX, mMoveY, mMoveRadius, mPaint);
                canvas.drawBitmap(mBitmap, mMoveX - mBitmap.getWidth() / 2, mMoveY - mBitmap.getHeight() / 2, null);
            }
        }

        if(animStart){
            if (curAnimNumber < animNumber) { // 动画进行中
                canvas.drawBitmap(explosionAnim[curAnimNumber], mMoveX - animWidth / 2, mMoveY - animHeight / 2, null);
                curAnimNumber++;
                if (curAnimNumber == 1) { // 第一帧立即执行
                    invalidate();
                } else { // 其余帧每隔固定时间执行
                    postInvalidateDelayed(200);
                }
            } else { // 动画结束
                animStart = false;
                curAnimNumber = 0;
                recycleBitmap();
                setVisibility(View.GONE); // 隐藏BounceCircle
                mMoveX = 0;
                mMoveY = 0;

                // 删除后的回调
                if (mFinishListener != null) {
                    mFinishListener.onFinish(null, mOriginalView);
                }

                if(mListenerMaps != null){
                    for (Map.Entry<String, OnFinishListener> entry : mListenerMaps.entrySet()) {
                        String key = entry.getKey();
                        Object tag = mOriginalView.getTag();
                        if(tag != null && key.equals(tag)){
                            entry.getValue().onFinish(key, mOriginalView);
                            break;
                        }
                    }
                }

                isTouchable = true;
            }
        }
    }

    /** 回收Bitmap资源 */
    private void recycleBitmap() {
        if (explosionAnim != null && explosionAnim.length != 0) {
            for (int i = 0; i < explosionAnim.length; i++) {
                if (explosionAnim[i] != null && !explosionAnim[i].isRecycled()) {
                    explosionAnim[i].recycle();
                    explosionAnim[i] = null;
                }
            }
            explosionAnim = null;
            if(mBitmap != null){
                mBitmap.recycle();
                mBitmap = null;
            }
        }
    }


    /** 画固定圆和移动圆之间的连线 */
    private void drawLinePath(Canvas canvas) {
        path.reset();

        float sina = (mMoveY - mCircleY) / curDistance; // 移动圆圆心和固定圆圆心之间的连线与X轴相交形成的角度的sin值
        float cosa = (mCircleX - mMoveX) / curDistance; // 移动圆圆心和固定圆圆心之间的连线与X轴相交形成的角度的cos值

        path.moveTo(mCircleX - sina * mMoveRadius * ratio, mCircleY - cosa * mMoveRadius * ratio); // A点坐标
        path.lineTo(mCircleX + sina * mMoveRadius * ratio, mCircleY + cosa * mMoveRadius * ratio); // AB连线
        path.quadTo((mCircleX + mMoveX) / 2, (mCircleY + mMoveY) / 2, mMoveX + sina * mMoveRadius, mMoveY + cosa * mMoveRadius); // 控制点为两个圆心的中间点，二阶贝塞尔曲线，BC连线
        path.lineTo(mMoveX - sina * mMoveRadius, mMoveY - cosa * mMoveRadius); // CD连线
        path.quadTo((mCircleX + mMoveX) / 2, (mCircleY + mMoveY) / 2, mCircleX - sina * mMoveRadius * ratio, mCircleY - cosa * mMoveRadius * ratio); // 控制点也是两个圆心的中间点，二阶贝塞尔曲线，DA连线

        canvas.drawPath(path, mPaint);
    }

    public boolean down(View originalView) {
        if(originalView.getVisibility() == View.GONE || originalView.getVisibility() == View.INVISIBLE){
            return false;
        }
        if (isTouchable) {
            isTouchable = false;

            //---
            needDraw = true; // 由于BounceCircle是公用的，每次进来时都要确保needDraw的值为true
            originalView.getParent().requestDisallowInterceptTouchEvent(true); // 不允许父控件处理TouchEvent，当父控件为ListView这种本身可滑动的控件时必须要控制

            Bitmap bitmap = createViewBitmap(originalView); //将View视图画到转为Bitmap
            int width = originalView.getWidth();
            int height = originalView.getHeight();
            int diameter = width < height ? width : height; //根据View大小确定移动圆形半径

            int[] position = new int[2];
            originalView.getLocationOnScreen(position);

            mOriginalView = originalView;
            mMoveRadius = diameter/2;
            mMoveX = mCircleX = position[0] + width/2;
            mMoveY = mCircleY = position[1] - Utils.getTopBarHeight(AppManager.getInstance().getCurrentActivity()) + height/2;
            mBitmap = bitmap;
            invalidate();

            setVisibility(View.VISIBLE);
            originalView.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    public boolean move(MotionEvent event) {
        mMoveX = event.getRawX();
        mMoveY = event.getRawY() - Utils.getTopBarHeight(AppManager.getInstance().getCurrentActivity());

        float dx = mMoveX - mCircleX;
        float dy = mMoveY - mCircleY;
        curDistance = (float) Math.sqrt(dx * dx + dy * dy);
        ratio = (mMaxDistance - curDistance) / mMaxDistance;
        invalidate();
        return true;
    }

    public boolean up() {
        if(mOriginalView != null){
            mOriginalView.getParent().requestDisallowInterceptTouchEvent(false); // 将控制权还给父控件
        }
        if (ratio > ratioMin) { // 没有超出最大移动距离，手抬起时需要让移动圆回到固定圆的位置
            shakeAnimation(mMoveX, mMoveY);
            mMoveX = mCircleX;
            mMoveY = mCircleY;

        } else { // 超出最大移动距离
            needDraw = false;
            animStart = true;
            initAnim();
        }
        invalidate();
        return true;
    }

    /** 初始化爆炸动画 */
    private void initAnim() {
        if (explosionAnim == null) {
            explosionAnim = new Bitmap[animNumber];
            explosionAnim[0] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_one);
            explosionAnim[1] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_two);
            explosionAnim[2] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_three);
            explosionAnim[3] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_four);
            explosionAnim[4] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_five);

            // 动画每帧的长宽都是一样的，取一个即可
            animWidth = explosionAnim[0].getWidth();
            animHeight = explosionAnim[0].getHeight();
        }
    }

    private Bitmap createViewBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // CycleTimes动画重复的次数
    private void shakeAnimation(float upX, float upY) {
        float x, y;
        x = 0.3f * (upX - mCircleX) * curDistance / mMaxDistance;
        y = 0.3f * (upY - mCircleY) * curDistance / mMaxDistance;

        ObjectAnimator animx = ObjectAnimator.ofFloat(this, "translationX", x);
        animx.setInterpolator(new CycleInterpolator(1));
        ObjectAnimator animy = ObjectAnimator.ofFloat(this, "translationY", y);
        animy.setInterpolator(new CycleInterpolator(1));
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束
                if (mOriginalView != null) {
                    mOriginalView.setVisibility(View.VISIBLE);
                }
                setVisibility(View.GONE);

                isTouchable = true;
                mMoveX = 0;
                mMoveY = 0;
                ratio = 1;
            }
        });
        set.setDuration(200);
        set.playTogether(animx, animy);
        set.start();
    }

    public interface OnFinishListener {
        void onFinish(String tag, View view);
    }

    /**事件交接, 交接v的事件*/
    public boolean handoverTouch(View v, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return down(v);
            case MotionEvent.ACTION_MOVE:
                return move(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return up();
        }
        return false;
    }
}