package com.example.pinterest_clone_test2.services.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.interfaces.Downloader;

public class PinMediaDownloader implements Downloader {
    private final DownloadManager downloadManager;
    public static String ACTION_PIN_DOWNLOAD_COMPLETE = "my.shit.action.PIN_DOWNLOAD_COMPLETE";

    public PinMediaDownloader(@NonNull Context context) {
        downloadManager = context.getSystemService(DownloadManager.class);
        assert downloadManager != null;
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadFinishBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(downloadFinishBroadcastReceiver, filter);
        }
    }

    final BroadcastReceiver downloadFinishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent finishIntent = new Intent(ACTION_PIN_DOWNLOAD_COMPLETE);
            context.sendBroadcast(finishIntent);
        }
    };

    @Override
    public long DownloadFile(@NonNull String url, @NonNull String mimeType, @NonNull String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setTitle("Downloading" + fileName)
                .setMimeType(mimeType)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        return downloadManager.enqueue(request);
    }
}
