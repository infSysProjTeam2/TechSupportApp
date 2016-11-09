package com.techsupportapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseReference;
import com.techsupportapp.R;
import com.techsupportapp.adapters.TicketRecyclerAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;
import com.techsupportapp.utility.ItemClickSupport;

import java.util.ArrayList;

public class ListOfTicketsFragments {
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private FirstFragment firstFragment;
        private SecondFragment secondFragment;
        private ThirdFragment thirdFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return FirstFragment.newInstance();
            else if (position == 1)
                return SecondFragment.newInstance();
            else
                return ThirdFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Доступные";
                case 1:
                    return "Решенные мной";
                case 2:
                    return "Решенные";
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    firstFragment = (FirstFragment) createdFragment;
                    break;
                case 1:
                    secondFragment = (SecondFragment) createdFragment;
                    break;
                case 2:
                    thirdFragment = (ThirdFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        public void updateFirstFragment(ArrayList<Ticket> listOfAvailableTickets, ArrayList<User> usersList, Context context, DatabaseReference databaseReference){
            firstFragment.updateContent(listOfAvailableTickets, usersList, context, databaseReference);
        }

        public void updateSecondFragment(ArrayList<Ticket> listOfMyClosedTickets, ArrayList<User> usersList, Context context){
            secondFragment.updateContent(listOfMyClosedTickets, usersList, context);
        }

        public void updateThirdFragment(ArrayList<Ticket> listOfSolvedTickets, ArrayList<User> usersList, Context context){
            thirdFragment.updateContent(listOfSolvedTickets, usersList, context);
        }
    }

    public static class FirstFragment extends Fragment {
        RecyclerView viewOfAvailableTickets;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            viewOfAvailableTickets = (RecyclerView) v.findViewById(R.id.recycler);

            return v;
        }

        public void updateContent(final ArrayList<Ticket> listOfAvailableTickets, final ArrayList<User> usersList, final Context context, final DatabaseReference databaseReference){
            TicketRecyclerAdapter adapter = new TicketRecyclerAdapter(context, listOfAvailableTickets, usersList, getFragmentManager());
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfAvailableTickets.setLayoutManager(mLayoutManager);
            viewOfAvailableTickets.setHasFixedSize(false);
            viewOfAvailableTickets.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            ItemClickSupport.addTo(viewOfAvailableTickets).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, final int position, View v) {
                    new MaterialDialog.Builder(context)
                            .title("Принять заявку")
                            .content("Вы действительно хотите принять заявку?")
                            .positiveText(android.R.string.yes)
                            .negativeText(android.R.string.no)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    listOfAvailableTickets.get(position).addAdmin(Globals.currentUser.getLogin(), Globals.currentUser.getUserName());

                                    databaseReference.child(DatabaseVariables.Tickets.DATABASE_MARKED_TICKET_TABLE).child(listOfAvailableTickets.get(position).getTicketId()).setValue(listOfAvailableTickets.get(position));
                                    databaseReference.child(DatabaseVariables.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(listOfAvailableTickets.get(position).getTicketId()).removeValue();
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
        }

        public static FirstFragment newInstance() {
            FirstFragment f = new FirstFragment();
            return f;
        }
    }

    public static class SecondFragment extends Fragment {
        RecyclerView viewOfMyClosedTickets;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            viewOfMyClosedTickets = (RecyclerView)v.findViewById(R.id.recycler);

            return v;
        }

        public void updateContent(ArrayList<Ticket> listOfMyClosedTickets, ArrayList<User> usersList, Context context){
            TicketRecyclerAdapter adapter = new TicketRecyclerAdapter(context, listOfMyClosedTickets, usersList, getFragmentManager());
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfMyClosedTickets.setLayoutManager(mLayoutManager);
            viewOfMyClosedTickets.setHasFixedSize(false);
            viewOfMyClosedTickets.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        public static SecondFragment newInstance() {
            SecondFragment f = new SecondFragment();
            return f;
        }
    }

    public static class ThirdFragment extends Fragment {
        RecyclerView viewOfSolvedTickets;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            viewOfSolvedTickets = (RecyclerView)v.findViewById(R.id.recycler);

            return v;
        }

        public void updateContent(ArrayList<Ticket> listOfSolvedTickets, ArrayList<User> usersList, Context context){
            TicketRecyclerAdapter adapter = new TicketRecyclerAdapter(context, listOfSolvedTickets, usersList, getFragmentManager());
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfSolvedTickets.setLayoutManager(mLayoutManager);
            viewOfSolvedTickets.setHasFixedSize(false);
            viewOfSolvedTickets.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        public static ThirdFragment newInstance() {
            ThirdFragment f = new ThirdFragment();
            return f;
        }
    }
}
