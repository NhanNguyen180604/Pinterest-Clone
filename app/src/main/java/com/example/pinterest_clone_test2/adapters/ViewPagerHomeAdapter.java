package com.example.pinterest_clone_test2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.home.TabObjectFragment;

import java.util.List;

public class ViewPagerHomeAdapter extends FragmentStateAdapter {
    List<List<Pin>> pinTabs;

    public ViewPagerHomeAdapter(@NonNull Fragment fragment, List<List<Pin>> pinTabs) {
        super(fragment);
        this.pinTabs = pinTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new TabObjectFragment(pinTabs.get(position));
    }

    @Override
    public int getItemCount() {
        return pinTabs.size();
    }
}