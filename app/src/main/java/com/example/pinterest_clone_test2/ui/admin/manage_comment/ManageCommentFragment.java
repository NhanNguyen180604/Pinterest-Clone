package com.example.pinterest_clone_test2.ui.admin.manage_comment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.ReportedCommentAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentManageCommentBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportedComment;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageCommentFragment extends Fragment implements ReportedCommentAdapter.OnReportedCommentListener {

    private FragmentManageCommentBinding binding;
    private ManageCommentViewModel viewModel;
    private ReportedCommentAdapter adapter;
    private List<ReportedComment> reportedComments = new ArrayList<>();

    // Danh sách lý do báo cáo cho filter
    private List<ReportReason> reportReasons;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Để hiển thị menu
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageCommentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManageCommentViewModel.class);

        // Lấy danh sách lý do báo cáo
        reportReasons = ReportReason.GetReasons(requireContext());

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập các điều khiển UI
        setupSearchBar();
        setupFilterButton();
        setupFilterChips();

        // Thiết lập các observers
        setupObservers();

        // Tải dữ liệu ban đầu
        viewModel.fetchReportedComments();
    }

    private void setupRecyclerView() {
        adapter = new ReportedCommentAdapter(requireContext(), reportedComments, this);
        binding.rvReportedComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReportedComments.setAdapter(adapter);
    }

    private void setupSearchBar() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setSearchQuery(s.toString());
            }
        });
    }

    private void setupFilterButton() {
        binding.btnFilter.setOnClickListener(v -> {
            if (binding.filterChipsContainer.getVisibility() == View.VISIBLE) {
                binding.filterChipsContainer.setVisibility(View.GONE);
            } else {
                binding.filterChipsContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupFilterChips() {
        // Status filter chip
        binding.chipFilterStatus.setOnClickListener(v -> showStatusFilterDialog());

        // Reason filter chip
        binding.chipFilterReason.setOnClickListener(v -> showReasonFilterDialog());

        // Date filter chip
        binding.chipFilterDate.setOnClickListener(v -> showDateFilterDialog());

        // Sort filter chip
        binding.chipFilterSort.setOnClickListener(v -> showSortDialog());

        // Reset filters chip
        binding.chipResetFilters.setOnClickListener(v -> {
            viewModel.resetFilters();
            updateFilterChipsText();
            Toast.makeText(requireContext(), R.string.filters_reset, Toast.LENGTH_SHORT).show();
        });

        // Initialize chips text
        updateFilterChipsText();
    }

    private void showStatusFilterDialog() {
        String[] options = {
                getString(R.string.status_all),
                getString(R.string.status_unchecked),
                getString(R.string.status_checked)
        };

        int currentSelection = viewModel.getCurrentFilterOption().ordinal();

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_by_status)
                .setSingleChoiceItems(options, currentSelection, (dialog, which) -> {
                    ManageCommentViewModel.FilterOption option;
                    switch (which) {
                        case 1:
                            option = ManageCommentViewModel.FilterOption.UNCHECKED;
                            break;
                        case 2:
                            option = ManageCommentViewModel.FilterOption.CHECKED;
                            break;
                        case 0:
                        default:
                            option = ManageCommentViewModel.FilterOption.ALL;
                    }
                    viewModel.setCurrentFilterOption(option);
                    updateFilterChipsText();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showReasonFilterDialog() {
        List<String> reasonTexts = new ArrayList<>();
        reasonTexts.add(getString(R.string.all_reasons));
        for (ReportReason reason : reportReasons) {
            reasonTexts.add(reason.getTitle());
        }

        String[] options = reasonTexts.toArray(new String[0]);

        // Determine current selection
        int selectedIndex = 0;
        String currentReasonId = viewModel.getFilterReasonId();
        if (currentReasonId != null) {
            for (int i = 0; i < reportReasons.size(); i++) {
                if (reportReasons.get(i).getId().equals(currentReasonId)) {
                    selectedIndex = i + 1; // +1 because first item is "All reasons"
                    break;
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_by_reason)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    if (which == 0) {
                        viewModel.setFilterReasonId(null);
                    } else {
                        viewModel.setFilterReasonId(reportReasons.get(which - 1).getId());
                    }
                    updateFilterChipsText();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDateFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_range_picker, null);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(dialogView);

        Chip chipToday = dialogView.findViewById(R.id.chip_today);
        Chip chipWeek = dialogView.findViewById(R.id.chip_last_week);
        Chip chipMonth = dialogView.findViewById(R.id.chip_last_month);
        Chip chipCustom = dialogView.findViewById(R.id.chip_custom);
        View customDateLayout = dialogView.findViewById(R.id.layout_custom_date);
        View btnStartDate = dialogView.findViewById(R.id.btn_select_start_date);
        View btnEndDate = dialogView.findViewById(R.id.btn_select_end_date);
        android.widget.TextView tvStartDate = dialogView.findViewById(R.id.tv_start_date);
        android.widget.TextView tvEndDate = dialogView.findViewById(R.id.tv_end_date);

        // Setup pre-defined date ranges
        chipToday.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            viewModel.setFilterStartDate(cal.getTime());

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            viewModel.setFilterEndDate(cal.getTime());

            updateFilterChipsText();
            dialog.dismiss();
        });

        chipWeek.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -7);
            viewModel.setFilterStartDate(cal.getTime());

            cal = Calendar.getInstance();
            viewModel.setFilterEndDate(cal.getTime());

            updateFilterChipsText();
            dialog.dismiss();
        });

        chipMonth.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            viewModel.setFilterStartDate(cal.getTime());

            cal = Calendar.getInstance();
            viewModel.setFilterEndDate(cal.getTime());

            updateFilterChipsText();
            dialog.dismiss();
        });

        // Setup custom date range
        chipCustom.setOnClickListener(v -> {
            customDateLayout.setVisibility(View.VISIBLE);
        });

        // Setup date pickers for custom range
        btnStartDate.setOnClickListener(v -> showDatePicker(date -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvStartDate.setText(sdf.format(date));
            viewModel.setFilterStartDate(date);
        }));

        btnEndDate.setOnClickListener(v -> showDatePicker(date -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvEndDate.setText(sdf.format(date));
            viewModel.setFilterEndDate(cal.getTime());
        }));

        // Apply button for custom range
        dialogView.findViewById(R.id.btn_apply_custom).setOnClickListener(v -> {
            updateFilterChipsText();
            dialog.dismiss();
        });

        // Reset date filter
        dialogView.findViewById(R.id.btn_clear_date).setOnClickListener(v -> {
            viewModel.setFilterStartDate(null);
            viewModel.setFilterEndDate(null);
            updateFilterChipsText();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePicker(DateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    listener.onDateSelected(selectedDate.getTime());
                },
                year, month, day);

        datePickerDialog.show();
    }

    interface DateSelectedListener {
        void onDateSelected(Date date);
    }

    private void showSortDialog() {
        String[] options = {
                getString(R.string.sort_by_newest),
                getString(R.string.sort_by_oldest),
                getString(R.string.sort_by_most_reports),
                getString(R.string.sort_by_least_reports)
        };

        int currentSelection;
        switch (viewModel.getCurrentSortOption()) {
            case LAST_REPORTED_DESC:
                currentSelection = 0;
                break;
            case LAST_REPORTED_ASC:
                currentSelection = 1;
                break;
            case REPORT_COUNT_DESC:
                currentSelection = 2;
                break;
            case REPORT_COUNT_ASC:
                currentSelection = 3;
                break;
            default:
                currentSelection = 0;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sort_by)
                .setSingleChoiceItems(options, currentSelection, (dialog, which) -> {
                    ManageCommentViewModel.SortOption option;
                    switch (which) {
                        case 0:
                            option = ManageCommentViewModel.SortOption.LAST_REPORTED_DESC;
                            break;
                        case 1:
                            option = ManageCommentViewModel.SortOption.LAST_REPORTED_ASC;
                            break;
                        case 2:
                            option = ManageCommentViewModel.SortOption.REPORT_COUNT_DESC;
                            break;
                        case 3:
                            option = ManageCommentViewModel.SortOption.REPORT_COUNT_ASC;
                            break;
                        default:
                            option = ManageCommentViewModel.SortOption.LAST_REPORTED_DESC;
                    }
                    viewModel.setCurrentSortOption(option);
                    updateFilterChipsText();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateFilterChipsText() {
        // Update status chip text
        switch (viewModel.getCurrentFilterOption()) {
            case ALL:
                binding.chipFilterStatus.setText(R.string.status_all);
                break;
            case CHECKED:
                binding.chipFilterStatus.setText(R.string.status_checked);
                break;
            case UNCHECKED:
                binding.chipFilterStatus.setText(R.string.status_unchecked);
                break;
        }

        // Update reason chip text
        String reasonId = viewModel.getFilterReasonId();
        if (reasonId == null) {
            binding.chipFilterReason.setText(R.string.all_reasons);
        } else {
            for (ReportReason reason : reportReasons) {
                if (reason.getId().equals(reasonId)) {
                    binding.chipFilterReason.setText(reason.getTitle());
                    break;
                }
            }
        }

        // Update date chip text
        Date startDate = viewModel.getFilterStartDate();
        Date endDate = viewModel.getFilterEndDate();
        if (startDate == null && endDate == null) {
            binding.chipFilterDate.setText(R.string.date_range);
        } else {
            binding.chipFilterDate.setText(R.string.date_filtered);
        }

        // Update sort chip text
        switch (viewModel.getCurrentSortOption()) {
            case LAST_REPORTED_DESC:
                binding.chipFilterSort.setText(R.string.sort_by_newest);
                break;
            case LAST_REPORTED_ASC:
                binding.chipFilterSort.setText(R.string.sort_by_oldest);
                break;
            case REPORT_COUNT_DESC:
                binding.chipFilterSort.setText(R.string.sort_by_most_reports);
                break;
            case REPORT_COUNT_ASC:
                binding.chipFilterSort.setText(R.string.sort_by_least_reports);
                break;
        }

        // Update active filters text
        updateActiveFiltersText();
    }

    private void updateActiveFiltersText() {
        StringBuilder activeFilters = new StringBuilder();

        // Add status filter if active
        if (viewModel.getCurrentFilterOption() != ManageCommentViewModel.FilterOption.ALL) {
            activeFilters.append(getString(R.string.status_label)).append(": ");
            switch (viewModel.getCurrentFilterOption()) {
                case CHECKED:
                    activeFilters.append(getString(R.string.status_checked));
                    break;
                case UNCHECKED:
                    activeFilters.append(getString(R.string.status_unchecked));
                    break;
            }
            activeFilters.append(" • ");
        }

        // Add reason filter if active
        String reasonId = viewModel.getFilterReasonId();
        if (reasonId != null) {
            activeFilters.append(getString(R.string.reason_label)).append(": ");
            for (ReportReason reason : reportReasons) {
                if (reason.getId().equals(reasonId)) {
                    activeFilters.append(reason.getTitle());
                    break;
                }
            }
            activeFilters.append(" • ");
        }

        // Add date filter if active
        Date startDate = viewModel.getFilterStartDate();
        Date endDate = viewModel.getFilterEndDate();
        if (startDate != null || endDate != null) {
            activeFilters.append(getString(R.string.date_label)).append(": ");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            if (startDate != null && endDate != null) {
                activeFilters.append(sdf.format(startDate)).append(" - ").append(sdf.format(endDate));
            } else if (startDate != null) {
                activeFilters.append(getString(R.string.from)).append(" ").append(sdf.format(startDate));
            } else {
                activeFilters.append(getString(R.string.until)).append(" ").append(sdf.format(endDate));
            }
            activeFilters.append(" • ");
        }

        // Add sort info
        activeFilters.append(getString(R.string.sort_label)).append(": ");
        switch (viewModel.getCurrentSortOption()) {
            case LAST_REPORTED_DESC:
                activeFilters.append(getString(R.string.sort_by_newest));
                break;
            case LAST_REPORTED_ASC:
                activeFilters.append(getString(R.string.sort_by_oldest));
                break;
            case REPORT_COUNT_DESC:
                activeFilters.append(getString(R.string.sort_by_most_reports));
                break;
            case REPORT_COUNT_ASC:
                activeFilters.append(getString(R.string.sort_by_least_reports));
                break;
        }

        // Show or hide active filters text
        if (activeFilters.length() > 0) {
            binding.tvActiveFilters.setText(activeFilters.toString());
            binding.tvActiveFilters.setVisibility(View.VISIBLE);
        } else {
            binding.tvActiveFilters.setVisibility(View.GONE);
        }
    }

    private void setupObservers() {
        // Observe danh sách comment bị báo cáo
        viewModel.getReportedComments().observe(getViewLifecycleOwner(), comments -> {
            reportedComments.clear();
            if (comments != null) {
                reportedComments.addAll(comments);
            }
            adapter.updateData(reportedComments);

            // Hiển thị empty view nếu không có dữ liệu
            if (reportedComments.isEmpty()) {
                binding.tvNoData.setVisibility(View.VISIBLE);
                binding.rvReportedComments.setVisibility(View.GONE);
            } else {
                binding.tvNoData.setVisibility(View.GONE);
                binding.rvReportedComments.setVisibility(View.VISIBLE);
            }
        });

        // Observe trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe thông báo lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentClick(ReportedComment comment) {
        // Hiển thị dialog chi tiết báo cáo
        showReportDetailDialog(comment);
    }

    @Override
    public void onAuthorClick(ReportedComment comment) {
        // Chuyển đến màn hình profile người dùng
        navigateToUserProfile(comment.getCommentAuthorId());
    }

    @Override
    public void onProcessClick(ReportedComment comment) {
        // Hiển thị dialog xử lý báo cáo
        showProcessReportDialog(comment);
    }

    @Override
    public void onViewPinClick(ReportedComment comment) {
        // Chuyển đến màn hình pin chi tiết
        navigateToPinDetail(comment.getPinId());
    }

    private void showReportDetailDialog(ReportedComment comment) {
        // Tạo thông tin chi tiết báo cáo
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        StringBuilder details = new StringBuilder();
        details.append(getString(R.string.content_label)).append(": ").append(comment.getCommentContent()).append("\n\n");
        details.append(getString(R.string.author_label)).append(": ").append(comment.getCommentAuthorName()).append("\n");
        details.append(getString(R.string.comment_time_label)).append(": ").append(sdf.format(new Date(comment.getCommentCreatedAt()))).append("\n");
        details.append(getString(R.string.reports_count_label)).append(": ").append(comment.getReportCount()).append("\n");
        details.append(getString(R.string.last_report_time_label)).append(": ").append(sdf.format(new Date(comment.getLastReportedAt()))).append("\n\n");

        details.append(getString(R.string.report_reasons_label)).append(":\n");
        Map<String, Integer> reasonsCount = comment.getReasonsCount();
        for (ReportReason reason : reportReasons) {
            Integer count = reasonsCount.get(reason.getId());
            if (count != null && count > 0) {
                details.append("- ").append(reason.getTitle()).append(": ").append(count).append(" ").append(getString(R.string.reports)).append("\n");
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.report_details)
                .setMessage(details.toString())
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void showProcessReportDialog(ReportedComment comment) {
        String[] options = {
                getString(R.string.mark_as_checked),
                getString(R.string.delete_comment)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.process_report)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Đánh dấu đã kiểm tra
                            showMarkAsCheckedDialog(comment);
                            break;
                        case 1:
                            // Xóa bình luận
                            showDeleteConfirmDialog(comment);
                            break;
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showMarkAsCheckedDialog(ReportedComment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm)
                .setMessage(R.string.mark_as_checked_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    viewModel.markReportAsChecked(comment.getReportId(), success -> {
                        if (success) {
                            Toast.makeText(requireContext(), R.string.marked_as_checked, Toast.LENGTH_SHORT).show();
                            // Gửi thông báo cho người dùng
                            viewModel.sendNotifications(comment, false, sent -> {
                                if (sent) {
                                    Toast.makeText(requireContext(), R.string.notification_sent, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteConfirmDialog(ReportedComment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_comment_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteComment(comment.getCommentId(), success -> {
                        if (success) {
                            Toast.makeText(requireContext(), R.string.comment_deleted, Toast.LENGTH_SHORT).show();
                            // Gửi thông báo cho người dùng
                            viewModel.sendNotifications(comment, true, sent -> {
                                if (sent) {
                                    Toast.makeText(requireContext(), R.string.notification_sent, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), R.string.error_deleting_comment, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void navigateToPinDetail(String pinId) {
        if (pinId == null || pinId.isEmpty()) {
            Toast.makeText(requireContext(), R.string.pin_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebasePinService.fetchPinsFromIds(List.of(pinId), new FirebasePinService.OnPinsFetchedFromIdsCallback() {
            @Override
            public void onSuccess(List<Pin> pins) {
                if (pins.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.pin_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Bundle để truyền dữ liệu
                Bundle args = new Bundle();
                args.putParcelableArrayList("pins", new ArrayList<>(pins));
                args.putString("source", "admin"); // Mark source as admin
                args.putInt("initial_position", 0);
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin);
                navController.navigate(R.id.action_manageCommentFragment_to_pinFragment4, args);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), R.string.error_loading_post, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToUserProfile(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), R.string.user_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        // Bundle để truyền dữ liệu
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("source", "admin"); // Mark source as admin

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin);
        navController.navigate(R.id.action_manageCommentFragment_to_userProfileFragment, args);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.comment_management_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            // Làm mới dữ liệu
            viewModel.fetchReportedComments();
            return true;
        } else if (id == R.id.action_statistics) {
            // Hiển thị thống kê
            showStatisticsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showStatisticsDialog() {
        // Đếm số báo cáo theo lý do
        Map<String, Integer> reasonStats = new HashMap<>();
        for (ReportReason reason : reportReasons) {
            reasonStats.put(reason.getTitle(), 0);
        }

        // Đếm số lượng báo cáo đã xử lý và chưa xử lý
        int checkedCount = 0;
        int uncheckedCount = 0;

        for (ReportedComment comment : reportedComments) {
            // Đếm trạng thái
            if (comment.isChecked()) {
                checkedCount++;
            } else {
                uncheckedCount++;
            }

            // Đếm theo lý do
            Map<String, Integer> commentReasons = comment.getReasonsCount();
            for (ReportReason reason : reportReasons) {
                Integer count = commentReasons.get(reason.getId());
                if (count != null && count > 0) {
                    Integer current = reasonStats.get(reason.getTitle());
                    reasonStats.put(reason.getTitle(), current + 1);
                }
            }
        }

        // Tạo nội dung dialog
        StringBuilder stats = new StringBuilder();
        stats.append(getString(R.string.total_reports)).append(": ").append(reportedComments.size()).append("\n\n");
        stats.append(getString(R.string.status_label)).append(":\n");
        stats.append("- ").append(getString(R.string.processed)).append(": ").append(checkedCount).append("\n");
        stats.append("- ").append(getString(R.string.unprocessed)).append(": ").append(uncheckedCount).append("\n\n");

        stats.append(getString(R.string.report_reasons_label)).append(":\n");
        for (Map.Entry<String, Integer> entry : reasonStats.entrySet()) {
            if (entry.getValue() > 0) {
                stats.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.report_statistics)
                .setMessage(stats.toString())
                .setPositiveButton(R.string.close, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}