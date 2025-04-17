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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.ViewPagerAccountAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentAccountBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.upload.UploadDialogFragment;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;

    public AccountFragment() {
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
                    tab.setText(getResources().getString(R.string.pins));
                    break;
                case 1:
                    tab.setText(getResources().getString(R.string.boards));
                    break;
                case 2:
                    tab.setText(getResources().getString(R.string.collages));
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

        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        if (currentUserDocument != null) {
            RequestOptions glideOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.turtle_huh)
                    .centerCrop();

            Glide.with(binding.btnAccount.getContext())
                    .load(currentUserDocument.getString("avatarUrl"))
                    .apply(glideOptions)
                    .into(binding.btnAccount);
        }

        binding.btnAccount.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_navigation_account_to_settingsDrawerFragment);
        });

        binding.btnAddNewPin.setOnClickListener(v -> {
            UploadDialogFragment uploadDialogFragment = new UploadDialogFragment();
            uploadDialogFragment.show(requireActivity().getSupportFragmentManager(), null);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}