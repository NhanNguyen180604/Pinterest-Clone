package com.example.pinterest_clone_test2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pinterest_clone_test2.ui.notifications.MessagesListFragment;
import com.example.pinterest_clone_test2.ui.notifications.NotificationsListFragment;

public class NotificationsPagerAdapter extends FragmentStateAdapter {
    public NotificationsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new NotificationsListFragment();
            case 1:
                return new MessagesListFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
