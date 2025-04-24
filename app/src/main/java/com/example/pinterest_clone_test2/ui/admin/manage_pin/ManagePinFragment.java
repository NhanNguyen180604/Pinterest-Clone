package com.example.pinterest_clone_test2.ui.admin.manage_pin;

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
import com.example.pinterest_clone_test2.adapters.ReportedPinAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentManagePinBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportedPin;
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

public class ManagePinFragment extends Fragment implements ReportedPinAdapter.OnReportedPinListener {

    private FragmentManagePinBinding binding;
    private ManagePinViewModel viewModel;
    private ReportedPinAdapter adapter;
    private final List<ReportedPin> reportedPins = new ArrayList<>();

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
        binding = FragmentManagePinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManagePinViewModel.class);

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
        viewModel.fetchReportedPins();
    }

    private void setupRecyclerView() {
        adapter = new ReportedPinAdapter(requireContext(), reportedPins, this);
        binding.rvReportedPins.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReportedPins.setAdapter(adapter);
    }

    private void setupSearchBar() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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

        // Pin type filter chip
        binding.chipFilterPinType.setOnClickListener(v -> showPinTypeFilterDialog());

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
                    ManagePinViewModel.FilterOption option;
                    switch (which) {
                        case 1:
                            option = ManagePinViewModel.FilterOption.UNCHECKED;
                            break;
                        case 2:
                            option = ManagePinViewModel.FilterOption.CHECKED;
                            break;
                        case 0:
                        default:
                            option = ManagePinViewModel.FilterOption.ALL;
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

    private void showPinTypeFilterDialog() {
        String[] options = {
                getString(R.string.all_pin_types),
                getString(R.string.pin_type_image),
                getString(R.string.pin_type_gif),
                getString(R.string.pin_type_video)
        };

        // Determine current selection
        int selectedIndex = 0;
        Pin.PinType currentType = viewModel.getFilterPinType();
        if (currentType != null) {
            switch (currentType) {
                case IMAGE:
                    selectedIndex = 1;
                    break;
                case GIF:
                    selectedIndex = 2;
                    break;
                case VIDEO:
                    selectedIndex = 3;
                    break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_by_pin_type)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    Pin.PinType type = null;
                    switch (which) {
                        case 1:
                            type = Pin.PinType.IMAGE;
                            break;
                        case 2:
                            type = Pin.PinType.GIF;
                            break;
                        case 3:
                            type = Pin.PinType.VIDEO;
                            break;
                    }
                    viewModel.setFilterPinType(type);
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
        chipCustom.setOnClickListener(v -> customDateLayout.setVisibility(View.VISIBLE));

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
                    ManagePinViewModel.SortOption option;
                    switch (which) {
                        case 0:
                            option = ManagePinViewModel.SortOption.LAST_REPORTED_DESC;
                            break;
                        case 1:
                            option = ManagePinViewModel.SortOption.LAST_REPORTED_ASC;
                            break;
                        case 2:
                            option = ManagePinViewModel.SortOption.REPORT_COUNT_DESC;
                            break;
                        case 3:
                            option = ManagePinViewModel.SortOption.REPORT_COUNT_ASC;
                            break;
                        default:
                            option = ManagePinViewModel.SortOption.LAST_REPORTED_DESC;
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

        // Update pin type chip text
        Pin.PinType pinType = viewModel.getFilterPinType();
        if (pinType == null) {
            binding.chipFilterPinType.setText(R.string.all_pin_types);
        } else {
            switch (pinType) {
                case IMAGE:
                    binding.chipFilterPinType.setText(R.string.pin_type_image);
                    break;
                case GIF:
                    binding.chipFilterPinType.setText(R.string.pin_type_gif);
                    break;
                case VIDEO:
                    binding.chipFilterPinType.setText(R.string.pin_type_video);
                    break;
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
        if (viewModel.getCurrentFilterOption() != ManagePinViewModel.FilterOption.ALL) {
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

        // Add pin type filter if active
        Pin.PinType pinType = viewModel.getFilterPinType();
        if (pinType != null) {
            activeFilters.append(getString(R.string.pin_type_label)).append(" ");
            switch (pinType) {
                case IMAGE:
                    activeFilters.append(getString(R.string.pin_type_image));
                    break;
                case GIF:
                    activeFilters.append(getString(R.string.pin_type_gif));
                    break;
                case VIDEO:
                    activeFilters.append(getString(R.string.pin_type_video));
                    break;
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
        // Observe danh sách pin bị báo cáo
        viewModel.getReportedPins().observe(getViewLifecycleOwner(), pins -> {
            reportedPins.clear();
            if (pins != null) {
                reportedPins.addAll(pins);
            }
            adapter.updateData(reportedPins);

            // Hiển thị empty view nếu không có dữ liệu
            if (reportedPins.isEmpty()) {
                binding.tvNoData.setVisibility(View.VISIBLE);
                binding.rvReportedPins.setVisibility(View.GONE);
            } else {
                binding.tvNoData.setVisibility(View.GONE);
                binding.rvReportedPins.setVisibility(View.VISIBLE);
            }
        });

        // Observe trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        // Observe thông báo lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPinClick(ReportedPin pin) {
        navigateToPinDetail(pin.getPinId());
    }

    @Override
    public void onAuthorClick(ReportedPin pin) {
        navigateToUserProfile(pin.getPinAuthorId());
    }

    @Override
    public void onProcessClick(ReportedPin pin) {
        showProcessReportDialog(pin);
    }

    @Override
    public void onReportInfoClick(ReportedPin pin) {
        showReportDetailDialog(pin);
    }

    private void showReportDetailDialog(ReportedPin pin) {
        // Tạo thông tin chi tiết báo cáo
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        StringBuilder details = new StringBuilder();
        details.append(getString(R.string.pin_title_label)).append(" ").append(pin.getPinTitle() != null ? pin.getPinTitle() : "(Không có tiêu đề)").append("\n\n");
        details.append(getString(R.string.pin_description_label)).append(" ").append(pin.getPinDescription() != null ? pin.getPinDescription() : "(Không có mô tả)").append("\n\n");
        details.append(getString(R.string.pin_type_label)).append(" ");

        if (pin.getPinType() == null) {
            details.append(getString(R.string.pin_type_unknown));
        } else {
            switch (pin.getPinType()) {
                case IMAGE:
                    details.append(getString(R.string.pin_type_image));
                    break;
                case GIF:
                    details.append(getString(R.string.pin_type_gif));
                    break;
                case VIDEO:
                    details.append(getString(R.string.pin_type_video));
                    break;
            }
        }
        details.append("\n");

        details.append(getString(R.string.author_label)).append(" ").append(pin.getPinAuthorName()).append("\n");
        details.append(getString(R.string.pin_time_label)).append(" ").append(sdf.format(new Date(pin.getPinCreatedAt()))).append("\n");
        details.append(getString(R.string.reports_count_label)).append(": ").append(pin.getReportCount()).append("\n");
        details.append(getString(R.string.last_report_time_label)).append(": ").append(sdf.format(new Date(pin.getLastReportedAt()))).append("\n\n");

        details.append(getString(R.string.report_reasons_label)).append(":\n");
        Map<String, Integer> reasonsCount = pin.getReasonsCount();
        for (ReportReason reason : reportReasons) {
            Integer count = reasonsCount.get(reason.getId());
            if (count != null && count > 0) {
                details.append("- ").append(reason.getTitle()).append(": ").append(count).append(" ").append(getString(R.string.reports)).append("\n");
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.report_details_pin)
                .setMessage(details.toString())
                .setPositiveButton(R.string.close, null)
                .create();

        dialog.show();
    }

    private void showProcessReportDialog(ReportedPin pin) {
        String[] options = {
                getString(R.string.mark_as_checked),
                getString(R.string.delete_pin)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.process_report)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Đánh dấu đã kiểm tra
                            showMarkAsCheckedDialog(pin);
                            break;
                        case 1:
                            // Xóa pin
                            showDeleteConfirmDialog(pin);
                            break;
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showMarkAsCheckedDialog(ReportedPin pin) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm)
                .setMessage(R.string.mark_as_checked_confirm_pin)
                .setPositiveButton(R.string.confirm, (dialog, which) -> viewModel.markReportAsChecked(pin.getReportId(), success -> {
                    if (success) {
                        Toast.makeText(requireContext(), R.string.marked_as_checked, Toast.LENGTH_SHORT).show();
                        // Gửi thông báo cho người dùng
                        viewModel.sendNotifications(pin, false, sent -> {
                            if (sent) {
                                Toast.makeText(requireContext(), R.string.notification_sent, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    }
                }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteConfirmDialog(ReportedPin pin) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_pin_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deletePin(pin.getPinId(), success -> {
                    if (success) {
                        Toast.makeText(requireContext(), R.string.pin_deleted, Toast.LENGTH_SHORT).show();
                        // Gửi thông báo cho người dùng
                        viewModel.sendNotifications(pin, true, sent -> {
                            if (sent) {
                                Toast.makeText(requireContext(), R.string.notification_sent, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), R.string.error_deleting_pin, Toast.LENGTH_SHORT).show();
                    }
                }))
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

                pins.get(0).setTags(new ArrayList<>());
                // Bundle để truyền dữ liệu
                Bundle args = new Bundle();
                args.putParcelableArrayList("pins", new ArrayList<>(pins));
                args.putString("source", "admin_pin"); // Mark source as admin
                args.putInt("initial_position", 0);
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin);
                navController.navigate(R.id.action_managePinFragment_to_pinFragment, args);
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

        // Điều hướng đến UserProfileFragment
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin);
        navController.navigate(R.id.action_managePinFragment_to_userProfileFragment, args);
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
            viewModel.fetchReportedPins();
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

        // Đếm theo loại pin
        Map<String, Integer> typeStats = new HashMap<>();
        typeStats.put(getString(R.string.pin_type_image), 0);
        typeStats.put(getString(R.string.pin_type_gif), 0);
        typeStats.put(getString(R.string.pin_type_video), 0);
        typeStats.put(getString(R.string.pin_type_unknown), 0);

        for (ReportedPin pin : reportedPins) {
            // Đếm trạng thái
            if (pin.isChecked()) {
                checkedCount++;
            } else {
                uncheckedCount++;
            }

            // Đếm theo lý do
            Map<String, Integer> pinReasons = pin.getReasonsCount();
            for (ReportReason reason : reportReasons) {
                Integer count = pinReasons.get(reason.getId());
                if (count != null && count > 0) {
                    Integer current = reasonStats.get(reason.getTitle());
                    reasonStats.put(reason.getTitle(), current + 1);
                }
            }

            // Đếm theo loại pin
            String typeKey;
            Pin.PinType type = pin.getPinType();
            if (type == null) {
                typeKey = getString(R.string.pin_type_unknown);
            } else {
                switch (type) {
                    case IMAGE:
                        typeKey = getString(R.string.pin_type_image);
                        break;
                    case GIF:
                        typeKey = getString(R.string.pin_type_gif);
                        break;
                    case VIDEO:
                        typeKey = getString(R.string.pin_type_video);
                        break;
                    default:
                        typeKey = getString(R.string.pin_type_unknown);
                }
            }
            Integer typeCount = typeStats.get(typeKey);
            typeStats.put(typeKey, typeCount + 1);
        }

        // Tạo nội dung dialog
        StringBuilder stats = new StringBuilder();
        stats.append(getString(R.string.total_pin_reports)).append(": ").append(reportedPins.size()).append("\n\n");
        stats.append(getString(R.string.status_label)).append(":\n");
        stats.append("- ").append(getString(R.string.processed)).append(": ").append(checkedCount).append("\n");
        stats.append("- ").append(getString(R.string.unprocessed)).append(": ").append(uncheckedCount).append("\n\n");

        stats.append(getString(R.string.pin_type_label)).append(":\n");
        for (Map.Entry<String, Integer> entry : typeStats.entrySet()) {
            if (entry.getValue() > 0) {
                stats.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        stats.append("\n");

        stats.append(getString(R.string.report_reasons_label)).append(":\n");
        for (Map.Entry<String, Integer> entry : reasonStats.entrySet()) {
            if (entry.getValue() > 0) {
                stats.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.pin_report_statistics)
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