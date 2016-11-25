package com.techsupportapp.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.techsupportapp.EditUserProfileActivity;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.Globals;

/**
 * Класс для фрагмента диалогового окна, выдвигающегося снизу.
 * @author ahgpoug
 */
public class BottomSheetFragment extends BottomSheetDialogFragment {

    private String userId;
    private String specialistId;
    private String userName;
    private String login;
    private String regDate;
    private String workPlace;
    private int role;

    /**
     * Создание нового фрагмента.
     * @param userId Login обычного пользователя, привязанного к заявке.
     * @param specialistId Login специалиста, привязанного к заявке.
     * @param user объект пользователя, профиль которого будет просмотрен.
     */
    public static BottomSheetFragment newInstance(String userId, String specialistId, User user) {
        BottomSheetFragment fragment = new BottomSheetFragment();

        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("specialistId", specialistId);
        args.putString("userName", user.getUserName());
        args.putString("login", user.getLogin());
        args.putString("regDate", user.getRegistrationDate());
        args.putString("workPlace", user.getWorkPlace());
        args.putInt("role", user.getRole());

        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Метод, вызывающийся при создании нового экзмепляра диалога
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getArguments().getString("userId");
        specialistId = getArguments().getString("specialistId");
        userName = getArguments().getString("userName");
        login = getArguments().getString("login");
        regDate = getArguments().getString("regDate");
        workPlace = getArguments().getString("workPlace");
        role = getArguments().getInt("role");
    }

    /**
     * Управление поведением диалого
     */
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //Убрать заголовок в toolbar
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    /**
     * Установка View в диалоге
     */
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_bottom_sheet, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setPeekHeight(1200); //Начальная высота диалога
        }

        //Установка toolbar
        Toolbar toolbar = (Toolbar) contentView.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.editUser) { //Вызов EditUserProfileActivity.class для редактирования профиля
                    Intent intent;
                    intent = new Intent(getContext(), EditUserProfileActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("currUserId", Globals.currentUser.getLogin());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return true;
            }
        });
        toolbar.inflateMenu(R.menu.menu_bottom_dialog);
        toolbar.setTitle(userName);

        //Запись данных о пользователе в TextView
        final TextView userIdTV = (TextView) contentView.findViewById(R.id.userId);
        TextView regDateTV = (TextView) contentView.findViewById(R.id.regDate);
        TextView workPlaceTV = (TextView) contentView.findViewById(R.id.workPlace);
        TextView accessLevelTV = (TextView) contentView.findViewById(R.id.accessLevel);

        userIdTV.setText(login);
        regDateTV.setText(regDate);
        workPlaceTV.setText(workPlace);

        //Проверка Login текущего пользователя
        String find = userId;
        if (Globals.currentUser.getLogin().equals(userId))
            find = specialistId;

        //Если пользователь - не диспетчер и это не его профиль, то редактировать его нельзя
        if (Globals.currentUser.getRole() != User.MANAGER && !Globals.currentUser.getLogin().equals(find))
            toolbar.findViewById(R.id.editUser).setVisibility(View.GONE);

        if (role == User.SIMPLE_USER)
            accessLevelTV.setText("Пользователь");
        else if (role == User.DEPARTMENT_MEMBER)
            accessLevelTV.setText("Работник отдела");
        else if (role == User.DEPARTMENT_CHIEF)
            accessLevelTV.setText("Начальник отдела");
        else if (role == User.MANAGER)
            accessLevelTV.setText("Диспетчер");
    }
}