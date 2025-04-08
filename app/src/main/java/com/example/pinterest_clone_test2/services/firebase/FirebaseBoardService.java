package com.example.pinterest_clone_test2.services.firebase;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class FirebaseBoardService {
    public static void getUserBoards(GetBoardServiceCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("boards")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(callback::OnSuccess)
                .addOnFailureListener(callback::OnFailure);
    }

    public static void fetchPinsFromIds(List<String> pinIds, OnPinsFetchedCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (pinIds == null || pinIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String pinId : pinIds) {
            tasks.add(firestore.collection("pins").document(pinId).get());
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    List<Pin> pins = new ArrayList<>();
                    for (Object result : results) {
                        if (result instanceof DocumentSnapshot) {
                            DocumentSnapshot doc = (DocumentSnapshot) result;
                            Pin pin = doc.toObject(Pin.class);
                            if (pin != null) {
                                pin.setId(doc.getId());
                                pins.add(pin);
                            }
                        }
                    }
                    callback.onSuccess(pins);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public interface OnPinsFetchedCallback {
        void onSuccess(List<Pin> pins);

        void onFailure(Exception e);
    }

    public static void createNewBoard(@NonNull Board board, CreateBoardServiceCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("boards")
                .whereEqualTo("name", board.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                    if (!documentSnapshots.isEmpty()) {
                        callback.OnFailure(new Exception("Board name already exists"));
                        return;
                    }

                    Map<String, Object> boardData = new HashMap<>();
                    boardData.put("name", board.getName());
                    boardData.put("userId", currentUser.getUid());
                    boardData.put("isPublic", board.isPublic());
                    if (board.getPins() != null && !board.getPins().isEmpty()) {
                        boardData.put("pins", board.getPins());
                    } else {
                        boardData.put("pins", new ArrayList<>());
                    }
                    if (board.getCollaborators() != null && !board.getCollaborators().isEmpty()) {
                        boardData.put("collaborators", board.getCollaborators()
                                .stream()
                                .map(User::getId)
                                .collect(Collectors.toList())
                        );
                    } else {
                        boardData.put("collaborators", new ArrayList<>());
                    }
                    boardData.put("createdAt", System.currentTimeMillis());

                    firestore.collection("boards")
                            .add(boardData)
                            .addOnSuccessListener(callback::OnSuccess)
                            .addOnFailureListener(callback::OnFailure);
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void savePinToBoard(@NonNull String pinId, @NonNull String boardId, SavePinToBoardServiceCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("boards")
                .document(boardId)
                .update("pins", FieldValue.arrayUnion(pinId))
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public interface GetBoardServiceCallback {
        void OnSuccess(QuerySnapshot querySnapshot);

        void OnFailure(Exception e);
    }

    public interface CreateBoardServiceCallback {
        void OnSuccess(DocumentReference documentReference);

        void OnFailure(Exception e);
    }

    public interface SavePinToBoardServiceCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }
}
