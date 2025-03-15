package com.example.pinterest_clone_test2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.pin.PinObjectFragment;

import java.util.List;

public class ViewPagerPinAdapter extends FragmentStateAdapter {
    List<Pin> pins;
    int initial_position;

    public ViewPagerPinAdapter(@NonNull Fragment fragment, List<Pin> pins, int initial_position) {
        super(fragment);
        this.pins = pins;
        this.initial_position = initial_position;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new PinObjectFragment(pins.get(position));
    }

    @Override
    public int getItemCount() {
        return pins.size();
    }
}
