package com.example.pinterest_clone_test2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pinterest_clone_test2.ui.account.BoardTabObjectFragment;
import com.example.pinterest_clone_test2.ui.account.CollageTabObjectFragment;
import com.example.pinterest_clone_test2.ui.account.PinTabObjectFragment;

public class ViewPagerAccountAdapter extends FragmentStateAdapter {
    public ViewPagerAccountAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PinTabObjectFragment();
            case 1:
                return new BoardTabObjectFragment();
            case 2:
                return new CollageTabObjectFragment();
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
