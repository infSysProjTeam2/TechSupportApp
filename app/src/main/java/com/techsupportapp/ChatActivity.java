package com.techsupportapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.MessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdEventHandler;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.SendBirdNotificationHandler;
import com.sendbird.android.handler.DeleteMessageHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Mention;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessageModel;
import com.sendbird.android.model.MessagingChannel;
import com.sendbird.android.model.ReadStatus;
import com.sendbird.android.model.SystemMessage;
import com.sendbird.android.model.TypeStatus;
import com.techsupportapp.utility.GlobalsMethods;
import com.techsupportapp.utility.LetterBitmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


public class ChatActivity extends AppCompatActivity {
    private static final int REQUEST_MEMBER_LIST = 100;
    private static final int MY_PERMISSION_REQUEST_STORAGE = 100;

    private SendBirdChatFragment mSendBirdMessagingFragment;
    private SendBirdMessagingAdapter mSendBirdMessagingAdapter;

    private CountDownTimer mTimer;
    private MessagingChannel mMessagingChannel;
    private Bundle mSendBirdInfo;

    private static Context cntxt;

    private boolean isUploading;
    private boolean isForeground;

    private ProgressDialog dialog;

    public static Bundle makeMessagingStartArgs(String appKey, String uuid, String nickname, String targetUserId) {
        return makeMessagingStartArgs(appKey, uuid, nickname, new String[]{targetUserId});
    }

    public static Bundle makeMessagingStartArgs(String appKey, String uuid, String nickname, String[] targetUserIds) {
        Bundle args = new Bundle();
        args.putBoolean("start", true);
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        args.putStringArray("targetUserIds", targetUserIds);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setTitle("Загрузка");
        cntxt = getBaseContext();

        initFragment();
        initSendBird(getIntent().getExtras());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Загрузка...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isForeground = true;
        SendBird.markAsRead();

        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new CountDownTimer(60 * 60 * 24 * 7 * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mSendBirdMessagingAdapter != null) {
                    if (mSendBirdMessagingAdapter.checkTypeStatus()) {
                        mSendBirdMessagingAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFinish() {
            }
        };
        mTimer.start();


        if(isUploading) {
            isUploading = false;
        } else {
            mSendBirdMessagingAdapter.clear();
            mSendBirdMessagingAdapter.notifyDataSetChanged();

            if (mSendBirdInfo.getBoolean("start")) {
                String[] targetUserIds = mSendBirdInfo.getStringArray("targetUserIds");
                SendBird.startMessaging(Arrays.asList(targetUserIds));
            } else if (mSendBirdInfo.getBoolean("join")) {
                String channelUrl = mSendBirdInfo.getString("channelUrl");
                SendBird.joinMessaging(channelUrl);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        isForeground = false;

        if (mTimer != null) {
            mTimer.cancel();
        }

        if(!isUploading) {
            SendBird.disconnect();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }


    private void initFragment() {
        mSendBirdMessagingFragment = new SendBirdChatFragment();

        mSendBirdMessagingAdapter = new SendBirdMessagingAdapter(this);
        mSendBirdMessagingFragment.setSendBirdMessagingAdapter(mSendBirdMessagingAdapter);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdMessagingFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEMBER_LIST) {
            if (resultCode == RESULT_OK && data != null) {
                try {
                    SendBird.inviteMessaging(SendBird.getCurrentChannel().getUrl(), Arrays.asList(data.getStringArrayExtra("userIds")));
                } catch (IOException e) {
                    // Not Connected.
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private static String getDisplayMemberNames(List<MessagingChannel.Member> members) {
        if (members.size() < 2) {
            return "No Members";
        } else if (members.size() == 2) {
            StringBuffer names = new StringBuffer();
            for (MessagingChannel.Member member : members) {
                if (member.getId().equals(SendBird.getUserId())) {
                    continue;
                }

                names.append(", " + member.getName());
            }
            return names.delete(0, 2).toString();
        } else {
            return "Group " + members.size();
        }
    }

    private void initSendBird(Bundle extras) {
        mSendBirdInfo = extras;

        String appKey = extras.getString("appKey");
        String uuid = extras.getString("uuid");
        String nickname = extras.getString("nickname");
        String gcmRegToken = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this).getString("SendBirdGCMToken", "");

        SendBird.init(this, appKey);
        SendBird.login(SendBird.LoginOption.build(uuid).setUserName(nickname).setGCMRegToken(gcmRegToken));
        SendBird.registerNotificationHandler(new SendBirdNotificationHandler() {
            @Override
            public void onMessagingChannelUpdated(MessagingChannel messagingChannel) {
                if (mMessagingChannel != null && mMessagingChannel.getId() == messagingChannel.getId()) {
                    updateMessagingChannel(messagingChannel);
                }
            }

            @Override
            public void onMentionUpdated(Mention mention) {

            }
        });

        SendBird.setEventHandler(new SendBirdEventHandler() {
            @Override
            public void onConnect(Channel channel) {
                SendBird.markAsRead(channel.getUrl());
            }

            @Override
            public void onError(int code) {
                Log.e("SendBird", "Error code: " + code);
            }

            @Override
            public void onChannelLeft(Channel channel) {
            }

            @Override
            public void onMessageReceived(Message message) {
                if (isForeground) {
                    SendBird.markAsRead();
                }
                mSendBirdMessagingAdapter.addMessageModel(message);
            }

            @Override
            public void onMutedMessageReceived(Message message) {

            }

            @Override
            public void onSystemMessageReceived(SystemMessage systemMessage) {
                switch (systemMessage.getCategory()) {
                    case SystemMessage.CATEGORY_TOO_MANY_MESSAGES:
                        systemMessage.setMessage("Too many messages. Please try later.");
                        break;
                    case SystemMessage.CATEGORY_MESSAGING_USER_BLOCKED:
                        systemMessage.setMessage("Blocked.");
                        break;
                    case SystemMessage.CATEGORY_MESSAGING_USER_DEACTIVATED:
                        systemMessage.setMessage("Deactivated.");
                        break;
                }

                mSendBirdMessagingAdapter.addMessageModel(systemMessage);
            }

            @Override
            public void onBroadcastMessageReceived(BroadcastMessage broadcastMessage) {
                mSendBirdMessagingAdapter.addMessageModel(broadcastMessage);
            }

            @Override
            public void onFileReceived(FileLink fileLink) {
                mSendBirdMessagingAdapter.addMessageModel(fileLink);
            }

            @Override
            public void onMutedFileReceived(FileLink fileLink) {

            }

            @Override
            public void onReadReceived(ReadStatus readStatus) {
                mSendBirdMessagingAdapter.setReadStatus(readStatus.getUserId(), readStatus.getTimestamp());
            }

            @Override
            public void onTypeStartReceived(TypeStatus typeStatus) {
                mSendBirdMessagingAdapter.setTypeStatus(typeStatus.getUserId(), System.currentTimeMillis());
            }

            @Override
            public void onTypeEndReceived(TypeStatus typeStatus) {
                mSendBirdMessagingAdapter.setTypeStatus(typeStatus.getUserId(), 0);
            }

            @Override
            public void onAllDataReceived(SendBird.SendBirdDataType type, int count) {
                mSendBirdMessagingAdapter.notifyDataSetChanged();
                mSendBirdMessagingFragment.mListView.setSelection(mSendBirdMessagingAdapter.getCount() - 1);
            }

            @Override
            public void onMessageDelivery(boolean sent, String message, String data, String tempId) {
                if (!sent) {
                    mSendBirdMessagingFragment.mEtxtMessage.setText(message);
                }
            }

            @Override
            public void onMessagingStarted(final MessagingChannel messagingChannel) {
                mSendBirdMessagingAdapter.clear();
                updateMessagingChannel(messagingChannel);

                SendBird.queryMessageList(messagingChannel.getUrl()).load(Long.MAX_VALUE, 30, 10, new MessageListQuery.MessageListQueryResult() {
                    @Override
                    public void onResult(List<MessageModel> messageModels) {
                        for (MessageModel model : messageModels) {
                            mSendBirdMessagingAdapter.addMessageModel(model);
                        }
                        mSendBirdMessagingAdapter.notifyDataSetChanged();
                        mSendBirdMessagingFragment.mListView.setSelection(30);

                        SendBird.join(messagingChannel.getUrl());
                        SendBird.connect(mSendBirdMessagingAdapter.getMaxMessageTimestamp());
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }

            @Override
            public void onMessagingUpdated(MessagingChannel messagingChannel) {
                updateMessagingChannel(messagingChannel);
            }

            @Override
            public void onMessagingEnded(MessagingChannel messagingChannel) {
            }

            @Override
            public void onAllMessagingEnded() {
            }

            @Override
            public void onMessagingHidden(MessagingChannel messagingChannel) {
            }

            @Override
            public void onAllMessagingHidden() {
            }

        });
    }

    private void updateMessagingChannel(MessagingChannel messagingChannel) {
        mMessagingChannel = messagingChannel;
        setTitle(getDisplayMemberNames(messagingChannel.getMembers()));

        Hashtable<String, Long> readStatus = new Hashtable<String, Long>();
        for (MessagingChannel.Member member : messagingChannel.getMembers()) {
            Long currentStatus = mSendBirdMessagingAdapter.mReadStatus.get(member.getId());
            if (currentStatus == null) {
                currentStatus = 0L;
            }
            readStatus.put(member.getId(), Math.max(currentStatus, messagingChannel.getLastReadMillis(member.getId())));
        }
        mSendBirdMessagingAdapter.resetReadStatus(readStatus);

        mSendBirdMessagingAdapter.setMembers(messagingChannel.getMembers());
        mSendBirdMessagingAdapter.notifyDataSetChanged();
        dialog.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static class SendBirdChatFragment extends Fragment {
        private ListView mListView;
        private SendBirdMessagingAdapter mAdapter;
        private EditText mEtxtMessage;
        private Button mBtnSend;

        public SendBirdChatFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_messaging, container, false);
            initUIComponents(rootView);
            return rootView;
        }


        private void initUIComponents(View rootView) {
            mListView = (ListView) rootView.findViewById(R.id.list);
            turnOffListViewDecoration(mListView);
            mListView.setAdapter(mAdapter);

            mBtnSend = (Button) rootView.findViewById(R.id.btn_send);
            mEtxtMessage = (EditText) rootView.findViewById(R.id.etxt_message);

            mBtnSend.setEnabled(false);
            mBtnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send();
                }
            });


            mEtxtMessage.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            send();
                        }
                        return true; // Do not hide keyboard.
                    }

                    return false;
                }
            });
            mEtxtMessage.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            mEtxtMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mBtnSend.setEnabled(s.length() > 0);

                    if (s.length() > 0) {
                        SendBird.typeStart();
                    } else {
                        SendBird.typeEnd();
                    }
                }
            });
            mListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //Helper.hideKeyboard(getActivity());
                    return false;
                }
            });
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                        if (view.getFirstVisiblePosition() == 0 && view.getChildCount() > 0 && view.getChildAt(0).getTop() == 0) {
                            SendBird.queryMessageList(SendBird.getChannelUrl()).prev(mAdapter.getMinMessageTimestamp(), 30, new MessageListQuery.MessageListQueryResult() {
                                @Override
                                public void onResult(List<MessageModel> messageModels) {
                                    if (messageModels.size() <= 0) {
                                        return;
                                    }

                                    for (MessageModel model : messageModels) {
                                        mAdapter.addMessageModel(model);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                    mListView.setSelection(messageModels.size());
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        } else if (view.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1 && view.getChildCount() > 0) {
                            SendBird.queryMessageList(SendBird.getChannelUrl()).next(mAdapter.getMaxMessageTimestamp(), 30, new MessageListQuery.MessageListQueryResult() {
                                @Override
                                public void onResult(List<MessageModel> messageModels) {
                                    if (messageModels.size() <= 0) {
                                        return;
                                    }

                                    for (MessageModel model : messageModels) {
                                        mAdapter.addMessageModel(model);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });
        }

        private void turnOffListViewDecoration(ListView listView) {
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setHorizontalFadingEdgeEnabled(false);
            listView.setVerticalFadingEdgeEnabled(false);
            listView.setHorizontalScrollBarEnabled(false);
            listView.setVerticalScrollBarEnabled(true);
            listView.setSelector(new ColorDrawable(0x00ffffff));
            listView.setCacheColorHint(0x00000000); // For Gingerbread scrolling bug fix
        }

        private void send() {
            SendBird.send(mEtxtMessage.getText().toString());
            mEtxtMessage.setText("");

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //Helper.hideKeyboard(getActivity());
            }
        }

        public void setSendBirdMessagingAdapter(SendBirdMessagingAdapter adapter) {
            mAdapter = adapter;
            if (mListView != null) {
                mListView.setAdapter(adapter);
            }
        }
    }

    public class SendBirdMessagingAdapter extends BaseAdapter {
        private static final int TYPE_UNSUPPORTED = 0;
        private static final int TYPE_MESSAGE = 1;
        private static final int TYPE_SYSTEM_MESSAGE = 2;
        private static final int TYPE_FILELINK = 3;
        private static final int TYPE_BROADCAST_MESSAGE = 4;
        private static final int TYPE_TYPING_INDICATOR = 5;

        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<Object> mItemList;

        private Hashtable<String, Long> mReadStatus;
        private Hashtable<String, Long> mTypeStatus;
        private List<MessagingChannel.Member> mMembers;
        private long mMaxMessageTimestamp = Long.MIN_VALUE;
        private long mMinMessageTimestamp = Long.MAX_VALUE;

        public SendBirdMessagingAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<Object>();
            mReadStatus = new Hashtable<String, Long>();
            mTypeStatus = new Hashtable<String, Long>();
        }

        @Override
        public int getCount() {
            return mItemList.size() + ((mTypeStatus.size() <= 0) ? 0 : 1);
        }

        @Override
        public Object getItem(int position) {
            if (position >= mItemList.size()) {
                ArrayList<String> names = new ArrayList<String>();
                for (MessagingChannel.Member member : mMembers) {
                    if (mTypeStatus.containsKey(member.getId())) {
                        names.add(member.getName());
                    }
                }

                return names;
            }
            return mItemList.get(position);
        }

        public void delete(Object object) {
            mItemList.remove(object);
        }

        public void clear() {
            mMaxMessageTimestamp = Long.MIN_VALUE;
            mMinMessageTimestamp = Long.MAX_VALUE;

            mReadStatus.clear();
            mTypeStatus.clear();
            mItemList.clear();
        }

        public void resetReadStatus(Hashtable<String, Long> readStatus) {
            mReadStatus = readStatus;
        }

        public void setReadStatus(String userId, long timestamp) {
            if (mReadStatus.get(userId) == null || mReadStatus.get(userId) < timestamp) {
                mReadStatus.put(userId, timestamp);
            }
        }

        public void setTypeStatus(String userId, long timestamp) {
            if (userId.equals(SendBird.getUserId())) {
                return;
            }

            if (timestamp <= 0) {
                mTypeStatus.remove(userId);
            } else {
                mTypeStatus.put(userId, timestamp);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addMessageModel(MessageModel messageModel) {
            if (messageModel.isPast()) {
                mItemList.add(0, messageModel);
            } else {
                mItemList.add(messageModel);
            }
            updateMessageTimestamp(messageModel);
        }

        private void updateMessageTimestamp(MessageModel model) {
            mMaxMessageTimestamp = mMaxMessageTimestamp < model.getTimestamp() ? model.getTimestamp() : mMaxMessageTimestamp;
            mMinMessageTimestamp = mMinMessageTimestamp > model.getTimestamp() ? model.getTimestamp() : mMinMessageTimestamp;
        }

        public long getMaxMessageTimestamp() {
            return mMaxMessageTimestamp == Long.MIN_VALUE ? Long.MAX_VALUE : mMaxMessageTimestamp;
        }

        public long getMinMessageTimestamp() {
            return mMinMessageTimestamp == Long.MAX_VALUE ? Long.MIN_VALUE : mMinMessageTimestamp;
        }

        public void setMembers(List<MessagingChannel.Member> members) {
            mMembers = members;
        }


        @Override
        public int getItemViewType(int position) {
            if (position >= mItemList.size()) {
                return TYPE_TYPING_INDICATOR;
            }

            Object item = mItemList.get(position);
            if (item instanceof Message) {
                return TYPE_MESSAGE;
            } else if (item instanceof FileLink) {
                return TYPE_FILELINK;
            } else if (item instanceof SystemMessage) {
                return TYPE_SYSTEM_MESSAGE;
            } else if (item instanceof BroadcastMessage) {
                return TYPE_BROADCAST_MESSAGE;
            }

            return TYPE_UNSUPPORTED;
        }

        @Override
        public int getViewTypeCount() {
            return 6;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            final Object item = getItem(position);

            if (convertView == null || ((ViewHolder) convertView.getTag()).getViewType() != getItemViewType(position)) {
                viewHolder = new ViewHolder();
                viewHolder.setViewType(getItemViewType(position));

                switch (getItemViewType(position)) {
                    case TYPE_UNSUPPORTED:
                        convertView = new View(mInflater.getContext());
                        convertView.setTag(viewHolder);
                        break;
                    case TYPE_MESSAGE: {
                        TextView tv;
                        ImageView iv;
                        View v;

                        convertView = mInflater.inflate(R.layout.view_messaging_message, parent, false);

                        v = convertView.findViewById(R.id.left_container);
                        viewHolder.setView("left_container", v);
                        iv = (ImageView) convertView.findViewById(R.id.img_left_thumbnail);
                        viewHolder.setView("left_thumbnail", iv);
                        tv = (TextView) convertView.findViewById(R.id.txt_left);
                        viewHolder.setView("left_message", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_left_name);
                        viewHolder.setView("left_name", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_left_time);
                        viewHolder.setView("left_time", tv);

                        v = convertView.findViewById(R.id.right_container);
                        viewHolder.setView("right_container", v);
                        iv = (ImageView) convertView.findViewById(R.id.img_right_thumbnail);
                        viewHolder.setView("right_thumbnail", iv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right);
                        viewHolder.setView("right_message", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_name);
                        viewHolder.setView("right_name", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_time);
                        viewHolder.setView("right_time", tv);
                        tv = (TextView) convertView.findViewById(R.id.txt_right_status);
                        viewHolder.setView("right_status", tv);

                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_SYSTEM_MESSAGE: {
                        convertView = mInflater.inflate(R.layout.view_system_message, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_BROADCAST_MESSAGE: {
                        convertView = mInflater.inflate(R.layout.view_system_message, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_TYPING_INDICATOR: {
                        convertView = mInflater.inflate(R.layout.view_typing_indicator, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                }
            }


            viewHolder = (ViewHolder) convertView.getTag();
            switch (getItemViewType(position)) {
                case TYPE_UNSUPPORTED:
                    break;
                case TYPE_MESSAGE:
                    Message message = (Message) item;
                    if (message.getSenderId().equals(SendBird.getUserId())) {
                        viewHolder.getView("left_container", View.class).setVisibility(View.GONE);
                        viewHolder.getView("right_container", View.class).setVisibility(View.VISIBLE);

                        displayUrlImage(viewHolder.getView("right_thumbnail", ImageView.class), message.getSenderName());
                        viewHolder.getView("right_name", TextView.class).setText(message.getSenderName());
                        viewHolder.getView("right_message", TextView.class).setText(message.getMessage());
                        viewHolder.getView("right_time", TextView.class).setText(getDisplayDateTime(mContext, message.getTimestamp()));

                        int readCount = 0;
                        for (String key : mReadStatus.keySet()) {
                            if (key.equals(message.getSenderId())) {
                                readCount += 1;
                                continue;
                            }

                            if (mReadStatus.get(key) >= message.getTimestamp()) {
                                readCount += 1;
                            }
                        }
                        if (readCount < mReadStatus.size()) {
                            if (mReadStatus.size() - readCount > 1) {
                                viewHolder.getView("right_status", TextView.class).setText("Непрочитано " + (mReadStatus.size() - readCount));
                            } else {
                                viewHolder.getView("right_status", TextView.class).setText("Непрочитано");
                            }
                        } else {
                            viewHolder.getView("right_status", TextView.class).setText("");
                        }

                        viewHolder.getView("right_container").setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                new AlertDialog.Builder(mContext)
                                        .setTitle("Удалить сообщение")
                                        .setMessage("Вы действительно хотите удалить сообщение?")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SendBird.deleteMessage(((Message) item).getMessageId(), new DeleteMessageHandler() {
                                                    @Override
                                                    public void onError(SendBirdException e) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onSuccess(long messageId) {
                                                        mSendBirdMessagingAdapter.delete(item);
                                                        mSendBirdMessagingAdapter.notifyDataSetChanged();
                                                        Toast.makeText(mContext, "Сообщение было удалено.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .create()
                                        .show();

                                return true;
                            }
                        });
                    } else {
                        viewHolder.getView("left_container", View.class).setVisibility(View.VISIBLE);
                        viewHolder.getView("right_container", View.class).setVisibility(View.GONE);

                        displayUrlImage(viewHolder.getView("left_thumbnail", ImageView.class), message.getSenderName());
                        viewHolder.getView("left_name", TextView.class).setText(message.getSenderName());
                        viewHolder.getView("left_message", TextView.class).setText(message.getMessage());
                        viewHolder.getView("left_time", TextView.class).setText(getDisplayDateTime(mContext, message.getTimestamp()));
                    }
                    break;
                case TYPE_SYSTEM_MESSAGE:
                    SystemMessage systemMessage = (SystemMessage) item;
                    viewHolder.getView("message", TextView.class).setText(Html.fromHtml(systemMessage.getMessage()));
                    break;
                case TYPE_BROADCAST_MESSAGE:
                    BroadcastMessage broadcastMessage = (BroadcastMessage) item;
                    viewHolder.getView("message", TextView.class).setText(Html.fromHtml(broadcastMessage.getMessage()));
                    break;

                case TYPE_TYPING_INDICATOR: {
                    int itemCount = ((List) item).size();
                    String typeMsg = ((List) item).get(0)
                            + ((itemCount > 1) ? " +" + (itemCount - 1) : "")
                            + ((itemCount > 1) ? "" : "")
                            + "пишет...";
                    viewHolder.getView("message", TextView.class).setText(typeMsg);
                    break;
                }
            }

            return convertView;
        }

        public boolean checkTypeStatus() {
            for (String key : mTypeStatus.keySet()) {
                Long ts = mTypeStatus.get(key);
                if (System.currentTimeMillis() - ts > 10 * 1000L) {
                    mTypeStatus.remove(key);
                    return true;
                }
            }

            return false;
        }


        private class ViewHolder {
            private Hashtable<String, View> holder = new Hashtable<String, View>();
            private int type;

            public int getViewType() {
                return this.type;
            }

            public void setViewType(int type) {
                this.type = type;
            }

            public void setView(String k, View v) {
                holder.put(k, v);
            }

            public View getView(String k) {
                return holder.get(k);
            }

            public <T> T getView(String k, Class<T> type) {
                return type.cast(getView(k));
            }
        }
    }

    private static String getDisplayDateTime(Context context, long milli) {
        Date date = new Date(milli);

        if (System.currentTimeMillis() - milli < 60 * 60 * 24 * 1000l) {
            return DateFormat.getTimeFormat(context).format(date);
        }

        return DateFormat.getDateFormat(context).format(date) + " " + DateFormat.getTimeFormat(context).format(date);
    }

    private static void displayUrlImage(ImageView imageView, String name) {
        int COVER_IMAGE_SIZE = 100;
        LetterBitmap letterBitmap = new LetterBitmap(cntxt);
        Bitmap letterTile = letterBitmap.getLetterTile(name.substring(0), name, COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
        imageView.setImageBitmap(GlobalsMethods.ImageMethods.getclip(letterTile));
    }
}
