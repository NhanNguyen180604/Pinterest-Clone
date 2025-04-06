package com.example.pinterest_clone_test2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.ui.home.TabObjectFragment;

import java.util.List;

public class ViewPagerHomeAdapter extends FragmentStateAdapter {
    List<Board> boards;

    public ViewPagerHomeAdapter(@NonNull Fragment fragment, List<Board> boards) {
        super(fragment);
        this.boards = boards;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new TabObjectFragment(boards.get(position));
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }
}