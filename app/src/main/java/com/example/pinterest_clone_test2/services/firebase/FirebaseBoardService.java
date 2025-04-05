package com.example.pinterest_clone_test2.services.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public abstract class FirebaseBoardService {
    public static void getUserBoards(GetBoardServiceCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("boards")
                .whereEqualTo("authorId", currentUser.getUid())
                .get()
                .addOnSuccessListener(callback::OnSuccess)
                .addOnFailureListener(callback::OnFailure);
    }

    public interface GetBoardServiceCallback {
        void OnSuccess(QuerySnapshot querySnapshot);

        void OnFailure(Exception e);
    }
}
