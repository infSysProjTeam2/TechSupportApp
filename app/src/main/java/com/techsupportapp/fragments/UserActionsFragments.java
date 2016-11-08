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

public class UserActionsFragments {
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private FirstFragment firstFragment;
        private SecondFragment secondFragment;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return FirstFragment.newInstance();
            else
                return SecondFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 2;
        }

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

        public boolean updateFirstFragment(ArrayList<User> unverifiedUsersList, Context context, DatabaseReference databaseReference, boolean search){
            return firstFragment.updateContent(unverifiedUsersList, context, databaseReference, search);
        }

        public void updateSecondFragment(ArrayList<User> usersList, Context context){
            secondFragment.updateContent(usersList, context);
        }
    }

    public static class FirstFragment extends Fragment {
        RecyclerView unverifiedUsersView;
        boolean result;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            unverifiedUsersView = (RecyclerView) v.findViewById(R.id.recycler);

            return v;
        }

        public boolean updateContent(final ArrayList<User> unverifiedUsersList, final Context context, final DatabaseReference databaseReference, boolean search) {
            result = search;
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            unverifiedUsersView.setLayoutManager(mLayoutManager);
            unverifiedUsersView.setHasFixedSize(false);

            UnverifiedUserRecyclerAdapter adapter = new UnverifiedUserRecyclerAdapter(context, unverifiedUsersList);
            unverifiedUsersView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

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
                                        try {
                                            databaseReference.child(UserActionsActivity.getDatabaseUserPath(selectedUser)).child(selectedUser.getBranchId()).setValue(selectedUser);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        result = false;

                                        databaseReference.child(DatabaseVariables.Users.DATABASE_UNVERIFIED_USER_TABLE).child(selectedUser.getBranchId()).removeValue();
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
                                    result = false;

                                    databaseReference.child(DatabaseVariables.Users.DATABASE_UNVERIFIED_USER_TABLE).child(selectedUser.getBranchId()).removeValue();
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

            public static FirstFragment newInstance() {
            FirstFragment f = new FirstFragment();
            return f;
        }
    }

    public static class SecondFragment extends Fragment {
        RecyclerView usersView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            usersView = (RecyclerView) v.findViewById(R.id.recycler);

            return v;
        }


        public void updateContent(final ArrayList<User> usersList, Context context){
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);;
            usersView.setLayoutManager(mLayoutManager);
            usersView.setHasFixedSize(false);

            UserRecyclerAdapter adapter = new UserRecyclerAdapter(context, usersList);
            usersView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            ItemClickSupport.addTo(usersView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView,final int position, View v) {
                    BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetFragment.newInstance(usersList.get(position).getLogin(), Globals.currentUser.getLogin(), usersList.get(position));
                    bottomSheetDialogFragment.show(getFragmentManager(), bottomSheetDialogFragment.getTag());
                }
            });
        }
        public static SecondFragment newInstance() {
            SecondFragment f = new SecondFragment();
            return f;
        }
    }
}
