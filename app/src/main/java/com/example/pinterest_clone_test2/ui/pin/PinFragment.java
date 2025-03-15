package com.example.pinterest_clone_test2.ui.pin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pinterest_clone_test2.adapters.ViewPagerPinAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentPinBinding;
import com.example.pinterest_clone_test2.models.Pin;

import java.util.List;

public class PinFragment extends Fragment {

    FragmentPinBinding binding;
    List<Pin> pins;
    private int initial_position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            pins = getArguments().getParcelableArrayList("pins");  // will use this when we have a backend server
            pins = Pin.testData;
            initial_position = getArguments().getInt("position");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPagerPinAdapter adapter = new ViewPagerPinAdapter(this, pins, initial_position);
        binding.pinPager.setAdapter(adapter);
        binding.pinPager.setCurrentItem(initial_position, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}