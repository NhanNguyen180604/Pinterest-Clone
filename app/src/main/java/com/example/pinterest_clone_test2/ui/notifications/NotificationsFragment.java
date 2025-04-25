package com.example.pinterest_clone_test2.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.NotificationsPagerAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentNotificationsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public NotificationsFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NotificationsPagerAdapter adapter = new NotificationsPagerAdapter(this);
        binding.notificationViewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.notificationViewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getResources().getString(R.string.updates));
                    } else {
                        tab.setText(getResources().getString(R.string.messages));
                    }
                })
                .attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}