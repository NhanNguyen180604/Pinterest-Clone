package com.example.pinterest_clone_test2.utils;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class BindingAdapter {
    // idk what this is, may be it's similar to Data-Converter in WinUI3
    @androidx.databinding.BindingAdapter("reactionIcon")
    public static void setReactionIcon(TextView textView, int resourceId) {
        if (resourceId != 0) {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(textView.getContext(), resourceId),
                    null, null, null
            );
        }
    }
}
