package com.example.pinterest_clone_test2.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.pinterest_clone_test2.R;

public class DownloadMediaBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getResources().getString(R.string.download_pin_finished), Toast.LENGTH_SHORT).show();
    }
}
