package com.example.pinterest_clone_test2.ui.pin.edit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentEditPinBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EditPinFragment extends Fragment {
    FragmentEditPinBinding binding;
    Pin pin;
    EditPinFragmentViewModel viewModel;
    ExoPlayer mainExoPlayer;
    ExoPlayer boardExoPlayer;
    Handler handler = new Handler();

    public EditPinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = requireActivity().getIntent();
        pin = intent.getParcelableExtra("pin");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(requireActivity().getApplication(), requireActivity())).get(EditPinFragmentViewModel.class);
        binding = FragmentEditPinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        restoreStates();

        binding.btnClose.setOnClickListener(v -> {
            requireActivity().setResult(Activity.RESULT_CANCELED);
            requireActivity().finish();
        });

        if (pin == null) {
            showUnknownErrorToast();
            binding.btnSave.setEnabled(false);
            return;
        } else {
            binding.btnSave.setEnabled(true);
        }

        setBoardImageAndTextAsync();

        binding.btnSave.setOnClickListener(v -> {
            updatePin();
            binding.btnSave.setEnabled(false);
        });

        binding.tvClickableDelete.setOnClickListener(v -> {
            ConfirmDeletePinModalBottomSheet sheet = new ConfirmDeletePinModalBottomSheet(() -> {
                deletePin();
                binding.btnSave.setEnabled(false);
            });
            sheet.show(requireActivity().getSupportFragmentManager(), ConfirmDeletePinModalBottomSheet.TAG);
        });

        // set board image or video thumbnail
        if (pin.getType() == Pin.PinType.IMAGE) {
            binding.ivPreviewImage.setVisibility(View.VISIBLE);
            binding.playerView.setVisibility(View.GONE);
            binding.playerViewBoard.setVisibility(View.GONE);

            Glide.with(binding.ivPreviewImage.getContext())
                    .load(pin.getMediaUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .error(R.drawable.turtle_huh)
                            .centerCrop())
                    .into(binding.ivPreviewImage);

            Glide.with(binding.ivBoardImage.getContext())
                    .load(pin.getThumbnailUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .error(R.drawable.turtle_huh)
                            .centerCrop())
                    .into(binding.ivBoardImage);
        } else if (pin.getType() == Pin.PinType.GIF) {
            binding.ivPreviewImage.setVisibility(View.VISIBLE);
            binding.playerView.setVisibility(View.GONE);
            binding.playerViewBoard.setVisibility(View.GONE);

            Glide.with(binding.ivPreviewImage.getContext())
                    .asGif()
                    .load(pin.getMediaUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .error(R.drawable.turtle_huh)
                            .centerCrop())
                    .into(binding.ivPreviewImage);

            Glide.with(binding.ivBoardImage.getContext())
                    .asGif()
                    .load(pin.getMediaUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .error(R.drawable.turtle_huh)
                            .centerCrop())
                    .into(binding.ivBoardImage);
        } else {
            binding.ivPreviewImage.setVisibility(View.GONE);
            binding.playerView.setVisibility(View.VISIBLE);
            binding.playerViewBoard.setVisibility(View.VISIBLE);

            mainExoPlayer = new ExoPlayer.Builder(requireContext()).build();
            boardExoPlayer = new ExoPlayer.Builder(requireContext()).build();

            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(pin.getMediaUrl()));
            mainExoPlayer.setMediaItem(mediaItem);
            boardExoPlayer.setMediaItem(mediaItem);

            binding.playerView.setPlayer(mainExoPlayer);
            binding.playerViewBoard.setPlayer(boardExoPlayer);

            mainExoPlayer.prepare();
            mainExoPlayer.setPlayWhenReady(false);
            boardExoPlayer.prepare();
            boardExoPlayer.setPlayWhenReady(false);
        }

        binding.etPinTitle.setText(pin.getName());
        binding.etPinDescription.setText(pin.getDescription());
        binding.switchAllowComments.setChecked(pin.getAllowComment());

        binding.cvBoardImage.setOnClickListener(v -> navigateToPickBoards());
        binding.tvBoardName.setOnClickListener(v -> navigateToPickBoards());
        binding.ivChevronRight.setOnClickListener(v -> navigateToPickBoards());
    }

    private void setBoardImageAndTextAsync() {
        Thread thread = new Thread(() -> {
            QuerySnapshot currentUserBoardSnapshot = FirebaseBoardService.getCurrentUserBoardSnapshot();
            if (currentUserBoardSnapshot == null) {
                FirebaseBoardService.getUserBoards(getBoardServiceCallback);
            } else {
                getBoardServiceCallback.OnSuccess(currentUserBoardSnapshot);
            }
        });
        thread.start();
    }

    // super ultimate spaghetti
    FirebaseBoardService.GetBoardServiceCallback getBoardServiceCallback = new FirebaseBoardService.GetBoardServiceCallback() {
        @Override
        public void OnSuccess(QuerySnapshot querySnapshot) {
            List<String> boardNames = new ArrayList<>(); // boards that this pin belongs to
            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
            MutableLiveData<Map<String, BoardBooleanPair>> boardMapLiveData = viewModel.getBoardMap();
            Map<String, BoardBooleanPair> boardMap = boardMapLiveData.getValue();
            assert boardMap != null;

            if (!boardMap.isEmpty()) {
                for (Map.Entry<String, BoardBooleanPair> entry : boardMap.entrySet()) {
                    BoardBooleanPair pair = entry.getValue();
                    if (pair.isIncluded()) {
                        boardNames.add(pair.getBoard().getName());
                    }
                }
            } else {
                for (DocumentSnapshot document : documents) {
                    Board board = document.toObject(Board.class);
                    if (board == null) {
                        continue;
                    }
                    board.setId(document.getId());

                    if (board.getPins() != null && board.getPins().contains(pin.getId())) {
                        boardNames.add(board.getName());
                        boardMap.put(board.getId(), new BoardBooleanPair().setBoard(board).setIncluded(true));
                    } else {
                        boardMap.put(board.getId(), new BoardBooleanPair().setBoard(board).setIncluded(false));
                    }
                }
            }

            if (!boardNames.isEmpty()) {
                Collections.sort(boardNames);
                handler.post(() -> binding.tvBoardName.setText(String.join(", ", boardNames)));
            }
        }

        @Override
        public void OnFailure(Exception e) {
            Log.e("EditPinFragment", "Failed to fetch boards");
            if (e.getMessage() != null) {
                Log.e("EditPinFragment", e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
    };

    void restoreStates() {
        Pin oldPinState = viewModel.getPinState();
        if (oldPinState != null) {
            pin = oldPinState;
        }
    }

    void navigateToPickBoards() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_edit_pin);
        Bundle args = new Bundle();
        args.putParcelable("pin", pin);
        navController.navigate(R.id.action_editPinFragment_to_editPinPickBoardsFragment, args);
    }

    @Override
    public void onPause() {
        super.onPause();
        updatePinFromInput();

        viewModel.setPinState(pin);
        if (mainExoPlayer != null) {
            mainExoPlayer.stop();
            mainExoPlayer.release();
            mainExoPlayer = null;
        }
        if (boardExoPlayer != null) {
            boardExoPlayer.release();
            boardExoPlayer = null;
        }
    }

    private void updatePinFromInput() {
        if (binding.etPinTitle.getText() != null) {
            pin.setName(binding.etPinTitle.getText().toString());
        }
        if (binding.etPinDescription.getText() != null) {
            pin.setDescription(binding.etPinDescription.getText().toString());
        }
        pin.setAllowComment(binding.switchAllowComments.isChecked());
    }

    void updatePin() {
        updatePinFromInput();
        MutableLiveData<Map<String, BoardBooleanPair>> boardMapLiveData = viewModel.getBoardMap();
        Map<String, BoardBooleanPair> boardMap = boardMapLiveData.getValue();
        assert boardMap != null;
        FirebasePinService.updatePinWithBoards(pin, boardMap, (updatePinSuccess, updateBoardSuccess) -> {
            if (updatePinSuccess && updateBoardSuccess) {
                Intent data = new Intent();
                data.putExtra("pin", pin);
                requireActivity().setResult(Activity.RESULT_OK, data);
                requireActivity().finish();
                return;
            }

            binding.btnSave.setEnabled(true);

            if (!updateBoardSuccess) {
                Toast.makeText(requireContext(), getResources().getString(R.string.update_pin_values_failure), Toast.LENGTH_SHORT).show();
            }
            if (!updateBoardSuccess) {
                Toast.makeText(requireContext(), getResources().getString(R.string.update_pin_boards_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void deletePin() {
        FirebasePinService.deletePin(pin.getId(), new FirebasePinService.DeletePinCallback() {
            @Override
            public void OnSuccess() {
                Intent data = new Intent();
                data.putExtra("delete", true);
                requireActivity().setResult(Activity.RESULT_OK, data);
                requireActivity().finish();
            }

            @Override
            public void OnFailure(Exception e) {
                Toast.makeText(requireContext(), getResources().getString(R.string.update_pin_boards_failure), Toast.LENGTH_SHORT).show();
                if (e.getMessage() != null) {
                    Log.e("EditPinFragment", e.getMessage());
                } else {
                    e.printStackTrace();
                }
                binding.btnSave.setEnabled(true);
            }
        });
    }

    void showUnknownErrorToast() {
        Toast.makeText(requireContext(), getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
    }
}