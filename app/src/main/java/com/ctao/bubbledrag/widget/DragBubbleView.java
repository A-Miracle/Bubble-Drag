package com.ctao.bubbledrag.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;

import com.ctao.bubbledrag.AppManager;
import com.ctao.bubbledrag.R;
import com.ctao.bubbledrag.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 可拖拽气泡
 * Created by A Miracle on 2016/3/24.
 */
public class DragBubbleView extends View{

    public static final int BUBBLE = 0x12345678;

    /**Path*/				        private Path mPath;
    /**Paint*/				        private Paint mPaint;
    /**固定圆缩放比例*/			    private float mRatio = 1;
    /**定点圆缩放比例最小值*/		    private float mRatioMin = 0.2f;
    /**是否需要执行onDraw方法*/	        private boolean isNeedDraw;
    /**固定圆的圆心x, y坐标*/			private float mCircleX, mCircleY;
    /**移动圆形半径*/			        private int mMoveRadius;
    /**移动圆和固定圆圆心之间的距离*/ 	private float mCurDistance;
    /**固定圆和移动圆的圆心之间距离的限值*/private float mMaxDistance;
    /**移动圆的圆心x, y坐标*/			private float mMoveX, mMoveY;
    /**动画播放时锁定x, y坐标*/        private float mLockX, mLockY;
    /**动画开始*/				        private boolean isAnimStart;
    /**动画帧的个数*/			        private int mAnimNumber = 5;
    /**动画播放的当前帧*/			    private int mCurAnimNumber;
    /**动画帧的宽度*/			        private int mAnimWidth;
    /**动画帧的高度*/			        private int mAnimHeight;
    /**爆炸动画*/				        private Bitmap[] mExplosionAnim;
    /**移动图片*/				        private Bitmap mBitmap;
    /**原始View*/				    private View mOriginalView;
    /**气泡爆炸完成结束回调*/		    private OnFinishListener mFinishListener;
    /**气泡爆炸完成结束回调集合*/		private HashMap<String, OnFinishListener> mListenerMaps;

    // 是否响应按键事件，如果一个气泡已经在响应，其它气泡就不响应，同一界面始终最多只有一个气泡响应按键
    private boolean isTouchable = true;

    /**集合动画列表*/                 private List<View> mViews;
    /**集合动画开始*/                 private boolean isListAnimStart;
    /**集合气泡爆炸完成结束回调*/	    private OnListFinishListener mListFinishListener;

    /**挂载到Activity上*/
    public static DragBubbleView attach2Window(Activity activity){
        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        DragBubbleView bubbleView = new DragBubbleView(activity.getApplication());
        rootView.addView(bubbleView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return bubbleView;
    }

    /**事件交接, 交接v的事件*/
    public boolean handoverTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return down(v);
            case MotionEvent.ACTION_MOVE:
                return move(event, v);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return up(v);
        }
        return false;
    }

    /**气泡爆炸完成结束回调*/
    public void setOnFinishListener(OnFinishListener listener) {
        mFinishListener = listener;
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

    public void setColor(int color){
        if(mPaint != null){
            mPaint.setColor(color);
        }
    }

    public void setMaxDistance(float maxDistance) {
        mMaxDistance = maxDistance;
    }

    /**
     * 全部忽略
     * @param views 需要执行爆炸动画的MessageView
     * @param listener
     */
    public void allIgnore(List<View> views, OnListFinishListener listener){
        if(views != null && views.size() > 0){
            mViews = views;
            mListFinishListener = listener;
            postInvalidate();
        }
    }

    /**
     * 全部忽略
     * @param viewGroup 对应列表ViewGroup, 必须设置 MessageView.setTag(BUBBLE, BUBBLE);
     * @param listener
     */
    public void allIgnore(ViewGroup viewGroup, OnListFinishListener listener){
        if(viewGroup != null){
            mViews = new ArrayList<>();
            findBubble(viewGroup, mViews);
            if(mViews.size() > 0){
                mListFinishListener = listener;
                postInvalidate();
            }else{
                mViews = null;
            }
        }
    }

    public DragBubbleView(Context context) {
        this(context, null);
    }

    public DragBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mMaxDistance = Utils.converDip2px(86);
        setVisibility(View.GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(isNeedDraw){
            if (mMoveX != 0 && mMoveY != 0) {
                if (mRatio >= mRatioMin) {
                    //画连线
                    drawLinePath(canvas);
                    // 画固定圆
                    canvas.drawCircle(mCircleX, mCircleY, mMoveRadius * mRatio, mPaint);
                }

                // 画移动圆(图片)
                if(mBitmap == null){
                    canvas.drawCircle(mMoveX, mMoveY, mMoveRadius, mPaint);
                }else{
                    canvas.drawBitmap(mBitmap, mMoveX - mBitmap.getWidth() / 2, mMoveY - mBitmap.getHeight() / 2, null);
                }
            }
        }

        if(isAnimStart){
            if (mCurAnimNumber < mAnimNumber) { // 动画进行中
                if(mLockX == 0 && mLockY == 0){
                    mLockX = mMoveX;
                    mLockY = mMoveY;
                }
                canvas.drawBitmap(mExplosionAnim[mCurAnimNumber], mLockX - mAnimWidth / 2, mLockY - mAnimHeight / 2, null);
                mCurAnimNumber++;
                if (mCurAnimNumber == 1) { // 第一帧立即执行
                    invalidate();
                } else { // 其余帧每隔固定时间执行
                    postInvalidateDelayed(160);
                }
            } else { // 动画结束
                isAnimStart = false;
                mCurAnimNumber = 0;
                recycleBitmap();
                responseListener();
                mLockX = mLockY = 0;
                reset();

                if(mViews != null && mViews.size() > 0){
                    isListAnimStart = true;
                    postInvalidateDelayed(160);
                }
            }
        }

        if(isListAnimStart){
            startListAnim(canvas);
        }
    }

    private void startListAnim(Canvas canvas) {
        if(mViews != null && mViews.size() > 0){
            if(mExplosionAnim == null){
                initAnim();
                setVisibility(View.VISIBLE);
            }

            if(mLockX == 0 && mLockY == 0){ //锁定一个View, 执行动画
                View view = mViews.get(0);
                int width = view.getWidth();
                int height = view.getHeight();
                int[] position = new int[2];
                view.getLocationOnScreen(position);
                mLockX = position[0] + width/2;
                mLockY = position[1] - Utils.getTopBarHeight(AppManager.getInstance().getCurrentActivity()) + height/2;
                view.setVisibility(View.GONE);
            }

            if (mCurAnimNumber < mAnimNumber) { // 动画进行中
                canvas.drawBitmap(mExplosionAnim[mCurAnimNumber], mLockX - mAnimWidth / 2, mLockY - mAnimHeight / 2, null);
                mCurAnimNumber++;
                if (mCurAnimNumber == 1) { // 第一帧立即执行
                    invalidate();
                } else { // 其余帧每隔固定时间执行
                    postInvalidateDelayed(160);
                }
            } else { // 动画结束
                mCurAnimNumber = 0;
                mLockX = mLockY = 0;
                mViews.remove(0);
                startListAnim(canvas); //轮询下一个View
            }
        }else{
            isListAnimStart = false;
            mViews = null;
            recycleBitmap();
            setVisibility(View.GONE);
            if(mListFinishListener != null){
                mListFinishListener.onFinish();
            }
        }
    }

    /**响应事件*/
    private void responseListener() {
        if(mOriginalView != null){
            mOriginalView.getParent().requestDisallowInterceptTouchEvent(false); // 将控制权还给父控件

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
        }
    }

    /** 回收Bitmap资源 */
    private void recycleBitmap() {
        if (mExplosionAnim != null && mExplosionAnim.length != 0) {
            for (int i = 0; i < mExplosionAnim.length; i++) {
                if (mExplosionAnim[i] != null && !mExplosionAnim[i].isRecycled()) {
                    mExplosionAnim[i].recycle();
                    mExplosionAnim[i] = null;
                }
            }
            mExplosionAnim = null;
            if(mBitmap != null){
                mBitmap.recycle();
                mBitmap = null;
            }
        }
    }

    /** 画固定圆和移动圆之间的连线 */
    private void drawLinePath(Canvas canvas) {
        if(mCurDistance == 0){
            return;
        }

        mPath.reset();

        float sina = (mMoveY - mCircleY) / mCurDistance; // 移动圆圆心和固定圆圆心之间的连线与X轴相交形成的角度的sin值
        float cosa = (mCircleX - mMoveX) / mCurDistance; // 移动圆圆心和固定圆圆心之间的连线与X轴相交形成的角度的cos值

        mPath.moveTo(mCircleX - sina * mMoveRadius * mRatio, mCircleY - cosa * mMoveRadius * mRatio); // A点坐标
        mPath.lineTo(mCircleX + sina * mMoveRadius * mRatio, mCircleY + cosa * mMoveRadius * mRatio); // AB连线
        mPath.quadTo((mCircleX + mMoveX) / 2, (mCircleY + mMoveY) / 2, mMoveX + sina * mMoveRadius, mMoveY + cosa * mMoveRadius); // 控制点为两个圆心的中间点，二阶贝塞尔曲线，BC连线
        mPath.lineTo(mMoveX - sina * mMoveRadius, mMoveY - cosa * mMoveRadius); // CD连线
        mPath.quadTo((mCircleX + mMoveX) / 2, (mCircleY + mMoveY) / 2, mCircleX - sina * mMoveRadius * mRatio, mCircleY - cosa * mMoveRadius * mRatio); // 控制点也是两个圆心的中间点，二阶贝塞尔曲线，DA连线

        canvas.drawPath(mPath, mPaint);
    }

    public boolean down(View originalView) {
        if (originalView == null || mOriginalView != null) {
            return true;
        }
        if(originalView.getVisibility() == View.GONE || originalView.getVisibility() == View.INVISIBLE){
            return true;
        }
        if (!isTouchable) {
            return true;
        }

        isTouchable = false;

        //---
        isNeedDraw = true; // 由于BounceCircle是公用的，每次进来时都要确保needDraw的值为true
        originalView.getParent().requestDisallowInterceptTouchEvent(true); // 不允许父控件处理TouchEvent，当父控件为ListView这种本身可滑动的控件时必须要控制

        Bitmap bitmap = Utils.createBitmapFromView(originalView); //将View视图画到转为Bitmap
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

    public boolean move(MotionEvent event, View originalView) {
        if(mOriginalView != originalView){
            return true;
        }
        mMoveX = event.getRawX();
        mMoveY = event.getRawY() - Utils.getTopBarHeight(AppManager.getInstance().getCurrentActivity());

        float dx = mMoveX - mCircleX;
        float dy = mMoveY - mCircleY;
        mCurDistance = (float) Math.sqrt(dx * dx + dy * dy);
        mRatio = (mMaxDistance - mCurDistance) / mMaxDistance;
        invalidate();
        return true;
    }

    public boolean up(View originalView) {
        if(mOriginalView != originalView){
            return true;
        }
        if (mRatio > mRatioMin) { // 没有超出最大移动距离，手抬起时需要让移动圆回到固定圆的位置
            shakeAnimation(mMoveX, mMoveY);
            mMoveX = mCircleX;
            mMoveY = mCircleY;

        } else { // 超出最大移动距离
            isNeedDraw = false;
            isAnimStart = true;
            initAnim();
        }
        invalidate();
        return true;
    }

    /** 初始化爆炸动画 */
    private void initAnim() {
        if (mExplosionAnim == null) {
            mExplosionAnim = new Bitmap[mAnimNumber];
            mExplosionAnim[0] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_one);
            mExplosionAnim[1] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_two);
            mExplosionAnim[2] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_three);
            mExplosionAnim[3] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_four);
            mExplosionAnim[4] = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion_five);

            // 动画每帧的长宽都是一样的，取一个即可
            mAnimWidth = mExplosionAnim[0].getWidth();
            mAnimHeight = mExplosionAnim[0].getHeight();
        }
    }

    // CycleTimes动画重复的次数
    private void shakeAnimation(float upX, float upY) {
        float x, y;
        x = 0.3f * (upX - mCircleX) * mCurDistance / mMaxDistance;
        y = 0.3f * (upY - mCircleY) * mCurDistance / mMaxDistance;

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
                    mOriginalView.getParent().requestDisallowInterceptTouchEvent(false); // 将控制权还给父控件
                    mOriginalView.setVisibility(View.VISIBLE);
                }
                reset();
            }
        });
        set.setDuration(200);
        set.playTogether(animx, animy);
        set.start();
    }

    private void reset() {
        setVisibility(View.GONE);
        isTouchable = true;
        mMoveX = mCircleX = 0;
        mMoveY = mCircleY = 0;
        mRatio = 1;
        mOriginalView = null;
    }

    private void findBubble(ViewGroup viewGroup, List<View> views){
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if(childAt.getTag(BUBBLE) != null
                    && childAt.getVisibility() == View.VISIBLE){
                views.add(childAt);
            }
            if(childAt instanceof ViewGroup) {
                findBubble((ViewGroup) childAt, views);// 轮询
            }
        }
    }

    public interface OnFinishListener {
        void onFinish(String tag, View view);
    }

    public interface OnListFinishListener {
        void onFinish();
    }
}
