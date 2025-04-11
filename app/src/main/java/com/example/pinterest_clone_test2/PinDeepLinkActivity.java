package com.example.pinterest_clone_test2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pinterest_clone_test2.databinding.ActivityPinDeepLinkBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class PinDeepLinkActivity extends AppCompatActivity {

    ActivityPinDeepLinkBinding binding;
    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinDeepLinkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        FirebaseUserService.initUserDocument();

        Intent intent = getIntent();
        Uri data = intent.getData();
        NavController navController = Navigation.findNavController(PinDeepLinkActivity.this, R.id.nav_host_fragment_activity_pin_deep_link);

        if (data != null) {
            String pinId = data.getQueryParameter("pinId");
            if (pinId == null || pinId.isEmpty()) {
                navigateToErrorFragment(navController, getResources().getString(R.string.no_pin_id));
            } else {
                List<String> pinIds = new ArrayList<>();
                pinIds.add(pinId);

                FirebasePinService.fetchPinsFromIds(pinIds, new FirebasePinService.OnPinsFetchedFromIdsCallback() {
                    @Override
                    public void onSuccess(List<Pin> pins) {
                        if (pins.isEmpty()) {
                            navigateToErrorFragment(navController, getResources().getString(R.string.pin_not_found));
                            return;
                        }
                        Bundle args = new Bundle();
                        args.putParcelableArrayList("pins", new ArrayList<>(pins));
                        args.putInt("position", 0);
                        args.putString("source", "pinDeepLink");
                        navController.navigate(R.id.action_pinDeepLinkStartingFragment_to_pinFragmentDeepLink, args);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("PinDeepLink", "Failed to fetch pin");
                        e.printStackTrace();
                        navigateToErrorFragment(navController, getResources().getString(R.string.fetch_pin_failure));
                    }
                });
            }
        } else {
            navigateToErrorFragment(navController, getResources().getString(R.string.fetch_pin_failure));
            Log.e("PinDeepLink", "Data is null, da fuq");
        }

        findViewById(R.id.nav_host_fragment_activity_pin_deep_link).post(() -> {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.pinDeepLinkStartingFragment) {
                    if (firstTime) {
                        firstTime = false;
                        return;
                    }

                    if (isTaskRoot()) {
                        returnHome();
                    }

                    // when we get back to the starting fragment, finish the activity
                    finish();
                }
            });
        });

        binding.btnReturnHome.setOnClickListener(v -> {
            returnHome();
            finish();
        });
    }

    private void returnHome() {
        Intent mainActivityIntent = new Intent(PinDeepLinkActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
    }

    private void navigateToErrorFragment(NavController navController, String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString("message", errorMessage);
        navController.navigate(R.id.action_pinDeepLinkStartingFragment_to_pinDeepLinkErrorFragment, bundle);
    }
}