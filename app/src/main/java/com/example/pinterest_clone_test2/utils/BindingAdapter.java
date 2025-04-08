package com.example.pinterest_clone_test2.utils;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BindingAdapter {
    @androidx.databinding.BindingAdapter("reactionIcon")
    public static void setReactionIcon(TextView textView, int resourceId) {
        if (resourceId != 0) {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(textView.getContext(), resourceId),
                    null, null, null
            );
        }
    }

    @androidx.databinding.BindingAdapter("createdAt")
    public static void setCreatedAt(TextView textView, Long createdAt) {
        if (createdAt != null && createdAt > 0) {
            textView.setText(Instant.ofEpochMilli(createdAt)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            textView.setText("");
        }
    }
}
