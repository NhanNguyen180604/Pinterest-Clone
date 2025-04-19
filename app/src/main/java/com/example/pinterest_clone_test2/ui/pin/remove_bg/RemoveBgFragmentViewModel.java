package com.example.pinterest_clone_test2.ui.pin.remove_bg;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class RemoveBgFragmentViewModel extends ViewModel {
    SavedStateHandle savedStateHandle;
    public static String ORIGINAL_IMAGE_URL = "original_image_url";

    public RemoveBgFragmentViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;
    }

    public void setOriginalImageUrl(String originalImageUrl) {
        savedStateHandle.set(ORIGINAL_IMAGE_URL, originalImageUrl);
    }

    public String getOriginalImageUrl() {
        return savedStateHandle.get(ORIGINAL_IMAGE_URL);
    }
}
