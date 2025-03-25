package com.example.pinterest_clone_test2.interfaces;

import androidx.annotation.NonNull;

public interface Downloader {
    long DownloadFile(@NonNull String url, @NonNull String mimeType, @NonNull String fileName);
}
