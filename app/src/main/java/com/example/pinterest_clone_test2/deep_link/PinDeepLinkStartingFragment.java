package com.example.pinterest_clone_test2.deep_link;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.pinterest_clone_test2.R;

public class PinDeepLinkStartingFragment extends Fragment {

    public PinDeepLinkStartingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pin_deep_link_starting, container, false);
    }
}