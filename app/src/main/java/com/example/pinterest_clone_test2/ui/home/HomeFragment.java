package com.example.pinterest_clone_test2.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.ViewPagerHomeAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentHomeBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    ViewPager2 view_pager;
    TabLayout tab_layout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<List<Pin>> pinTabs = new ArrayList<>();
        pinTabs.add(Pin.testData);
        pinTabs.add(Pin.testData);

        view_pager = binding.homePager;
        ViewPagerHomeAdapter adapter = new ViewPagerHomeAdapter(this, pinTabs);
        view_pager.setAdapter(adapter);

        tab_layout = binding.homeTabPager;
        new TabLayoutMediator(tab_layout, view_pager,
                (tab, position) -> {
                    tab.setText(String.format(Locale.US, "Tab %02d", position + 1));
                }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}