package com.example.pinterest_clone_test2.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.InterestsAdapter;
import com.example.pinterest_clone_test2.services.firebase.FirebaseTagService;

import java.util.ArrayList;
import java.util.List;

public class FragmentRegisterInterests extends Fragment implements InterestsAdapter.OnTagSelectedListener {

    private RecyclerView rvInterests;
    private Button btnNext;
    private ImageButton btnBack;
    private TextView tvDescription;
    private InterestsAdapter adapter;
    private static final int REQUIRED_SELECTIONS = 5;

    public FragmentRegisterInterests() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_interests, container, false);

        // Initialize views
        rvInterests = view.findViewById(R.id.rv_interests);
        btnNext = view.findViewById(R.id.btn_next);
        btnBack = view.findViewById(R.id.btn_back);
        tvDescription = view.findViewById(R.id.tv_description);

        // Make sure fixed tags are initialized
        FirebaseTagService.initFixedTags(requireContext());

        // Get the list of fixed tags (these are the English names)
        List<String> fixedTagsEnglish = FirebaseTagService.getFixedTags();

        // Convert to localized tag names for display
        List<String> localizedTags = new ArrayList<>();
        for (String tag : fixedTagsEnglish) {
            int resId = getResources().getIdentifier(tag, "string", requireContext().getPackageName());
            if (resId != 0) {
                localizedTags.add(getString(resId));
            } else {
                // Fallback to the English name if no resource is found
                localizedTags.add(tag);
            }
        }

        // Setup RecyclerView
        rvInterests.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        adapter = new InterestsAdapter(requireContext(), localizedTags, this);
        rvInterests.setAdapter(adapter);

        // Set click listeners
        btnNext.setOnClickListener(v -> {
            List<String> selectedTags = adapter.getSelectedTags();
            if (selectedTags.size() == REQUIRED_SELECTIONS) {
                ((LoginActivity) requireActivity()).registerInterests(selectedTags);
            } else {
                int remaining = REQUIRED_SELECTIONS - selectedTags.size();
                String message = getString(R.string.select_more_interests, remaining);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Initially disable the next button
        updateNextButtonState(0);

        return view;
    }

    @Override
    public void onTagSelected(int selectedCount) {
        updateNextButtonState(selectedCount);
    }

    private void updateNextButtonState(int selectedCount) {
        if (selectedCount == REQUIRED_SELECTIONS) {
            btnNext.setEnabled(true);
            btnNext.setBackgroundTintList(requireContext().getColorStateList(R.color.red_pinterest));
            btnNext.setTextColor(requireContext().getColorStateList(android.R.color.white));
        } else {
            btnNext.setEnabled(selectedCount > 0);
            btnNext.setBackgroundTintList(requireContext().getColorStateList(R.color.grey_text_box));
            btnNext.setTextColor(requireContext().getColorStateList(R.color.black));

            // Update description text
            int remaining = REQUIRED_SELECTIONS - selectedCount;
            String descriptionText = selectedCount > 0
                    ? getString(R.string.select_more_interests, remaining)
                    : getString(R.string.interests_description);
            tvDescription.setText(descriptionText);
        }
    }
}