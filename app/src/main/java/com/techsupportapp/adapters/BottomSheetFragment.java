package com.techsupportapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.techsupportapp.EditUserProfileActivity;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.Globals;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private String userId;
    private String adminId;
    private String userName;
    private String login;
    private String regDate;
    private String workPlace;
    private int role;

    public static BottomSheetFragment newInstance(String userId, String adminId, User user) {
        BottomSheetFragment fragment = new BottomSheetFragment();

        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("adminId", adminId);
        args.putString("userName", user.getUserName());
        args.putString("login", user.getLogin());
        args.putString("regDate", user.getRegistrationDate());
        args.putString("workPlace", user.getWorkPlace());
        args.putInt("role", user.getRole());

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getArguments().getString("userId");
        adminId = getArguments().getString("adminId");
        userName = getArguments().getString("userName");
        login = getArguments().getString("login");
        regDate = getArguments().getString("regDate");
        workPlace = getArguments().getString("workPlace");
        role = getArguments().getInt("role");
    }

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
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_bottom_sheet, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setPeekHeight(1100);
        }

        ImageButton editUser = (ImageButton) contentView.findViewById(R.id.editUserBtn);
        TextView userNameTV = (TextView) contentView.findViewById(R.id.userName);
        final TextView userIdTV = (TextView) contentView.findViewById(R.id.userId);
        TextView regDateTV = (TextView) contentView.findViewById(R.id.regDate);
        TextView workPlaceTV = (TextView) contentView.findViewById(R.id.workPlace);
        TextView accessLevelTV = (TextView) contentView.findViewById(R.id.accessLevel);

        userNameTV.setText(userName);
        userIdTV.setText(login);
        regDateTV.setText(regDate);
        workPlaceTV.setText(workPlace);

        String find = userId;
        if (Globals.currentUser.getLogin().equals(userId))
            find = adminId;

        if (Globals.currentUser.getRole() == User.SIMPLE_USER && !Globals.currentUser.getLogin().equals(find))
            editUser.setVisibility(View.GONE);

        if (role == User.SIMPLE_USER)
            accessLevelTV.setText("Пользователь");
        else if (role == User.DEPARTMENT_MEMBER)
            accessLevelTV.setText("Работник отдела");
        else if (role == User.ADMINISTRATOR)
            accessLevelTV.setText("Администратор");
        else if (role == User.DEPARTMENT_CHIEF)
            accessLevelTV.setText("Начальник отдела");

        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(getContext(), EditUserProfileActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("currUserId", Globals.currentUser.getLogin());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}