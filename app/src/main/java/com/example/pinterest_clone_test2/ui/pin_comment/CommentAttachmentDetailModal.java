package com.example.pinterest_clone_test2.ui.pin_comment;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.ImageDetailViewBinding;
import com.example.pinterest_clone_test2.models.Comment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CommentAttachmentDetailModal extends BottomSheetDialogFragment {
    ImageDetailViewBinding binding;
    public static String TAG = "CommentAttachmentModal";
    Comment _comment;

    public CommentAttachmentDetailModal(Comment comment) {
        _comment = comment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ImageDetailViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        assert view != null;

        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        ViewGroup.LayoutParams params = binding.imageDetailLayoutContainer.getLayoutParams();
        params.height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.9);
        binding.imageDetailLayoutContainer.setLayoutParams(params);

        binding.btnClose.setOnClickListener(v -> dismiss());

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.karyl)
                .error(R.drawable.turtle_huh);

        if (_comment.getAttachmentUrl() != null) {
            Glide.with(binding.ivImage.getContext())
                    .load(_comment.getAttachmentUrl())
                    .fitCenter()
                    .apply(options)
                    .into(binding.ivImage);
        } else {
            Glide.with(binding.ivImage.getContext())
                    .load(_comment.getAttachmentUri())
                    .fitCenter()
                    .apply(options)
                    .into(binding.ivImage);
        }
    }
}
