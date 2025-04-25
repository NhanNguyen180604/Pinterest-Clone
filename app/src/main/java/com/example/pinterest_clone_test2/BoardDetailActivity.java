package com.example.pinterest_clone_test2;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.pinterest_clone_test2.databinding.ActivityBoardDetailBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;

public class BoardDetailActivity extends AppCompatActivity {
    ActivityBoardDetailBinding binding;
    public static String SOURCE = "BoardDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBoardDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String boardId = getIntent().getStringExtra("boardId");

        if (boardId == null) {
            Toast.makeText(this, "No board ID passed", Toast.LENGTH_SHORT).show();
        }

        FirebaseBoardService.getBoardByIdWithPins(boardId, new FirebaseBoardService.GetSingleBoardWithPinsCallback() {
            @Override
            public void OnSuccess(Board board) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("board", board);
                binding.btnBack.setOnClickListener(v -> updatePinOrder(board));
                getOnBackPressedDispatcher().addCallback(BoardDetailActivity.this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        updatePinOrder(board);
                    }
                });

                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_board_detail);

                if (navHostFragment != null) {
                    NavController navController = navHostFragment.getNavController();
                    navController.navigate(R.id.boardDetailFragment2, bundle);

                    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                        if (destination.getId() == R.id.pinFragment4
                                || destination.getId() == R.id.boardDetailOrganizeFragment
                                || destination.getId() == R.id.userProfileFragment5) {
                            binding.btnBack.setVisibility(View.GONE);
                            binding.btnMore.setVisibility(View.GONE);
                            binding.btnShare.setVisibility(View.GONE);
                        } else {
                            binding.btnBack.setVisibility(View.VISIBLE);
                            binding.btnMore.setVisibility(View.VISIBLE);
                            binding.btnShare.setVisibility(View.VISIBLE);
                        }
                    });
                }

            }

            @Override
            public void OnFailure(Exception e) {
                Toast.makeText(BoardDetailActivity.this, "Failed to load board: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }

            private void updatePinOrder(Board board) {
                FirebaseBoardService.updatePinOrder(boardId, board.getPinsObj(), new FirebaseBoardService.UpdatePinOrderCallback() {
                    @Override
                    public void OnSuccess() {
                        finish();
                    }

                    @Override
                    public void OnFailure(Exception e) {
                        finish();
                    }
                });
            }
        });
    }

}