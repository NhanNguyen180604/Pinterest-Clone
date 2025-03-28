package com.example.pinterest_clone_test2.ui.admin.manage_pin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdminAdapter;
import com.example.pinterest_clone_test2.models.Pin;

import java.util.ArrayList;
import java.util.List;

public class ManagePinFragment extends Fragment {

    private RecyclerView recyclerView;
    private PinListAdminAdapter pinListAdminAdapter;
    private List<Pin> pinList = new ArrayList<>();
    private List<Pin> filteredPinList = new ArrayList<>();
    private SearchView searchView;

    public ManagePinFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_pin, container, false);

        // Initialize RecyclerView and SearchView
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);

        // Set up RecyclerView with LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data from Pin.testData (you can replace this with real data)
        pinList = Pin.testData;
        filteredPinList.addAll(pinList);

        // Set up the adapter
        pinListAdminAdapter = new PinListAdminAdapter(filteredPinList, new PinListAdminAdapter.PinAdminActionListener() {
            @Override
            public void onEditClick(Pin pin) {
                Toast.makeText(getContext(), "Edit Pin: " + pin.getId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Pin pin) {
                Toast.makeText(getContext(), "Deleted Pin: " + pin.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(pinListAdminAdapter);

        // Handle search query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Search logic: filter pins by author name or other parameters
                filterPins(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the list as the user types
                filterPins(newText);
                return false;
            }
        });

        return view;
    }

    // Method to filter pins based on the search query
    private void filterPins(String query) {
        filteredPinList.clear();

        if (query.isEmpty()) {
            filteredPinList.addAll(pinList);
        } else {
            for (Pin pin : pinList) {
                if (pin.getAuthorId().toLowerCase().contains(query.toLowerCase())) {
                    filteredPinList.add(pin);
                }
            }
        }
    }
}
