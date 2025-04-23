package com.example.pinterest_clone_test2.services.firebase;

import static androidx.core.content.ContextCompat.getString;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class FirebaseBoardService {
    private static QuerySnapshot currentUserBoardSnapshot;

    public static QuerySnapshot getCurrentUserBoardSnapshot() {
        return currentUserBoardSnapshot;
    }

    private static boolean currentUserBoardListUpdated = false;

    public static boolean isCurrentUserBoardListUpdated() {
        return currentUserBoardListUpdated;
    }

    public static void getUserBoards(GetBoardServiceCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("boards")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.OnSuccess(querySnapshot);
                    currentUserBoardSnapshot = querySnapshot;
                    currentUserBoardListUpdated = false;
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void getUserBoardsByUserId(String userId, boolean publicOnly, GetUserBoardsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Tạo truy vấn cơ bản
        Query query = firestore.collection("boards")
                .whereEqualTo("userId", userId);

        if (publicOnly) {
            query = query.whereEqualTo("isPublic", true);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Board> boards = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Board board = doc.toObject(Board.class);
                        if (board != null) {
                            board.setId(doc.getId());
                            boards.add(board);
                        }
                    }
                    callback.OnSuccess(boards);
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void createAllPinsBoard(Context context, String userId, CreateAllPinsBoardCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Lấy thông tin người dùng để lấy danh sách pins
        firestore.collection("users")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    if (documents.isEmpty()) {
                        callback.OnFailure(new Exception("User not found"));
                        return;
                    }

                    DocumentSnapshot userDoc = documents.get(0);
                    List<String> userPinIds = (List<String>) userDoc.get("pins");

                    if (userPinIds == null || userPinIds.isEmpty()) {
                        callback.OnEmpty();
                        return;
                    }

                    // Tạo board "Tất cả Ghim"
                    Board allPinsBoard = new Board()
                            .setId("all_pins_" + userId)
                            .setName(getString(context, R.string.all) + " " + getString(context, R.string.pins))
                            .setDescription(getString(context, R.string.all_saved_pins))
                            .setAuthorId(userId)
                            .setPublic(true)
                            .setPins(userPinIds);

                    // Tải pins cho board này
                    FirebasePinService.fetchPinsFromIds(userPinIds, new FirebasePinService.OnPinsFetchedFromIdsCallback() {
                        @Override
                        public void onSuccess(List<Pin> pins) {
                            allPinsBoard.setPinsObj(pins);
                            callback.OnSuccess(allPinsBoard);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Vẫn trả về board nếu không tải được pins
                            allPinsBoard.setPinsObj(new ArrayList<>());
                            callback.OnSuccess(allPinsBoard);
                        }
                    });
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void fetchPinsForBoard(Board board, FetchPinsForBoardCallback callback) {
        if (board.getPins() != null && !board.getPins().isEmpty()) {
            FirebasePinService.fetchPinsFromIds(board.getPins(), new FirebasePinService.OnPinsFetchedFromIdsCallback() {
                @Override
                public void onSuccess(List<Pin> pins) {
                    board.setPinsObj(pins);
                    callback.OnSuccess(board);
                }

                @Override
                public void onFailure(Exception e) {
                    board.setPinsObj(new ArrayList<>());
                    callback.OnFailure(e);
                }
            });
        } else {
            board.setPinsObj(new ArrayList<>());
            callback.OnSuccess(board);
        }
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
                        boardData.put("collaborators", board.getCollaborators()
                                .stream()
                                .map(User::getUserId)
                                .collect(Collectors.toList())
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

    public interface GetUserBoardsCallback {
        void OnSuccess(List<Board> boards);

        void OnFailure(Exception e);
    }

    public interface CreateAllPinsBoardCallback {
        void OnSuccess(Board allPinsBoard);

        void OnEmpty();

        void OnFailure(Exception e);
    }

    public interface FetchPinsForBoardCallback {
        void OnSuccess(Board board);

        void OnFailure(Exception e);
    }
}
