package com.example.pinterest_clone_test2.deep_link;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.BoardDetailActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.ActivityAddCollaboratorsBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

public class AddCollaboratorsDeepLinkActivity extends AppCompatActivity {
    ActivityAddCollaboratorsBinding binding;
    String userId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddCollaboratorsBinding.inflate(getLayoutInflater());
        Uri data = getIntent().getData();
        assert data != null;
        Log.d("AddCollaboratorActivity", "Full URI: " + data);
        for (String param : data.getQueryParameterNames()) {
            Log.d("ParamCheck", param + ": " + data.getQueryParameter(param));
        }
        String boardId = data.getQueryParameter("boardId");
        userId = data.getQueryParameter("userId");
        Log.d("AddCollaboratorActivity", "userId: " + userId);
        setContentView(binding.getRoot());
        binding.btnAccept.setOnClickListener(v -> {
            binding.btnAccept.setText("Loading…");
            if (boardId == null || userId == null) {
                binding.btnAccept.setText("Accept");
                Toast.makeText(this, "Invalid invite link", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseBoardService.addCurrentUserToCollaborators(boardId, new FirebaseBoardService.AddCollaboratorServiceCallback() {
                @Override
                public void OnSuccess() {
                    binding.btnAccept.setText("Done");
                    Intent intent = new Intent(AddCollaboratorsDeepLinkActivity.this, BoardDetailActivity.class);
                    intent.putExtra("boardId", boardId);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void OnFailure(Exception e) {
                    binding.btnAccept.setText("Accept");
                    Toast.makeText(AddCollaboratorsDeepLinkActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        binding.btnDeny.setOnClickListener(v -> finish());
        assert userId != null;
        FirebaseUserService.getUserById(userId, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("name");
                    binding.tvInviteCollab.setText(username + " " + getResources().getString(R.string.has_invited_you));
                    RequestOptions glideOptions = new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .error(R.drawable.turtle_huh)
                            .centerCrop();

                    Glide.with(binding.ivProfileImg.getContext())
                            .load(document.getString("avatarUrl"))
                            .apply(glideOptions)
                            .into(binding.ivProfileImg);
                } else {
                    Log.d("Firestore", "No such user");
                }
            } else {
                Log.e("Firestore", "Error fetching user", task.getException());
            }
        });
        FirebaseBoardService.getBoardByIdWithPins(boardId, new FirebaseBoardService.GetSingleBoardWithPinsCallback() {
            @Override
            public void OnSuccess(Board board) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("board", board);
                bundle.putString("source", "DeepLink");
                getOnBackPressedDispatcher().addCallback( AddCollaboratorsDeepLinkActivity.this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                    }
                });

                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_add_collab);

                if (navHostFragment != null) {
                    NavController navController = navHostFragment.getNavController();
                    navController.navigate(R.id.boardDetailFragment3, bundle);
                }

            }

            @Override
            public void OnFailure(Exception e) {
                finish();
            }

        });
    }
}

