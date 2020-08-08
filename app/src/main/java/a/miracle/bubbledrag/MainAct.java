package a.miracle.bubbledrag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import a.miracle.bubbledrag.base.BaseAct;
import a.miracle.bubbledrag.widget.DragBubbleView;

import java.util.ArrayList;
import java.util.Random;

public class MainAct extends BaseAct implements OnTouchListener, OnClickListener {
    private static final int TAG_COLOR = R.id.color;
    private static final int TAG_DATA = R.id.data;
    private static final int TAG_POSITION = R.id.position;

    private DragBubbleView dragView;
    private View unread_message;
    private ListView listview;
    private MessageAdapter mAdapter;

    private int[] typeStyle = {R.drawable.red_dot_shape, R.drawable.red_dot_shape2, R.drawable.red_dot_shape3, R.drawable.red_dot_shape4};
    private int[] colorStyle = {R.color.red, R.color.red, R.color.blue_light, R.color.blue_light};

    private ArrayList<Message> mData = new ArrayList<>();

    {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            Message message = new Message();
            message.unreadNum = 1 + random.nextInt(199);
            message.type = random.nextInt(4);
            mData.add(message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dragView = DragBubbleView.attach2Window(this);

        listview = (ListView) findViewById(R.id.listview);
        unread_message = findViewById(R.id.unread_message);

        dragView.setOnFinishListener(new DragBubbleView.OnFinishListener() {
            @Override
            public void onFinish(String tag, View view) {
                //方法一： 根据Tag响应事件
                //方法二： 传递事件, 此处介绍方法二
                view.performClick(); //交递给Click事件
            }
        });

        mAdapter = new MessageAdapter();
        listview.setAdapter(mAdapter);

        unread_message.setTag(TAG_COLOR, R.color.red);
        unread_message.setTag(TAG_DATA, new Message(100, 1));
        unread_message.setOnTouchListener(this);
        unread_message.setOnClickListener(this);
    }

    private class MessageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplication()).inflate(R.layout.item, parent, false);
            }

            // 简易demo, ViewHolder略

            Message message = mData.get(position);
            final TextView number = (TextView) convertView.findViewById(R.id.number);
            if (message.unreadNum > 0) {
                number.setVisibility(View.VISIBLE);
                number.setText(message.unreadNum > 99 ? "99+" : message.unreadNum + "");
                number.setBackgroundResource(typeStyle[message.type]);
                number.setTag(TAG_COLOR, colorStyle[message.type]);
                number.setTag(TAG_DATA, message);
                number.setTag(TAG_POSITION, position);
                number.setTag(DragBubbleView.BUBBLE, DragBubbleView.BUBBLE);
                number.setOnTouchListener(MainAct.this);
                number.setOnClickListener(MainAct.this);
            } else {
                number.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    private class Message {
        int unreadNum; // 未读条数
        int type; // 消息类型(样式)

        public Message() {
            super();
        }

        public Message(int unreadNum, int type) {
            super();
            this.unreadNum = unreadNum;
            this.type = type;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int colorId = (int) v.getTag(TAG_COLOR);
        int color = getResources().getColor(colorId);
        dragView.setColor(color);
        return dragView.handoverTouch(v, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.unread_message:
                // 方法一: 自行找出所有需要执行爆炸动画的 MessageView
                //dragView.allIgnore(List<View> views, OnListFinishListener listener);

                // 方法二: 设置 MessageView.setTag(DragBubbleView.BUBBLE, DragBubbleView.BUBBLE);
                dragView.allIgnore(listview, new DragBubbleView.OnListFinishListener() {
                    @Override
                    public void onFinish() {
                        for (Message message : mData) {
                            message.unreadNum = 0;
                        }
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplication(), "全部忽略", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                Message message = (Message) v.getTag(TAG_DATA);
                int position = (int) v.getTag(TAG_POSITION);
                message.unreadNum = 0;
                mAdapter.notifyDataSetChanged();
                Toast.makeText(getApplication(), "item: " + position, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
