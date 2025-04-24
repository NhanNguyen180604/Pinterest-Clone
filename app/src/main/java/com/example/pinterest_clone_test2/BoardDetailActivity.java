package com.example.pinterest_clone_test2;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.ActivityBoardDetailBinding;
import com.example.pinterest_clone_test2.databinding.FragmentBoardDetailBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.account.board_tab.AddCollaboratorBottomSheet;
import com.example.pinterest_clone_test2.ui.pin.PinFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BoardDetailActivity extends AppCompatActivity {
    ActivityBoardDetailBinding binding;
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
                binding.btnBack.setOnClickListener(v -> {
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
                });

                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_board_detail);

                if (navHostFragment != null) {
                    NavController navController = navHostFragment.getNavController();
                    navController.navigate(R.id.boardDetailFragment2, bundle);

                    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                        if (destination.getId() == R.id.pinFragment4 || destination.getId() == R.id.boardDetailOrganizeFragment) {
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
        });

    }

}