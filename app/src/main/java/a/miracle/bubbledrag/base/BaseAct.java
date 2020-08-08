package a.miracle.bubbledrag.base;

import android.app.Activity;
import android.os.Bundle;

import a.miracle.bubbledrag.AppManager;

/**
 * Created by A Miracle on 2016/3/24.
 */
public class BaseAct extends Activity {

    @Override
    public void finish() {
        AppManager.getInstance().removeActivity(this);
        super.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
    }
}
