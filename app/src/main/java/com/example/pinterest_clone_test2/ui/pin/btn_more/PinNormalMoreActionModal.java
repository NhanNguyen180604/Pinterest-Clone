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
import com.example.pinterest_clone_test2.databinding.PinMoreActionModalBottomSheetBinding;
import com.example.pinterest_clone_test2.interfaces.ReportModalCallbacks;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.PinReport;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.ui.pin.PinObjectFragment;
import com.example.pinterest_clone_test2.ui.report.ReportModalBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PinNormalMoreActionModal extends BottomSheetDialogFragment {
    PinMoreActionModalBottomSheetBinding binding;
    public static String TAG = "PinMoreActionModalBottomSheet";
    Pin pin;
    boolean savedToBoard;
    PinObjectFragment.DownloadPinMediaCallback downloadCallback;
    PinObjectFragment.HidePinCallback hidePinCallback;
    Context context;
    ActivityResultLauncher<Intent> editPinActivityLauncher;

    public PinNormalMoreActionModal(Pin pin, Context context, boolean savedToBoard, ActivityResultLauncher<Intent> editPinActivityLauncher, PinObjectFragment.DownloadPinMediaCallback downloadCallback, PinObjectFragment.HidePinCallback hidePinCallback) {
        this.pin = pin;
        this.context = context;
        this.savedToBoard = savedToBoard;
        this.downloadCallback = downloadCallback;
        this.hidePinCallback = hidePinCallback;
        this.editPinActivityLauncher = editPinActivityLauncher;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PinMoreActionModalBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!savedToBoard) {
            binding.tvClickableHide.setOnClickListener(v -> {
                hidePinCallback.Hide();
                dismiss();
            });
            binding.tvClickableEdit.setVisibility(View.GONE);
            binding.tvClickableSend.setVisibility(View.GONE);
            binding.tvClickableCopyLink.setVisibility(View.GONE);
        } else {
            binding.tvClickableHide.setVisibility(View.GONE);
            binding.tvClickableEdit.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), EditPinActivity.class);
                intent.putExtra("pin", pin);
                editPinActivityLauncher.launch(intent);
                dismiss();
            });
            binding.tvClickableSend.setOnClickListener(v -> dismiss());
            binding.tvClickableCopyLink.setOnClickListener(v -> {
                // could crash the app, FUCK IT THEN
//            String sharedLink = String.format(Locale.US, context.getResources().getString(R.string.pin_deep_link_string_template), pin.getId());
//            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText(sharedLink, sharedLink);
//            clipboard.setPrimaryClip(clip);
            dismiss();
            Toast.makeText(context, context.getResources().getString(R.string.copied), Toast.LENGTH_SHORT).show();
            });
        }

        binding.tvClickableDownload.setOnClickListener(v -> {
            downloadCallback.Download();
            dismiss();
        });

        binding.tvClickableAddToCollage.setOnClickListener(v -> {
            //TODO: open collage here, pass in the pin
            Toast.makeText(requireContext(), "Open collage UI and pass this pin", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        binding.tvClickableReport.setOnClickListener(v -> {
            ReportModalBottomSheet sheet = new ReportModalBottomSheet(context, reportModalCallbacks);
            sheet.show(requireActivity().getSupportFragmentManager(), ReportModalBottomSheet.TAG);
            dismiss();
        });

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    ReportModalCallbacks reportModalCallbacks = new ReportModalCallbacks() {
        @Override
        public void CreateReport(@NonNull List<ReportReason> reasons) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            assert currentUser != null;

            PinReport report = new PinReport(reasons, currentUser.getUid(), pin.getId(), context);
            report.sendReportToDatabase();
        }
    };
}
