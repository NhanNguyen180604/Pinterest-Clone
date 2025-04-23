package com.example.pinterest_clone_test2.ui.admin.manage_pin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.ReportedPinAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentManagePinBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportedPin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManagePinFragment extends Fragment {

    private FragmentManagePinBinding binding;
    private ManagePinViewModel viewModel;
    private ReportedPinAdapter adapter;
    private List<ReportedPin> reportedPins = new ArrayList<>();

    // Danh sách lý do báo cáo cho spinner
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
        setupFilterControls();

        // Thiết lập các observers
        setupObservers();

        // Tải dữ liệu ban đầu
        viewModel.fetchReportedPins();
    }

    private void setupRecyclerView() {
        adapter = new ReportedPinAdapter(requireContext(), reportedPins, new ReportedPinAdapter.OnReportedPinListener() {
            @Override
            public void onPinClick(ReportedPin pin) {
                // Hiển thị dialog chi tiết báo cáo
                showReportDetailDialog(pin);
            }

            @Override
            public void onMarkAsCheckedClick(ReportedPin pin) {
                // Hiển thị dialog xác nhận đánh dấu đã kiểm tra
                showMarkAsCheckedDialog(pin);
            }

            @Override
            public void onDeleteClick(ReportedPin pin) {
                // Hiển thị dialog xác nhận xóa
                showDeleteConfirmDialog(pin);
            }

            @Override
            public void onViewAuthorClick(ReportedPin pin) {
                // Chuyển đến trang profile người dùng
                navigateToUserProfile(pin.getPinAuthorId());
            }
        });

        binding.rvReportedPins.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReportedPins.setAdapter(adapter);
    }

    private void setupFilterControls() {
        // Thiết lập Spinner sắp xếp
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSort.setAdapter(sortAdapter);
        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ManagePinViewModel.SortOption option;
                switch (position) {
                    case 0:
                        option = ManagePinViewModel.SortOption.LAST_REPORTED_DESC;
                        break;
                    case 1:
                        option = ManagePinViewModel.SortOption.LAST_REPORTED_ASC;
                        break;
                    case 2:
                        option = ManagePinViewModel.SortOption.PIN_CREATED_DESC;
                        break;
                    case 3:
                        option = ManagePinViewModel.SortOption.PIN_CREATED_ASC;
                        break;
                    case 4:
                        option = ManagePinViewModel.SortOption.REPORT_COUNT_DESC;
                        break;
                    case 5:
                        option = ManagePinViewModel.SortOption.REPORT_COUNT_ASC;
                        break;
                    default:
                        option = ManagePinViewModel.SortOption.LAST_REPORTED_DESC;
                }
                viewModel.setCurrentSortOption(option);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Thiết lập Spinner lọc trạng thái
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.status_filter_options,
                android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(statusAdapter);
        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ManagePinViewModel.FilterOption option;
                switch (position) {
                    case 0:
                        option = ManagePinViewModel.FilterOption.ALL;
                        break;
                    case 1:
                        option = ManagePinViewModel.FilterOption.UNCHECKED;
                        break;
                    case 2:
                        option = ManagePinViewModel.FilterOption.CHECKED;
                        break;
                    default:
                        option = ManagePinViewModel.FilterOption.ALL;
                }
                viewModel.setCurrentFilterOption(option);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Thiết lập Spinner lọc lý do báo cáo
        List<String> reasonTexts = new ArrayList<>();
        reasonTexts.add("Tất cả lý do");
        for (ReportReason reason : reportReasons) {
            reasonTexts.add(reason.getTitle());
        }

        ArrayAdapter<String> reasonAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                reasonTexts
        );
        reasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerReason.setAdapter(reasonAdapter);
        binding.spinnerReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Tất cả lý do
                    viewModel.setFilterReasonId(null);
                } else {
                    // Lọc theo lý do cụ thể
                    viewModel.setFilterReasonId(reportReasons.get(position - 1).getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Thiết lập Spinner lọc loại Pin
        List<String> pinTypeTexts = new ArrayList<>();
        pinTypeTexts.add("Tất cả loại");
        pinTypeTexts.add("Hình ảnh");
        pinTypeTexts.add("GIF");
        pinTypeTexts.add("Video");

        ArrayAdapter<String> pinTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                pinTypeTexts
        );
        pinTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPinType.setAdapter(pinTypeAdapter);
        binding.spinnerPinType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Pin.PinType type = null;
                switch (position) {
                    case 0: // Tất cả loại
                        type = null;
                        break;
                    case 1: // Hình ảnh
                        type = Pin.PinType.IMAGE;
                        break;
                    case 2: // GIF
                        type = Pin.PinType.GIF;
                        break;
                    case 3: // Video
                        type = Pin.PinType.VIDEO;
                        break;
                }
                viewModel.setFilterPinType(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Thiết lập SearchView
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });

        // Thiết lập nút chọn ngày
        binding.btnSelectStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        binding.btnSelectEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Thiết lập nút reset filter
        binding.btnResetFilter.setOnClickListener(v -> {
            viewModel.resetFilters();
            binding.spinnerStatus.setSelection(0);
            binding.spinnerReason.setSelection(0);
            binding.spinnerPinType.setSelection(0);
            binding.searchView.setQuery("", false);
            binding.tvStartDate.setText(R.string.start_date);
            binding.tvEndDate.setText(R.string.end_date);
        });
    }

    private void setupObservers() {
        // Observe danh sách pin bị báo cáo
        viewModel.getReportedPins().observe(getViewLifecycleOwner(), pins -> {
            reportedPins.clear();
            if (pins != null) {
                reportedPins.addAll(pins);
            }
            adapter.notifyDataSetChanged();

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

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    // Format date for display
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(selectedDate.getTime());

                    if (isStartDate) {
                        binding.tvStartDate.setText(formattedDate);
                        viewModel.setFilterStartDate(selectedDate.getTime());
                    } else {
                        binding.tvEndDate.setText(formattedDate);
                        // Thiết lập thời gian là cuối ngày
                        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
                        selectedDate.set(Calendar.MINUTE, 59);
                        selectedDate.set(Calendar.SECOND, 59);
                        viewModel.setFilterEndDate(selectedDate.getTime());
                    }
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void showReportDetailDialog(ReportedPin pin) {
        // Tạo thông tin chi tiết báo cáo
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        StringBuilder details = new StringBuilder();
        details.append("Tiêu đề: ").append(pin.getPinTitle() != null ? pin.getPinTitle() : "(Không có tiêu đề)").append("\n\n");
        details.append("Mô tả: ").append(pin.getPinDescription() != null ? pin.getPinDescription() : "(Không có mô tả)").append("\n\n");
        details.append("Loại Pin: ").append(getPinTypeText(pin.getPinType())).append("\n");
        details.append("Tác giả: ").append(pin.getPinAuthorName()).append("\n");
        details.append("Thời gian tạo: ").append(sdf.format(new Date(pin.getPinCreatedAt()))).append("\n");
        details.append("Số lượt báo cáo: ").append(pin.getReportCount()).append("\n");
        details.append("Thời gian báo cáo gần nhất: ").append(sdf.format(new Date(pin.getLastReportedAt()))).append("\n\n");

        details.append("Lý do báo cáo:\n");
        Map<String, Integer> reasonsCount = pin.getReasonsCount();
        for (ReportReason reason : reportReasons) {
            Integer count = reasonsCount.get(reason.getId());
            if (count != null && count > 0) {
                details.append("- ").append(reason.getTitle()).append(": ").append(count).append(" lượt\n");
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Chi tiết báo cáo")
                .setMessage(details.toString())
                .setPositiveButton("Đóng", null)
                .create();

        dialog.show();
    }

    private String getPinTypeText(Pin.PinType type) {
        if (type == null) {
            return "Không xác định";
        }
        switch (type) {
            case IMAGE:
                return "Hình ảnh";
            case GIF:
                return "GIF";
            case VIDEO:
                return "Video";
            default:
                return "Không xác định";
        }
    }

    private void showMarkAsCheckedDialog(ReportedPin pin) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn đánh dấu báo cáo này là đã kiểm tra?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    viewModel.markReportAsChecked(pin.getReportId(), success -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Đã đánh dấu kiểm tra", Toast.LENGTH_SHORT).show();
                            // Gửi thông báo cho người dùng
                            viewModel.sendNotifications(pin, false, sent -> {
                                if (sent) {
                                    Toast.makeText(requireContext(), "Đã gửi thông báo cho người báo cáo", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmDialog(ReportedPin pin) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa pin này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deletePin(pin.getPinId(), success -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Đã xóa pin", Toast.LENGTH_SHORT).show();
                            // Gửi thông báo cho người dùng
                            viewModel.sendNotifications(pin, true, sent -> {
                                if (sent) {
                                    Toast.makeText(requireContext(), "Đã gửi thông báo cho người dùng", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), "Có lỗi xảy ra khi xóa pin", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void navigateToUserProfile(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bundle để truyền dữ liệu
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("source", "admin"); // Đánh dấu nguồn là từ trang admin

        // Tạo UserProfileFragment và truyền dữ liệu
        Fragment userProfileFragment = new com.example.pinterest_clone_test2.ui.user.UserProfileFragment();
        userProfileFragment.setArguments(args);

        // Sử dụng FragmentTransaction để thay thế fragment hiện tại
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_activity_admin, userProfileFragment)
                .addToBackStack(null)
                .commit();
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
        typeStats.put("Hình ảnh", 0);
        typeStats.put("GIF", 0);
        typeStats.put("Video", 0);
        typeStats.put("Không xác định", 0);

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
                typeKey = "Không xác định";
            } else {
                switch (type) {
                    case IMAGE:
                        typeKey = "Hình ảnh";
                        break;
                    case GIF:
                        typeKey = "GIF";
                        break;
                    case VIDEO:
                        typeKey = "Video";
                        break;
                    default:
                        typeKey = "Không xác định";
                }
            }
            Integer typeCount = typeStats.get(typeKey);
            typeStats.put(typeKey, typeCount + 1);
        }

        // Tạo nội dung dialog
        StringBuilder stats = new StringBuilder();
        stats.append("Tổng số báo cáo: ").append(reportedPins.size()).append("\n\n");
        stats.append("Trạng thái:\n");
        stats.append("- Đã xử lý: ").append(checkedCount).append("\n");
        stats.append("- Chưa xử lý: ").append(uncheckedCount).append("\n\n");

        stats.append("Loại Pin:\n");
        for (Map.Entry<String, Integer> entry : typeStats.entrySet()) {
            if (entry.getValue() > 0) {
                stats.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        stats.append("\n");

        stats.append("Lý do báo cáo:\n");
        for (Map.Entry<String, Integer> entry : reasonStats.entrySet()) {
            if (entry.getValue() > 0) {
                stats.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Thống kê báo cáo")
                .setMessage(stats.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}