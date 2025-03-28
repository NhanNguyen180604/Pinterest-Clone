package com.example.pinterest_clone_test2.ui.pin;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.interfaces.Downloader;

public class PinMediaDownloader implements Downloader {
    private final DownloadManager downloadManager;

    public PinMediaDownloader(@NonNull Context context) {
        downloadManager = context.getSystemService(DownloadManager.class);
        assert downloadManager != null;
    }

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
