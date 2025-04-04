package com.example.pinterest_clone_test2.services.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public abstract class FirebasePinService {
    public static void getPins(DocumentSnapshot lastVisible, int perPage, Filter filter, GetPinServiceCallback callback) {
        if (perPage < 1) {
            throw new IllegalArgumentException("Per page number must be greater than 0");
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Query query = firestore.collection("pins");

        if (filter != null) {
            query = query.where(filter);
        }

        query = query.orderBy("createdAt", Query.Direction.DESCENDING);

        // this is for pagination, use lastVisible to avoid infinite scrolling
        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.limit(perPage)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        callback.OnSuccess(queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.OnFailure(e);
                    }
                });
    }

    public interface GetPinServiceCallback {
        void OnSuccess(QuerySnapshot queryDocumentSnapshots);

        void OnFailure(Exception e);
    }
}
