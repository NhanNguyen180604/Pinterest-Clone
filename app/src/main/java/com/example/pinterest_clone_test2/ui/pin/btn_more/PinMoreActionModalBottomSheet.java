package com.example.pinterest_clone_test2.ui.pin.btn_more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.databinding.PinMoreActionModalBottomSheetBinding;
import com.example.pinterest_clone_test2.interfaces.ReportModalCallbacks;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.PinReport;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.ui.pin.PinObjectFragment;
import com.example.pinterest_clone_test2.ui.report.ReportModalBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class PinMoreActionModalBottomSheet extends BottomSheetDialogFragment {
    PinMoreActionModalBottomSheetBinding binding;
    public static String TAG = "PinMoreActionModalBottomSheet";
    Pin pin;
    PinObjectFragment.DownloadPinMediaCallback downloadCallback;

    public PinMoreActionModalBottomSheet(Pin pin, PinObjectFragment.DownloadPinMediaCallback downloadCallback) {
        this.pin = pin;
        this.downloadCallback = downloadCallback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PinMoreActionModalBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvClickableHide.setOnClickListener(v -> {
            //TODO: send hide request to database
            Toast.makeText(requireContext(), "Send hide request to database", Toast.LENGTH_SHORT).show();
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

        binding.tvClickableReport.setOnClickListener(v -> {
            ReportModalBottomSheet sheet = new ReportModalBottomSheet(reportModalCallbacks);
            sheet.show(requireActivity().getSupportFragmentManager(), ReportModalBottomSheet.TAG);
            dismiss();
        });

        binding.btnClose.setOnClickListener(v -> {
            dismiss();
        });
    }

    ReportModalCallbacks reportModalCallbacks = new ReportModalCallbacks() {
        @Override
        public void CreateReport(@NonNull List<ReportReason> reasons) {
            PinReport report = new PinReport(reasons, "default-user-id", pin.getId());
            report.sendReportToDatabase();
        }
    };
}
