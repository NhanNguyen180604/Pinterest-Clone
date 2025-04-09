package com.example.pinterest_clone_test2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pinterest_clone_test2.ui.account.board_tab.BoardTabObjectFragment;
import com.example.pinterest_clone_test2.ui.account.collage_tab.CollageTabObjectFragment;
import com.example.pinterest_clone_test2.ui.account.pin_tab.PinTabObjectFragment;

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
