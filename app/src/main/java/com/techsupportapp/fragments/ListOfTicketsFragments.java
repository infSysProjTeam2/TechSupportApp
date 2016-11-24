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

/**
 * Класс для фрагментов ViewPager, находящегося в ListOfTicketsActivity.class.
 * @author ahgpoug
 */
public class ListOfTicketsFragments {
    /**
     * Адаптер для фрагментов
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private FirstFragment firstFragment;
        private SecondFragment secondFragment;
        private ThirdFragment thirdFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Метод, загружающий новые фрагменты по их номеру
         */
        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return FirstFragment.newInstance();
            else if (position == 1)
                return SecondFragment.newInstance();
            else
                return ThirdFragment.newInstance();
        }

        //Получение числа фрагментов
        @Override
        public int getCount() {
            return 3;
        }

        /**
         * Установка заголовков в TabLayout для фрагментов
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Доступные";
                case 1:
                    return "Активные";
                case 2:
                    return "Закрытые";
            }
            return null;
        }

        /**
         * Метод для запоминания ссылок на фрагменты
         */
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

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param listOfAvailableTickets список заявок, доступных для принятия.
         * @param usersList список всех пользователей.
         * @param context контекст Activity, где был создан фрагмент.
         * @param databaseReference контекст ссыылка на базу данных для ее редактирования.
         */
        public void updateFirstFragment(ArrayList<Ticket> listOfAvailableTickets, ArrayList<User> usersList, Context context, DatabaseReference databaseReference){
            firstFragment.updateContent(listOfAvailableTickets, usersList, context, databaseReference);
        }

        /**
         * Метод для обновления информации на втором фрагменте.
         * @param listOfMyClosedTickets список заявок, закрытых текущим пользователем.
         * @param usersList список всех пользователей.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public void updateSecondFragment(ArrayList<Ticket> listOfMyClosedTickets, ArrayList<User> usersList, Context context){
            secondFragment.updateContent(listOfMyClosedTickets, usersList, context);
        }

        /**
         * Метод для обновления информации на третьем фрагменте.
         * @param listOfSolvedTickets список заявок, закрытых всеми пользователями.
         * @param usersList список всех пользователей.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public void updateThirdFragment(ArrayList<Ticket> listOfSolvedTickets, ArrayList<User> usersList, Context context){
            thirdFragment.updateContent(listOfSolvedTickets, usersList, context);
        }
    }

    /**
     * Фрагемент списка доступных для принятия заявок
     */
    public static class FirstFragment extends Fragment {
        RecyclerView viewOfAvailableTickets;

        /**
         * Метод, вызывающийся при создании фрагмента
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            viewOfAvailableTickets = (RecyclerView) v.findViewById(R.id.recycler);

            return v;
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param listOfAvailableTickets список заявок, доступных для принятия.
         * @param usersList список всех пользователей.
         * @param context контекст Activity, где был создан фрагмент.
         * @param databaseReference контекст ссыылка на базу данных для ее редактирования.
         */
        public void updateContent(final ArrayList<Ticket> listOfAvailableTickets, final ArrayList<User> usersList, final Context context, final DatabaseReference databaseReference){
            //Создание списка заявок для передачи в TicketExpandableRecyclerAdapter.class
            ArrayList<TicketExpandableRecyclerAdapter.TicketListItem> ticketListItems = new ArrayList<>();

            //TODO сделать категории
            //Заполнение списка заявок для передачи в TicketExpandableRecyclerAdapter.class
            ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem("Категория 1"));
            for (Ticket ticket : listOfAvailableTickets)
                ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem(ticket));

            //Создание нового адаптера для viewOfAvailableTickets
            TicketExpandableRecyclerAdapter adapter = new TicketExpandableRecyclerAdapter(TicketExpandableRecyclerAdapter.TYPE_AVAILABLE, context, databaseReference, ticketListItems, usersList, getFragmentManager());
            adapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfAvailableTickets.setLayoutManager(mLayoutManager);
            viewOfAvailableTickets.setHasFixedSize(false);
            viewOfAvailableTickets.setAdapter(adapter);

            //Раскрытие категорий, которые были раскрыты ранее
            try {
;                for (int position : Globals.expandedItemsAvailable)
                    adapter.expandItems(position, true);
            } catch (Exception e){
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }

        /**
         * Создание нового экземпляра текущего фрагемента
         */
        public static FirstFragment newInstance() {
            FirstFragment f = new FirstFragment();
            return f;
        }
    }

    /**
     * Фрагемент списка заявок, закрытых текущим пользователем
     */
    public static class SecondFragment extends Fragment {
        RecyclerView viewOfMyClosedTickets;

        /**
         * Метод, вызывающийся при создании фрагмента
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            viewOfMyClosedTickets = (RecyclerView)v.findViewById(R.id.recycler);

            return v;
        }

        /**
         * Метод для обновления информации на втором фрагменте.
         * @param listOfMyClosedTickets список заявок, закрытых текущим пользователем.
         * @param usersList список всех пользователей.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public void updateContent(ArrayList<Ticket> listOfMyClosedTickets, ArrayList<User> usersList, Context context){
            //Создание списка заявок для передачи в TicketExpandableRecyclerAdapter.class
            ArrayList<TicketExpandableRecyclerAdapter.TicketListItem> ticketListItems = new ArrayList<>();

            //TODO сделать категории
            //Заполнение списка заявок для передачи в TicketExpandableRecyclerAdapter.class
            ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem("Категория 1"));
            for (Ticket ticket : listOfMyClosedTickets)
                ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem(ticket));

            //Создание нового адаптера для viewOfMyClosedTickets
            TicketExpandableRecyclerAdapter adapter = new TicketExpandableRecyclerAdapter(TicketExpandableRecyclerAdapter.TYPE_ACTIVE, context, null, ticketListItems, usersList, getFragmentManager());
            adapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfMyClosedTickets.setLayoutManager(mLayoutManager);
            viewOfMyClosedTickets.setHasFixedSize(false);
            viewOfMyClosedTickets.setAdapter(adapter);

            //Раскрытие категорий, которые были раскрыты ранее
            try {
                for (int position : Globals.expandedItemsActive)
                    adapter.expandItems(position, true);
            } catch (Exception e){
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();

        }

        /**
         * Создание нового экземпляра текущего фрагемента
         */
        public static SecondFragment newInstance() {
            SecondFragment f = new SecondFragment();
            return f;
        }
    }

    /**
     * Фрагемент списка заявок, закрытых всем пользователями
     */
    public static class ThirdFragment extends Fragment {
        RecyclerView viewOfSolvedTickets;

        /**
         * Метод, вызывающийся при создании фрагмента
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            viewOfSolvedTickets = (RecyclerView)v.findViewById(R.id.recycler);

            return v;
        }

        /**
         * Метод для обновления информации на третьем фрагменте.
         * @param listOfSolvedTickets список заявок, закрытых всеми пользователями.
         * @param usersList список всех пользователей.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public void updateContent(ArrayList<Ticket> listOfSolvedTickets, ArrayList<User> usersList, Context context){
            //Создание списка заявок для передачи в TicketExpandableRecyclerAdapter.class
            ArrayList<TicketExpandableRecyclerAdapter.TicketListItem> ticketListItems = new ArrayList<>();

            //TODO сделать категории
            //Заполнение списка заявок для передачи в TicketExpandableRecyclerAdapter.class
            ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem("Категория 1"));
            for (Ticket ticket : listOfSolvedTickets)
                ticketListItems.add(new TicketExpandableRecyclerAdapter.TicketListItem(ticket));

            //Создание нового адаптера для viewOfSolvedTickets
            TicketExpandableRecyclerAdapter adapter = new TicketExpandableRecyclerAdapter(TicketExpandableRecyclerAdapter.TYPE_CLOSED, context, null, ticketListItems, usersList, getFragmentManager());
            adapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            viewOfSolvedTickets.setLayoutManager(mLayoutManager);
            viewOfSolvedTickets.setHasFixedSize(false);
            viewOfSolvedTickets.setAdapter(adapter);

            //Раскрытие категорий, которые были раскрыты ранее
            try {
                for (int position : Globals.expandedItemsClosed)
                    adapter.expandItems(position, true);
            } catch (Exception e){
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();
        }

        /**
         * Создание нового экземпляра текущего фрагемента
         */
        public static ThirdFragment newInstance() {
            ThirdFragment f = new ThirdFragment();
            return f;
        }
    }
}
