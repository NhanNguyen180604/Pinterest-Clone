package com.example.pinterest_clone_test2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinterest_clone_test2.databinding.ReportReasonViewHolderBinding;
import com.example.pinterest_clone_test2.models.ReportReason;

import java.util.List;

public class ReportReasonAdapter extends RecyclerView.Adapter<ReportReasonAdapter.ReportReasonViewHolder> {
    public static class ReportReasonViewHolder extends RecyclerView.ViewHolder {
        ReportReasonViewHolderBinding _binding;

        ReportReasonViewHolder(ReportReasonViewHolderBinding binding) {
            super(binding.getRoot());
            _binding = binding;
        }

        // I'm sorry
        void setReason(ReportReason reason, List<Boolean> checkedList, int position) {
            _binding.setReportReason(reason);
            _binding.reportReasonLayoutContainer.setOnClickListener(v -> {
                onClickListener.OnClick(reason, checkedList, position);
            });
            _binding.tvReportTitle.setOnClickListener(v -> {
                onClickListener.OnClick(reason, checkedList, position);
            });
            _binding.tvReportDescription.setOnClickListener(v -> {
                onClickListener.OnClick(reason, checkedList, position);
            });
            _binding.cbReason.setOnClickListener(v -> {
                onClickListener.OnClick(reason, checkedList, position);
            });
        }

        interface ReasonOnClickListener {
            void OnClick(ReportReason reason, List<Boolean> checkedList, int position);
        }

        ReasonOnClickListener onClickListener = new ReasonOnClickListener() {
            @Override
            public void OnClick(ReportReason reason, List<Boolean> checkedList, int position) {
                boolean isChecked = checkedList.get(position);
                checkedList.set(position, !isChecked);
                _binding.cbReason.setChecked(!isChecked);
            }
        };
    }

    private final List<ReportReason> _reportReasons;
    private final List<Boolean> _checkedList;

    public ReportReasonAdapter(List<ReportReason> reportReasons, List<Boolean> checkList) {
        assert reportReasons != null;
        assert !reportReasons.isEmpty();
        _reportReasons = reportReasons;
        _checkedList = checkList;
    }

    @NonNull
    @Override
    public ReportReasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ReportReasonViewHolder(ReportReasonViewHolderBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportReasonViewHolder holder, int position) {
        holder.setReason(_reportReasons.get(position), _checkedList, position);
    }

    @Override
    public int getItemCount() {
        if (_reportReasons != null)
            return _reportReasons.size();
        return 0;
    }
}
