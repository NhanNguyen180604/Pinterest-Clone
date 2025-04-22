package com.example.pinterest_clone_test2.ui.pin.btn_more;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.EditPinActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.PinAuthorMoreActionModalBottomSheetBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.pin.PinObjectFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PinAuthorMoreActionModal extends BottomSheetDialogFragment {
    PinAuthorMoreActionModalBottomSheetBinding binding;
    public static String TAG = "PinAuthorMoreActionModalBottomSheet";

    Pin pin;
    PinObjectFragment.DownloadPinMediaCallback downloadCallback;
    Context context;
    ActivityResultLauncher<Intent> editPinActivityLauncher;

    public PinAuthorMoreActionModal(Pin pin, Context context, PinObjectFragment.DownloadPinMediaCallback downloadCallback, ActivityResultLauncher<Intent> editPinActivityLauncher) {
        this.pin = pin;
        this.context = context;
        this.downloadCallback = downloadCallback;
        this.editPinActivityLauncher = editPinActivityLauncher;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PinAuthorMoreActionModalBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvClickableEdit.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EditPinActivity.class);
            intent.putExtra("pin", pin);
            editPinActivityLauncher.launch(intent);
            dismiss();
        });
        binding.tvClickableDownload.setOnClickListener(v -> {
            downloadCallback.Download();
            dismiss();
        });

        binding.tvClickableAddToCollage.setOnClickListener(v -> {
            //TODO: open collage here, pass in the pin
            Toast.makeText(requireContext(), "Open collage UI and pass this pin", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        binding.tvClickableCopyLink.setOnClickListener(v -> {
            // could crash the app, FUCK IT THEN
//            String sharedLink = String.format(Locale.US, context.getResources().getString(R.string.pin_deep_link_string_template), pin.getId());
//            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText(sharedLink, sharedLink);
//            clipboard.setPrimaryClip(clip);
            dismiss();
            Toast.makeText(context, context.getResources().getString(R.string.copied), Toast.LENGTH_SHORT).show();
        });

        binding.btnClose.setOnClickListener(v -> dismiss());
    }
}
