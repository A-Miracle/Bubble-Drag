package a.miracle.bubbledrag.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

/**
 * Utils
 * Created by A Miracle on 2016/3/24.
 */
public class Utils {

    private static final float DENSITY = Resources.getSystem().getDisplayMetrics().densityDpi / 160F;
    private static final Canvas sCanvas = new Canvas();

    /**
     * 从视图创建位图
     * @param view
     * @return
     */
    public static Bitmap createBitmapFromView(View view) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }
        view.clearFocus();
        Bitmap bitmap = createBitmapSafely(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888, 1);
        if (bitmap != null) {
            synchronized (sCanvas) {
                Canvas canvas = sCanvas;
                canvas.setBitmap(bitmap);
                view.draw(canvas);
                canvas.setBitmap(null);
            }
        }
        return bitmap;
    }

    /**
     * 一个安全的Bitmap.createBitmap(width, height, config)
     * @param width
     * @param height
     * @param config
     * @param retryCount 重试次数
     * @return
     */
    public static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            Log.e("Tools :", "OutOfMemoryError :", e);
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }

    /**
     * 获取状态栏高度＋标题栏高度
     * @param activity
     * @return
     */
    public static int getTopBarHeight(Activity activity) {
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }

    /**
     * 判断给定字符串是否空白串 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     */
    public static boolean isEmpty(CharSequence input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * !isEmpty(CharSequence input)
     */
    public static boolean isNotEmpty(CharSequence input) {
        return !isEmpty(input);
    }

    public static int dp2px(float dpValue) {
        return Math.round(dpValue * DENSITY);
    }

    public static int px2dp(float pxValue) {
        return Math.round(pxValue / DENSITY);
    }
}
