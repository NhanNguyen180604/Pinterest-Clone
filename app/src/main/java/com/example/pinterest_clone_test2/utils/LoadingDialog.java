package com.example.pinterest_clone_test2.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.pinterest_clone_test2.R;

public class LoadingDialog {
    private Dialog dialog;
    private TextView tvMessage;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }

        tvMessage = dialog.findViewById(R.id.tv_loading_message);
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setMessage(String message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}