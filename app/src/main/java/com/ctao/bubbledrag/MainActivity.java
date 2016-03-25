package com.ctao.bubbledrag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ctao.bubbledrag.base.BaseActivity;
import com.ctao.bubbledrag.widget.DragBubbleView;

public class MainActivity extends BaseActivity {

    private DragBubbleView dragView;
    private BaseAdapter mAdpater;

    //模拟10条未读
    private boolean[] mData = {true, true, true, true, true, true, true, true, true, true};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dragView = (DragBubbleView) findViewById(R.id.dragView);
        dragView.setOnFinishListener(new DragBubbleView.OnFinishListener() {
            @Override
            public void onFinish(String tag, View view) {
                //方法一： 根据Tag响应事件
                //方法二：传递事件, 此处介绍方法二
                view.performClick(); //交递给Click事件
            }
        });

        View view = findViewById(R.id.unread_message);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return dragView.handoverTouch(v, event);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "全部忽略", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < mData.length; i++) {
                    mData[i] = false;
                }
                if(mAdpater != null){
                    mAdpater.notifyDataSetChanged();
                }
            }
        });

        ListView listview = (ListView) findViewById(R.id.listview);

        mAdpater = new BaseAdapter() {
            @Override
            public int getCount() {
                return mData.length;
            }

            @Override
            public Boolean getItem(int position) {
                return mData[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getApplication()).inflate(R.layout.item, parent, false);
                }
                final View view = convertView.findViewById(R.id.number);
                if(mData[position]){ //未读
                    view.setVisibility(View.VISIBLE);
                    view.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return dragView.handoverTouch(v, event);
                        }
                    });
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mData[position] = false;
                        }
                    });
                }else{
                    view.setVisibility(View.GONE);
                }
                return convertView;
            }
        };

        listview.setAdapter(mAdpater);
    }
}
