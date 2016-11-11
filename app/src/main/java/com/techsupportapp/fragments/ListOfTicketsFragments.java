package com.techsupportapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.innodroid.expandablerecycler.ExpandableRecyclerAdapter;
import com.techsupportapp.R;
import com.techsupportapp.adapters.TicketExpandableRecyclerAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.Globals;

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
            ArrayList<TicketExpandableRecyclerAdapter.TicketListItem> ticketListItems = new ArrayList<>();

            //TODO сделать категории
            ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem("Категория 1"));
            for (Ticket ticket : listOfAvailableTickets)
                ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem(ticket));

            TicketExpandableRecyclerAdapter adapter = new TicketExpandableRecyclerAdapter(TicketExpandableRecyclerAdapter.TYPE_AVAILABLE, context, databaseReference, ticketListItems, usersList, getFragmentManager());
            adapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfAvailableTickets.setLayoutManager(mLayoutManager);
            viewOfAvailableTickets.setHasFixedSize(false);
            viewOfAvailableTickets.setAdapter(adapter);

            try {
;                for (int position : Globals.expandedItemsAvailable)
                    adapter.expandItems(position, true);
            } catch (Exception e){
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
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
            ArrayList<TicketExpandableRecyclerAdapter.TicketListItem> ticketListItems = new ArrayList<>();

            //TODO сделать категории
            ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem("Категория 1"));
            for (Ticket ticket : listOfMyClosedTickets)
                ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem(ticket));

            TicketExpandableRecyclerAdapter adapter = new TicketExpandableRecyclerAdapter(TicketExpandableRecyclerAdapter.TYPE_MYCLOSED, context, null, ticketListItems, usersList, getFragmentManager());
            adapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfMyClosedTickets.setLayoutManager(mLayoutManager);
            viewOfMyClosedTickets.setHasFixedSize(false);
            viewOfMyClosedTickets.setAdapter(adapter);

            try {
                for (int position : Globals.expandedItemsMyClosed)
                    adapter.expandItems(position, true);
            } catch (Exception e){
                e.printStackTrace();
            }

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
            ArrayList<TicketExpandableRecyclerAdapter.TicketListItem> ticketListItems = new ArrayList<>();

            //TODO сделать категории
            ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem("Категория 1"));
            for (Ticket ticket : listOfSolvedTickets)
                ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem(ticket));

            TicketExpandableRecyclerAdapter adapter = new TicketExpandableRecyclerAdapter(TicketExpandableRecyclerAdapter.TYPE_CLOSED, context, null, ticketListItems, usersList, getFragmentManager());
            adapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfSolvedTickets.setLayoutManager(mLayoutManager);
            viewOfSolvedTickets.setHasFixedSize(false);
            viewOfSolvedTickets.setAdapter(adapter);

            try {
                for (int position : Globals.expandedItemsClosed)
                    adapter.expandItems(position, true);
            } catch (Exception e){
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();
        }

        public static ThirdFragment newInstance() {
            ThirdFragment f = new ThirdFragment();
            return f;
        }
    }
}
