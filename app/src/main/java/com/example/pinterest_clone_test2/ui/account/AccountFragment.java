package com.example.pinterest_clone_test2.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.ViewPagerAccountAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentAccountBinding;
import com.example.pinterest_clone_test2.ui.modal_bottom_sheets.CreatingModalBottomSheet;
import com.google.android.material.tabs.TabLayoutMediator;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;

    public AccountFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPagerAccountAdapter adapter = new ViewPagerAccountAdapter(this);
        binding.accountPager.setAdapter(adapter);
        binding.accountPager.setCurrentItem(1, false);
        new TabLayoutMediator(binding.tabLayout, binding.accountPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Pins");
                    break;
                case 1:
                    tab.setText("Boards");
                    break;
                case 2:
                    tab.setText("Collages");
                    break;
            }
        }).attach();

        binding.accountPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 2) {
                    binding.accountSearchView.setVisibility(View.GONE);
                    binding.btnAddNewPin.setVisibility(View.GONE);
                } else {
                    binding.accountSearchView.setVisibility(View.VISIBLE);
                    binding.btnAddNewPin.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(
                        R.id.action_navigation_account_to_settingsDrawerFragment,
                        null,
                        null,
                        null
                );
            }
        });

        binding.btnAddNewPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreatingModalBottomSheet modalBottomSheet = new CreatingModalBottomSheet();
                modalBottomSheet.show(requireActivity().getSupportFragmentManager(), CreatingModalBottomSheet.TAG);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}