package com.example.pinterest_clone_test2.services.firebase;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FirebaseBoardService {
    private static QuerySnapshot currentUserBoardSnapshot;

    public static QuerySnapshot getCurrentUserBoardSnapshot() {
        return currentUserBoardSnapshot;
    }

    private static boolean currentUserBoardListUpdated = false;

    public static boolean isCurrentUserBoardListUpdated() {
        return currentUserBoardListUpdated;
    }

    private static DocumentSnapshot currentUserBoard;
    public static DocumentSnapshot getCurrentUserBoardDocument() { return currentUserBoard; }

    public static void getUserBoards(GetBoardServiceCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (!userSnapshot.exists()) {
                        callback.OnFailure(new Exception("User document does not exist"));
                        return;
                    }

                    List<String> boardIds = (List<String>) userSnapshot.get("boards");
                    if (boardIds == null || boardIds.isEmpty()) {
                        firestore.collection("boards")
                                .whereEqualTo("name", currentUser.getUid())
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    callback.OnSuccess(querySnapshot);
                                    currentUserBoardSnapshot = querySnapshot;
                                    currentUserBoardListUpdated = false;
                                }).addOnFailureListener(callback::OnFailure);
                        return;
                    }
                    firestore.collection("boards")
                            .whereIn(FieldPath.documentId(), boardIds)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                callback.OnSuccess(querySnapshot);
                                currentUserBoardSnapshot = querySnapshot;
                                currentUserBoardListUpdated = false;
                            })
                            .addOnFailureListener(callback::OnFailure);
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void addCurrentUserToCollaborators(String boardId, AddCollaboratorServiceCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("boards")
                .document(boardId)
                .update("collaborators", FieldValue.arrayUnion(currentUser.getUid()))
                .addOnSuccessListener(unused -> {
                    FirebaseUserService.updateUserBoards(boardId, new FirebaseUserService.UpdateUserBoardsCallback() {
                        @Override
                        public void OnSuccess() {
                            callback.OnSuccess();
                        }

                        @Override
                        public void OnFailure(Exception e) {
                            callback.OnFailure(e);
                        }
                    });
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void updatePinOrder(String boardId, List<Pin> orderedPins, UpdatePinOrderCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Extract only pin IDs
        List<String> pinIds = new ArrayList<>();
        for (Pin pin : orderedPins) {
            pinIds.add(pin.getId());
        }

        firestore.collection("boards")
                .document(boardId)
                .update("pins", pinIds)
                .addOnSuccessListener(unused -> {
                    callback.OnSuccess();
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void deletePinsFromBoard(String boardId, List<String> pinIds, DeletePinsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference boardRef = firestore.collection("boards").document(boardId);

        List<Object> arrayRemovals = new ArrayList<>(pinIds);

        boardRef.update("pins", FieldValue.arrayRemove(arrayRemovals.toArray()))
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public static void getBoardByIdWithPins(String boardId, GetSingleBoardWithPinsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("boards").document(boardId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Board board = documentSnapshot.toObject(Board.class);
                        assert board != null;
                        board.setId(documentSnapshot.getId());

                        List<String> pinIds = board.getPins();
                        if (pinIds != null && !pinIds.isEmpty()) {
                            FirebasePinService.fetchPinsFromIds(pinIds, new FirebasePinService.OnPinsFetchedFromIdsCallback() {
                                @Override
                                public void onSuccess(List<Pin> pins) {
                                    board.setPinsObj(pins);
                                    currentUserBoard = documentSnapshot;
                                    callback.OnSuccess(board);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.OnFailure(e);
                                }
                            });
                        } else {
                            board.setPinsObj(new ArrayList<>());
                            currentUserBoard = documentSnapshot;
                            callback.OnSuccess(board);
                        }
                    } else {
                        callback.OnFailure(new Exception("Board not found"));
                    }
                })
                .addOnFailureListener(callback::OnFailure);
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
                        boardData.put("pins", new ArrayList<String>());
                    }
                    if (board.getCollaborators() != null && !board.getCollaborators().isEmpty()) {
                        boardData.put("collaborators", new ArrayList<>(board.getCollaborators())
                        );
                    } else {
                        boardData.put("collaborators", new ArrayList<String>());
                    }
                    boardData.put("createdAt", System.currentTimeMillis());

                    firestore.collection("boards")
                            .add(boardData)
                            .addOnSuccessListener(documentReference -> {
                                currentUserBoardListUpdated = true;
                                callback.OnSuccess(documentReference);
                            })
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

    public interface InviteLinkCallback {
        void OnSuccess(String inviteLink);

        void OnFailure(Exception e);
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

    public interface AddCollaboratorServiceCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface GetSingleBoardWithPinsCallback {
        void OnSuccess(Board board);

        void OnFailure(Exception e);
    }

    public interface DeletePinsCallback {
        void OnSuccess();
        void OnFailure(Exception e);
    }

    public interface UpdatePinOrderCallback {
        void OnSuccess();
        void OnFailure(Exception e);
    }
}
