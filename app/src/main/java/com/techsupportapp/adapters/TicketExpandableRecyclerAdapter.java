package com.techsupportapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseReference;
import com.innodroid.expandablerecycler.ExpandableRecyclerAdapter;
import com.techsupportapp.AcceptedTicketsActivity;
import com.techsupportapp.MessagingActivity;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.fragments.BottomSheetFragment;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TicketExpandableRecyclerAdapter extends ExpandableRecyclerAdapter<TicketExpandableRecyclerAdapter.TicketListItem> {
    private static final int TYPE_TICKET = 1001;

    public static final int TYPE_AVAILABLE = 1;
    public static final int TYPE_MYCLOSED = 2;
    public static final int TYPE_CLOSED = 3;
    public static final int TYPE_MYACCEPTED = 4;
    public static final int TYPE_MYCREATED = 5;
    public static final int TYPE_OVERVIEWCREATED = 6;

    private Context context;
    private int type;
    private final ArrayList<User> users;
    private FragmentManager fragmentManager;
    private DatabaseReference databaseReference;

    public TicketExpandableRecyclerAdapter(int type, Context context, DatabaseReference databaseReference, ArrayList<Ticket> values, ArrayList<User> users, FragmentManager fragmentManager) {
        super(context);

        this.context = context;
        this.type = type;
        this.users = users;
        this.fragmentManager = fragmentManager;
        this.databaseReference = databaseReference;

        setItems(getSampleItems(values));
    }

    @Override
    public void expandItems(int position, boolean notify) {
        super.expandItems(position, notify);

        if (type == TYPE_AVAILABLE)
            Globals.expandedItemsAvailable.add(position);
        else if (type == TYPE_MYCLOSED)
            Globals.expandedItemsMyClosed.add(position);
        else if (type == TYPE_CLOSED)
            Globals.expandedItemsClosed.add(position);
        else
            Globals.expandedItemsOverview.add(position);
    }

    @Override
    public void collapseItems(int position, boolean notify) {
        super.collapseItems(position, notify);

        if (type == TYPE_AVAILABLE)
            Globals.expandedItemsAvailable.remove(Globals.expandedItemsAvailable.indexOf(position));
        else if (type == TYPE_MYCLOSED)
            Globals.expandedItemsMyClosed.remove(Globals.expandedItemsMyClosed.indexOf(position));
        else if (type == TYPE_CLOSED)
            Globals.expandedItemsClosed.remove(Globals.expandedItemsClosed.indexOf(position));
        else
            Globals.expandedItemsOverview.remove(Globals.expandedItemsOverview.indexOf(position));
    }

    public static class TicketListItem extends ExpandableRecyclerAdapter.ListItem {
        public String text;
        public Ticket ticket;

        public TicketListItem(String group) {
            super(TYPE_HEADER);

            text = group;
        }

        public TicketListItem(Ticket ticket) {
            super(TYPE_TICKET);

            this.ticket = ticket;
        }
    }

    public class HeaderViewHolder extends ExpandableRecyclerAdapter.HeaderViewHolder {
        TextView name;

        public HeaderViewHolder(View view) {
            super(view, (ImageView) view.findViewById(R.id.item_arrow));

            name = (TextView) view.findViewById(R.id.item_header_name);
        }

        public void bind(int position) {
            super.bind(position);

            name.setText(visibleItems.get(position).text);
        }
    }

    public class TicketViewHolder extends ExpandableRecyclerAdapter.ViewHolder {
        TextView authorText;
        TextView dateText;
        TextView topicText;
        TextView descText;
        ImageView ticketImage;

        public TicketViewHolder(View view) {
            super(view);

            ticketImage = (ImageView) view.findViewById(R.id.ticketImage);
            authorText = (TextView) view.findViewById(R.id.ticketAuthor);
            dateText = (TextView) view.findViewById(R.id.ticketDate);
            topicText = (TextView) view.findViewById(R.id.ticketTopic);
            descText = (TextView) view.findViewById(R.id.ticketDesc);
        }

        public void bind(final int position) {
            final String titleText;

            final String userId = visibleItems.get(position).ticket.getUserId();
            final String adminId = visibleItems.get(position).ticket.getAdminId();

            if (Globals.currentUser.getRole() != User.SIMPLE_USER){
                authorText.setText(visibleItems.get(position).ticket.getUserName());
                titleText = visibleItems.get(position).ticket.getUserName();
            }
            else if (adminId == null || adminId.equals("")) {
                authorText.setText("Не установлено");
                titleText = authorText.getText().toString();
            } else {
                authorText.setText(visibleItems.get(position).ticket.getAdminName());
                titleText = visibleItems.get(position).ticket.getAdminName();
            }

            dateText.setText(visibleItems.get(position).ticket.getCreateDate());
            topicText.setText(visibleItems.get(position).ticket.getTopic());
            descText.setText(visibleItems.get(position).ticket.getMessage());
            ticketImage.setImageBitmap(Globals.ImageMethods.createUserImage(titleText, context));

            ticketImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!titleText.equals("Не установлено")) {
                        BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetFragment.newInstance(userId, adminId, getUser(userId, adminId));
                        bottomSheetDialogFragment.show(fragmentManager, bottomSheetDialogFragment.getTag());
                    }
                }
            });

            if (type == TYPE_AVAILABLE) {
                ticketImage.getRootView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(context)
                                .title("Принять заявку")
                                .content("Вы действительно хотите принять заявку?")
                                .positiveText(android.R.string.yes)
                                .negativeText(android.R.string.no)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        visibleItems.get(position).ticket.addAdmin(Globals.currentUser.getLogin(), Globals.currentUser.getUserName());

                                        databaseReference.child(DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE).child(visibleItems.get(position).ticket.getTicketId()).setValue(visibleItems.get(position).ticket);
                                        databaseReference.child(DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(visibleItems.get(position).ticket.getTicketId()).removeValue();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                    }
                });
            } else if (type == TYPE_MYACCEPTED){
                ticketImage.getRootView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MessagingActivity.class);

                        intent.putExtra("userName", visibleItems.get(position).ticket.getUserName());
                        intent.putExtra("chatRoom", visibleItems.get(position).ticket.getTicketId());

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });

                ticketImage.getRootView().setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View arg0) {
                        new MaterialDialog.Builder(context)
                                .title("Подтверждение решения проблемы по заявке " + visibleItems.get(position).ticket.getTicketId() + " от " + visibleItems.get(position).ticket.getCreateDate())
                                .content("Ваша проблема действительно была решена?")
                                .positiveText(android.R.string.yes)
                                .negativeText(android.R.string.no)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        databaseReference.child(DatabaseVariables.Tickets.DATABASE_SOLVED_TICKET_TABLE).child(visibleItems.get(position).ticket.getTicketId()).setValue(visibleItems.get(position).ticket);
                                        databaseReference.child(DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE).child(visibleItems.get(position).ticket.getTicketId()).removeValue();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();

                        return true;
                    }
                });
            } else if (type == TYPE_MYCREATED){
                ticketImage.getRootView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MessagingActivity.class);
                        if (visibleItems.get(position).ticket.getAdminId() == null || visibleItems.get(position).ticket.getAdminId().equals("")) {
                            Toast.makeText(context, "Администратор еще не просматривал ваше сообщение, подождите", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            intent.putExtra("userName", visibleItems.get(position).ticket.getAdminName());
                            intent.putExtra("chatRoom", visibleItems.get(position).ticket.getTicketId());
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });

                ticketImage.getRootView().setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View arg0) {
                        if (visibleItems.get(position).ticket.getAdminId() == null || visibleItems.get(position).ticket.getAdminId().equals("")) {
                            new MaterialDialog.Builder(context)
                                    .title("Отзыв заявки " + visibleItems.get(position).ticket.getTicketId() + " от " + visibleItems.get(position).ticket.getCreateDate())
                                    .content("Вы действительно хотите отозвать данную заявку?")
                                    .positiveText(android.R.string.yes)
                                    .negativeText(android.R.string.no)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            databaseReference.child(DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(visibleItems.get(position).ticket.getTicketId()).removeValue();
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .show();
                        }
                        return true;
                    }
                });
            } else if (type == TYPE_OVERVIEWCREATED){
                ticketImage.getRootView().setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View arg0) {
                        new MaterialDialog.Builder(context)
                                .title("Отказ от заявки пользователя " + visibleItems.get(position).ticket.getUserName())
                                .content("Вы действительно хотите отказаться от данной заявки?")
                                .positiveText(android.R.string.yes)
                                .negativeText(android.R.string.no)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        visibleItems.get(position).ticket.removeAdmin();
                                        databaseReference.child(DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(visibleItems.get(position).ticket.getTicketId()).setValue(visibleItems.get(position).ticket);
                                        databaseReference.child(DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE).child(visibleItems.get(position).ticket.getTicketId()).removeValue();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflate(R.layout.item_header, parent));
            case TYPE_TICKET:
            default:
                return new TicketViewHolder(inflate(R.layout.item_ticket, parent));
        }
    }

    @Override
    public void onBindViewHolder(ExpandableRecyclerAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                ((HeaderViewHolder) holder).bind(position);
                break;
            case TYPE_TICKET:
            default:
                ((TicketViewHolder) holder).bind(position);
                break;
        }
    }

    private List<TicketListItem> getSampleItems(ArrayList<Ticket> values) {
        List<TicketListItem> items = new ArrayList<>();

        items.add(new TicketListItem("Заявки"));
        for (Ticket ticket : values)
            items.add(new TicketListItem(ticket));

        return items;
    }

    private User getUser(String userId, String adminId) {
        String find;
        find = userId;

        if (Globals.currentUser.getLogin().equals(userId))
            find = adminId;

        ArrayList<String> idList = new ArrayList<>();
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getLogin().compareTo(rhs.getLogin());
            }
        });

        for (int i = 0; i < users.size(); i++)
            idList.add(users.get(i).getLogin());
        int index = Collections.binarySearch(idList, find, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        return users.get(index);
    }

}
