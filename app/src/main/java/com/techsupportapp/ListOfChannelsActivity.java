package com.techsupportapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.MessagingChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdNotificationHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Mention;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessageModel;
import com.sendbird.android.model.MessagingChannel;
import com.techsupportapp.variables.GlobalsMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


public class ListOfChannelsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private SendBirdMessagingChannelListFragment mSendBirdMessagingChannelListFragment;

    private String mAppId;
    private String mUserId;
    private String mNickname;
    private boolean isAdmin;
    private String mGcmRegToken;

    private static Context cntxt;

    private static ProgressDialog dialog;

    public static Bundle makeSendBirdArgs(String appKey, String uuid, String nickname, boolean isAdmin) {
        Bundle args = new Bundle();
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        args.putBoolean("isAdmin", isAdmin);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_channels);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        cntxt = getBaseContext();

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");
        isAdmin = getIntent().getExtras().getBoolean("isAdmin");
        mGcmRegToken = PreferenceManager.getDefaultSharedPreferences(ListOfChannelsActivity.this).getString("SendBirdGCMToken", "");

        dialog = new ProgressDialog(ListOfChannelsActivity.this);
        dialog.setMessage("Загрузка...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        initializeComponents();
        initFragment();
        initSendBird();

        Toast.makeText(this, "Долгое удержание по каналу для выхода из него.", Toast.LENGTH_LONG).show();
    }

    private void initializeComponents(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mUserId);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView userImage = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.userImage);
        TextView userName = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userName);
        TextView userType = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userType);

        int COVER_IMAGE_SIZE = 150;
        LetterBitmap letterBitmap = new LetterBitmap(ListOfChannelsActivity.this);
        Bitmap letterTile = letterBitmap.getLetterTile(mNickname.substring(0), mNickname.substring(1), COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
        userImage.setImageBitmap(ChatActivity.getclip(letterTile));

        userName.setText(mNickname);
        if (isAdmin)
            userType.setText("Администратор");
        else
            userType.setText("Пользователь");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfTickets) {
            if (isAdmin) {
                Intent intent = new Intent(ListOfChannelsActivity.this, ListOfTicketsActivity.class);
                intent.putExtra("appKey", mAppId);
                intent.putExtra("uuid", mUserId);
                intent.putExtra("nickname", mNickname);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(ListOfChannelsActivity.this, CreateTicketActivity.class);
                intent.putExtra("appKey", mAppId);
                intent.putExtra("uuid", mUserId);
                intent.putExtra("nickname", mNickname);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
            }
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            GlobalsMethods.showAbout(ListOfChannelsActivity.this);
            return true;
        } else if (id == R.id.exit) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initSendBird() {
        SendBird.init(this, mAppId);
        SendBird.login(SendBird.LoginOption.build(mUserId).setUserName(mNickname).setGCMRegToken(mGcmRegToken));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        SendBird.join("");
//        SendBird.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        SendBird.disconnect();
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void initFragment() {
        mSendBirdMessagingChannelListFragment = new SendBirdMessagingChannelListFragment();
        mSendBirdMessagingChannelListFragment.setSendBirdMessagingChannelListHandler(new SendBirdMessagingChannelListFragment.SendBirdMessagingChannelListHandler() {
            @Override
            public void onMessagingChannelSelected(MessagingChannel messagingChannel) {
                Intent intent = new Intent(ListOfChannelsActivity.this, ChatActivity.class);
                Bundle args = ChatActivity.makeMessagingJoinArgs(mAppId, mUserId, mNickname, messagingChannel.getUrl());
                intent.putExtras(args);
                startActivity(intent);
            }

        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdMessagingChannelListFragment)
                .commit();
    }

    public static class SendBirdMessagingChannelListFragment extends Fragment {
        private SendBirdMessagingChannelListHandler mHandler;
        private ListView mListView;
        private SendBirdMessagingChannelAdapter mAdapter;
        private MessagingChannelListQuery mMessagingChannelListQuery;

        public static interface SendBirdMessagingChannelListHandler {
            public void onMessagingChannelSelected(MessagingChannel channel);
        }

        public void setSendBirdMessagingChannelListHandler(SendBirdMessagingChannelListHandler handler) {
            mHandler = handler;
        }

        public SendBirdMessagingChannelListFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_messaging_channel_list, container, false);
            initUIComponents(rootView);
            return rootView;
        }

        private void initUIComponents(View rootView) {
            mListView = (ListView)rootView.findViewById(R.id.list);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MessagingChannel channel = mAdapter.getItem(position);
                    if(mHandler != null) {
                        mHandler.onMessagingChannelSelected(channel);
                    }
                }
            });
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount >= (int) (totalItemCount * 0.8f)) {
                        loadNextChannels();
                    }
                }
            });
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    final MessagingChannel channel = mAdapter.getItem(position);
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Покинуть")
                            .setMessage("Вы хотите покинуть этот канал??")
                            .setPositiveButton("Покинуть", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mAdapter.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                    SendBird.endMessaging(channel.getUrl());
                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create().show();
                    return true;
                }
            });

            mAdapter = new SendBirdMessagingChannelAdapter(getActivity());
            mListView.setAdapter(mAdapter);
        }

        private void initChannelQuery() {
        }
        private void loadNextChannels() {
            if(mMessagingChannelListQuery == null || mMessagingChannelListQuery.isLoading()) {
                return;
            }

            if(mMessagingChannelListQuery.hasNext()) {
                mMessagingChannelListQuery.next(new MessagingChannelListQuery.MessagingChannelListQueryResult() {
                    @Override
                    public void onResult(List<MessagingChannel> messagingChannels) {
                        mAdapter.addAll(messagingChannels);
                    }

                    @Override
                    public void onError(int i) {
                    }
                });
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            SendBird.registerNotificationHandler(new SendBirdNotificationHandler() {
                @Override
                public void onMessagingChannelUpdated(MessagingChannel messagingChannel) {
                    mAdapter.replace(messagingChannel);
                }

                @Override
                public void onMentionUpdated(Mention mention) {

                }
            });


            if(mMessagingChannelListQuery == null) {
                mMessagingChannelListQuery = SendBird.queryMessagingChannelList();
                mMessagingChannelListQuery.setLimit(30);
            }

            mMessagingChannelListQuery.next(new MessagingChannelListQuery.MessagingChannelListQueryResult() {
                @Override
                public void onResult(List<MessagingChannel> list) {
                    mAdapter.clear();
                    mAdapter.addAll(list);
                    mAdapter.notifyDataSetChanged();

                    SendBird.join("");
                    SendBird.connect();
                }

                @Override
                public void onError(int i) {

                }
            });
        }

        @Override
        public void onPause() {
            super.onPause();
            if(mMessagingChannelListQuery != null) {
                mMessagingChannelListQuery.cancel();
                mMessagingChannelListQuery = null;
            }

            SendBird.disconnect();
        }
    }

    public static class SendBirdMessagingChannelAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<MessagingChannel> mItemList;

        public static List<MessagingChannel> sortMessagingChannels(List<MessagingChannel> messagingChannels) {
            Collections.sort(messagingChannels, new Comparator<MessagingChannel>() {
                @Override
                public int compare(MessagingChannel lhs, MessagingChannel rhs) {
                    long lhsv = lhs.getLastMessageTimestamp();
                    long rhsv = rhs.getLastMessageTimestamp();
                    return (lhsv == rhsv) ? 0 : (lhsv < rhsv) ? 1 : -1;
                }
            });

            return messagingChannels;
        }


        public SendBirdMessagingChannelAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<MessagingChannel>();
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public MessagingChannel getItem(int position) {
            return mItemList.get(position);
        }

        public void clear() {
            mItemList.clear();
        }

        public MessagingChannel remove(int index) {
            return mItemList.remove(index);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void add(MessagingChannel channel) {
            mItemList.add(channel);
            notifyDataSetChanged();
        }

        public void addAll(List<MessagingChannel> channels) {
            mItemList.addAll(channels);
            notifyDataSetChanged();
        }

        public void replace(MessagingChannel newChannel) {
            for(MessagingChannel oldChannel : mItemList) {
                if(oldChannel.getId() == newChannel.getId()) {
                    mItemList.remove(oldChannel);
                    break;
                }
            }

            mItemList.add(0, newChannel);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                viewHolder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.view_messaging_channel, parent, false);
                viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                viewHolder.setView("txt_topic", convertView.findViewById(R.id.txt_topic));
                viewHolder.setView("txt_member_count", convertView.findViewById(R.id.txt_member_count));
                viewHolder.setView("txt_unread_count", convertView.findViewById(R.id.txt_unread_count));
                viewHolder.setView("txt_date", convertView.findViewById(R.id.txt_date));
                viewHolder.setView("txt_desc", convertView.findViewById(R.id.txt_desc));

                convertView.setTag(viewHolder);
            }

            MessagingChannel item = getItem(position);
            viewHolder = (ViewHolder) convertView.getTag();
            displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), getDisplayCoverImageUrl(item.getMembers()));
            viewHolder.getView("txt_topic", TextView.class).setText(getDisplayMemberNames(item.getMembers()));

            if(item.getUnreadMessageCount() > 0) {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.VISIBLE);
                viewHolder.getView("txt_unread_count", TextView.class).setText("" + item.getUnreadMessageCount());
            } else {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.INVISIBLE);
            }

            if(item.isGroupMessagingChannel()) {
                viewHolder.getView("txt_member_count", TextView.class).setVisibility(View.VISIBLE);
                viewHolder.getView("txt_member_count", TextView.class).setText("" + item.getMemberCount());
            } else {
                viewHolder.getView("txt_member_count", TextView.class).setVisibility(View.GONE);
            }

            if(item.hasLastMessage()) {
                MessageModel message = item.getLastMessage();
                if(message instanceof Message) {
                    viewHolder.getView("txt_date", TextView.class).setText(getDisplayTimeOrDate(mContext, message.getTimestamp()));
                    viewHolder.getView("txt_desc", TextView.class).setText("" + ((Message)message).getMessage());
                } else if(message instanceof BroadcastMessage) {
                    viewHolder.getView("txt_date", TextView.class).setText(getDisplayTimeOrDate(mContext, message.getTimestamp()));
                    viewHolder.getView("txt_desc", TextView.class).setText("" + ((BroadcastMessage) message).getMessage());
                } else if(message instanceof FileLink) {
                    viewHolder.getView("txt_date", TextView.class).setText(getDisplayTimeOrDate(mContext, message.getTimestamp()));
                    viewHolder.getView("txt_desc", TextView.class).setText("(FILE)");
                } else {
                    viewHolder.getView("txt_date", TextView.class).setText("");
                    viewHolder.getView("txt_desc", TextView.class).setText("");
                }
            } else {
                viewHolder.getView("txt_date", TextView.class).setText("");
                viewHolder.getView("txt_desc", TextView.class).setText("");
            }
            dialog.dismiss();
            return convertView;
        }

        private static class ViewHolder {
            private Hashtable<String, View> holder = new Hashtable<String, View>();

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

    private static String getDisplayCoverImageUrl(List<MessagingChannel.Member> members) {
        for(MessagingChannel.Member member : members) {
            if(member.getId().equals(SendBird.getUserId())) {
                continue;
            }
            return member.getName();
        }

        return "";
    }
    private static String getDisplayMemberNames(List<MessagingChannel.Member> members) {
        if(members.size() < 2) {
            return "No Members";
        }

        StringBuffer names = new StringBuffer();
        for(MessagingChannel.Member member : members) {
            if(member.getId().equals(SendBird.getUserId())) {
                continue;
            }

            names.append(", " + member.getName());
        }
        return names.delete(0, 2).toString();
    }

    private static String getDisplayTimeOrDate(Context context, long milli) {
        Date date = new Date(milli);

        if(System.currentTimeMillis() - milli > 60 * 60 * 24 * 1000l) {
            return DateFormat.getDateFormat(context).format(date);
        } else {
            return DateFormat.getTimeFormat(context).format(date);
        }
    }

    private static void displayUrlImage(ImageView imageView, String name) {
        int COVER_IMAGE_SIZE = 100;
        LetterBitmap letterBitmap = new LetterBitmap(cntxt);
        Bitmap letterTile = letterBitmap.getLetterTile(name.substring(0), name.substring(1), COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
        imageView.setImageBitmap(letterTile);
    }
}