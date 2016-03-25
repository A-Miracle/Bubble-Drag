package com.ctao.bubbledrag;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Stack;
/**
 * activity 堆栈式管理
 * Created by A Miracle on 2016/3/16.
 */
public class AppManager {
    /** 堆栈列表 */
    private static Stack<Activity> mActivitys = new Stack<>();// LinkedList
    private static AppManager mInstance;

    private AppManager() {
    }

    /** 单一实例 */
    public static AppManager getInstance() {
        if (mInstance == null) {
            synchronized (AppManager.class) {
                if (mInstance == null) {
                    mInstance = new AppManager();
                }
            }
        }
        return mInstance;
    }

    /** 添加Activity到堆栈 */
    public void addActivity(Activity activity) {
        mActivitys.add(activity);
    }

    /** 添加Activity到堆栈 */
    public void removeActivity(Activity activity) {
        mActivitys.remove(activity);
    }

    /** 获取当前Activity（堆栈中最后一个压入的） */
    public Activity getCurrentActivity() {
        return mActivitys.lastElement();
    }

    /** 除了此Activity之外的所有Activity全部关闭 */
    public void finishExcept(Activity except) {
        ArrayList<Activity> copy;
        synchronized (mActivitys) {
            copy = new ArrayList<>(mActivitys);
        }
        for (Activity activity : copy) {
            if (activity != except) {
                activity.finish();
            }
        }
    }

    /** 除了此Activity之外的所有Activity全部关闭 */
    public void finishExcept(Class<? extends Activity> exceptClass) {
        ArrayList<Activity> arrayList;
        synchronized (mActivitys) {
            arrayList = new ArrayList<>(mActivitys);
        }
        for (Activity activity : arrayList) {
            if (!activity.getClass().equals(exceptClass)) {
                activity.finish();
            }
        }
    }

    /** 关闭所有Activity */
    public void finishAll() {
        ArrayList<Activity> copy;
        synchronized (mActivitys) {
            copy = new ArrayList<>(mActivitys);
        }
        for (Activity activity : copy) {
            activity.finish();
        }
    }

    /**  关闭从栈内某个Activity开始到某个Activity结束[start, end); */
    public void finishStartToEnd(Class<? extends Activity> startClass, Class<? extends Activity> endClass) {
        boolean isClose = false;
        ArrayList<Activity> arrayList;
        synchronized (mActivitys) {
            arrayList = new ArrayList<>(mActivitys);
        }
        for (Activity activity : arrayList) {
            if (activity.getClass() == startClass) {
                isClose = true;
            }
            if (activity.getClass() == endClass) {
                isClose = false;
            }
            if (isClose) {
                activity.finish();
            }
        }
    }

    /** 退出应用程序 */
    public void exitApp() {
        try {
            finishAll();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
        }
    }
}
