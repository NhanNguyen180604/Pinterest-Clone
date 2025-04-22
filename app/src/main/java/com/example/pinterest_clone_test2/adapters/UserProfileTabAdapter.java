package com.example.pinterest_clone_test2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pinterest_clone_test2.ui.user.board_tab.UserProfileBoardsFragment;
import com.example.pinterest_clone_test2.ui.user.pin_tab.UserProfilePinsFragment;

public class UserProfileTabAdapter extends FragmentStateAdapter {
    private final String userId;
    private final String source;
    private final boolean isSelf;

    public static final int PINS_TAB = 0;
    public static final int BOARDS_TAB = 1;
    public static final int TAB_COUNT = 2;

    public UserProfileTabAdapter(FragmentActivity activity, String userId, String source, boolean isSelf) {
        super(activity);
        this.userId = userId;
        this.source = source;
        this.isSelf = isSelf;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == PINS_TAB) {
            return UserProfilePinsFragment.newInstance(userId, source);
        } else {
            return UserProfileBoardsFragment.newInstance(userId, source, isSelf);
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}