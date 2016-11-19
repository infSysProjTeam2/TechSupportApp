package com.techsupportapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseReference;
import com.techsupportapp.R;
import com.techsupportapp.UserActionsActivity;
import com.techsupportapp.adapters.UnverifiedUserRecyclerAdapter;
import com.techsupportapp.adapters.UserRecyclerAdapter;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;
import com.techsupportapp.utility.ItemClickSupport;

import java.util.ArrayList;

/**
 * Класс для фрагментов ViewPager, находящегося в UserActionsActivity.class.
 * @author ahgpoug
 */
public class UserActionsFragments {
    /**
     * Адаптер для фрагментов
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private FirstFragment firstFragment;
        private SecondFragment secondFragment;
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
            else
                return SecondFragment.newInstance();
        }

        //Получение числа фрагментов
        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Установка заголовков в TabLayout для фрагментов
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Не авторизованные";
                case 1:
                    return "Все";
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
            }
            return createdFragment;
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param unverifiedUsersList список пользователей с неподтвержденной регистрацией.
         * @param context контекст Activity, где был создан фрагмент.
         * @param databaseReference контекст ссыылка на базу данных для ее редактирования.
         * @param search параметр, было ли раскрыто поле для поиска.
         * @return true - нужно закрыть меню поиска. false - не нужно закрывать меню для поиска
         */
        public boolean updateFirstFragment(ArrayList<User> unverifiedUsersList, Context context, DatabaseReference databaseReference, boolean search){
            return firstFragment.updateContent(unverifiedUsersList, context, databaseReference, search);
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param usersList список пользователей с подтвержденной регистрацией.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public void updateSecondFragment(ArrayList<User> usersList, Context context){
            secondFragment.updateContent(usersList, context);
        }
    }

    /**
     * Фрагемент списка пользователей с неподтвержденной регистрацией.
     */
    public static class FirstFragment extends Fragment {
        RecyclerView unverifiedUsersView;
        boolean result;

        /**
         * Метод, вызывающийся при создании фрагмента
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            unverifiedUsersView = (RecyclerView) v.findViewById(R.id.recycler);

            return v;
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param unverifiedUsersList список пользователей с неподтвержденной регистрацией.
         * @param context контекст Activity, где был создан фрагмент.
         * @param databaseReference контекст ссыылка на базу данных для ее редактирования.
         * @param search параметр, было ли раскрыто поле для поиска.
         * @return true - нужно закрыть меню поиска. false - не нужно закрывать меню для поиска
         */
        public boolean updateContent(final ArrayList<User> unverifiedUsersList, final Context context, final DatabaseReference databaseReference, boolean search) {
            result = search;
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            unverifiedUsersView.setLayoutManager(mLayoutManager);
            unverifiedUsersView.setHasFixedSize(false);

            //Создание нового адаптера для unverifiedUsersView
            UnverifiedUserRecyclerAdapter adapter = new UnverifiedUserRecyclerAdapter(context, unverifiedUsersList);
            unverifiedUsersView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            //Одиночный клик по unverifiedUsersView
            ItemClickSupport.addTo(unverifiedUsersView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, final int position, View v) {
                    final User selectedUser = unverifiedUsersList.get(position);

                    try {
                        new MaterialDialog.Builder(context)
                                .title("Подтвердить пользователя " + selectedUser.getBranchId())
                                .content(UserActionsActivity.getLogInMessage(selectedUser))
                                .positiveText(android.R.string.yes)
                                .negativeText(android.R.string.no)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        //Подтверждение пользователя
                                        //Перенос из списка неподтвержденных пользователей в список подтвержденных
                                        try {
                                            databaseReference.child(UserActionsActivity.getDatabaseUserPath(selectedUser)).child(selectedUser.getBranchId()).setValue(selectedUser);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        result = false;

                                        databaseReference.child(DatabaseVariables.FullPath.Users.DATABASE_UNVERIFIED_USER_TABLE).child(selectedUser.getBranchId()).removeValue();
                                        Toast.makeText(context, "Пользователь добавлен в базу данных", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                    } catch (Exception e) {
                        Globals.showLongTimeToast(context, "Передана нулевая ссылка или неверно указаны права пользователя. Обратитесь к разработчику");
                    }
                }
            });

            //Долгий клик по unverifiedUsersView
            ItemClickSupport.addTo(unverifiedUsersView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                    final User selectedUser = unverifiedUsersList.get(position);
                    new MaterialDialog.Builder(context)
                            .title("Отклонить заявку пользователя " + selectedUser.getBranchId())
                            .content("Вы действительно хотите отклонить заявку пользователя " + selectedUser.getLogin() + " на регистрацию?")
                            .positiveText(android.R.string.yes)
                            .negativeText(android.R.string.no)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    //Отмена завявки пользователя на регистрацию
                                    //Удалиние заявки на регистрацию
                                    result = false;

                                    databaseReference.child(DatabaseVariables.FullPath.Users.DATABASE_UNVERIFIED_USER_TABLE).child(selectedUser.getBranchId()).removeValue();
                                    Globals.showLongTimeToast(context, "Заявка пользователя была успешно отклонена");
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
            return result;
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
     * Фрагемент списка всех пользователей
     */
    public static class SecondFragment extends Fragment {
        RecyclerView usersView;

        /**
         * Метод, вызывающийся при создании фрагмента
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            usersView = (RecyclerView) v.findViewById(R.id.recycler);

            return v;
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param usersList список пользователей с подтвержденной регистрацией.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public void updateContent(final ArrayList<User> usersList, Context context){
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            usersView.setLayoutManager(mLayoutManager);
            usersView.setHasFixedSize(false);

            UserRecyclerAdapter adapter = new UserRecyclerAdapter(context, usersList);
            usersView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            //Одиночных клик по usersView
            ItemClickSupport.addTo(usersView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView,final int position, View v) {
                    //Вызов диалогового окна, выдвагающегося снизу
                    BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetFragment.newInstance(usersList.get(position).getLogin(), Globals.currentUser.getLogin(), usersList.get(position));
                    bottomSheetDialogFragment.show(getFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            });
        }

        /**
         * Создание нового экземпляра текущего фрагемента
         */
        public static SecondFragment newInstance() {
            SecondFragment f = new SecondFragment();
            return f;
        }
    }
}
