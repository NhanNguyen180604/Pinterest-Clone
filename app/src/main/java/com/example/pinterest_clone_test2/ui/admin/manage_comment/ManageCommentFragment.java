package com.example.pinterest_clone_test2.ui.admin.manage_comment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pinterest_clone_test2.adapters.ManageCommentListAdapter;
import com.example.pinterest_clone_test2.daos.MockManageCommentDao;
import com.example.pinterest_clone_test2.databinding.FragmentManageCommentBinding;
import com.example.pinterest_clone_test2.models.Comment;

import java.util.List;

public class ManageCommentFragment extends Fragment {
    FragmentManageCommentBinding binding;
    List<Comment> Comments;
    ManageCommentListAdapter commentAdapter;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageCommentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = binding.rvCommentsList;
        recyclerView.setLayoutManager((new LinearLayoutManager(requireContext())));
        Comments = new MockManageCommentDao().getComments();
        commentAdapter = new ManageCommentListAdapter(Comments);
        recyclerView.setAdapter(commentAdapter);
    }
}