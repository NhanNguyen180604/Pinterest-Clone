package com.example.pinterest_clone_test2.ui.admin.manage_comment;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageCommentFragment extends Fragment {

    private FragmentManageCommentBinding binding;
    private ManageCommentViewModel viewModel;
    private ReportedCommentAdapter adapter;
    private List<ReportedComment> reportedComments = new ArrayList<>();

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
        setupFilterControls();

        // Thiết lập các observers
        setupObservers();

        // Tải dữ liệu ban đầu
        viewModel.fetchReportedComments();
    }

    private void setupRecyclerView() {
        adapter = new ReportedCommentAdapter(requireContext(), reportedComments, new ReportedCommentAdapter.OnReportedCommentListener() {
            @Override
            public void onCommentClick(ReportedComment comment) {
                // Hiển thị dialog chi tiết báo cáo
                showReportDetailDialog(comment);
            }

            @Override
            public void onMarkAsCheckedClick(ReportedComment comment) {
                // Hiển thị dialog xác nhận đánh dấu đã kiểm tra
                showMarkAsCheckedDialog(comment);
            }

            @Override
            public void onDeleteClick(ReportedComment comment) {
                // Hiển thị dialog xác nhận xóa
                showDeleteConfirmDialog(comment);
            }

            @Override
            public void onViewPinClick(ReportedComment comment) {
                // Chuyển đến màn hình pin chi tiết
                navigateToPinDetail(comment.getPinId());
            }
        });

        binding.rvReportedComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReportedComments.setAdapter(adapter);
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
                ManageCommentViewModel.SortOption option;
                switch (position) {
                    case 0:
                        option = ManageCommentViewModel.SortOption.LAST_REPORTED_DESC;
                        break;
                    case 1:
                        option = ManageCommentViewModel.SortOption.LAST_REPORTED_ASC;
                        break;
                    case 2:
                        option = ManageCommentViewModel.SortOption.COMMENT_CREATED_DESC;
                        break;
                    case 3:
                        option = ManageCommentViewModel.SortOption.COMMENT_CREATED_ASC;
                        break;
                    case 4:
                        option = ManageCommentViewModel.SortOption.REPORT_COUNT_DESC;
                        break;
                    case 5:
                        option = ManageCommentViewModel.SortOption.REPORT_COUNT_ASC;
                        break;
                    default:
                        option = ManageCommentViewModel.SortOption.LAST_REPORTED_DESC;
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
                ManageCommentViewModel.FilterOption option;
                switch (position) {
                    case 0:
                        option = ManageCommentViewModel.FilterOption.ALL;
                        break;
                    case 1:
                        option = ManageCommentViewModel.FilterOption.UNCHECKED;
                        break;
                    case 2:
                        option = ManageCommentViewModel.FilterOption.CHECKED;
                        break;
                    default:
                        option = ManageCommentViewModel.FilterOption.ALL;
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
            binding.searchView.setQuery("", false);
            binding.tvStartDate.setText(R.string.start_date);
            binding.tvEndDate.setText(R.string.end_date);
        });
    }

    private void setupObservers() {
        // Observe danh sách comment bị báo cáo
        viewModel.getReportedComments().observe(getViewLifecycleOwner(), comments -> {
            reportedComments.clear();
            if (comments != null) {
                reportedComments.addAll(comments);
            }
            adapter.notifyDataSetChanged();

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

    private void showReportDetailDialog(ReportedComment comment) {
        // Tạo thông tin chi tiết báo cáo
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        StringBuilder details = new StringBuilder();
        details.append("Nội dung: ").append(comment.getCommentContent()).append("\n\n");
        details.append("Tác giả: ").append(comment.getCommentAuthorName()).append("\n");
        details.append("Thời gian bình luận: ").append(sdf.format(new Date(comment.getCommentCreatedAt()))).append("\n");
        details.append("Số lượt báo cáo: ").append(comment.getReportCount()).append("\n");
        details.append("Thời gian báo cáo gần nhất: ").append(sdf.format(new Date(comment.getLastReportedAt()))).append("\n\n");

        details.append("Lý do báo cáo:\n");
        Map<String, Integer> reasonsCount = comment.getReasonsCount();
        for (ReportReason reason : reportReasons) {
            Integer count = reasonsCount.get(reason.getId());
            if (count != null && count > 0) {
                details.append("- ").append(reason.getTitle()).append(": ").append(count).append(" lượt\n");
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chi tiết báo cáo")
                .setMessage(details.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showMarkAsCheckedDialog(ReportedComment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn đánh dấu báo cáo này là đã kiểm tra?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    viewModel.markReportAsChecked(comment.getReportId(), success -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Đã đánh dấu kiểm tra", Toast.LENGTH_SHORT).show();
                            // Gửi thông báo cho người dùng
                            viewModel.sendNotifications(comment, false, sent -> {
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

    private void showDeleteConfirmDialog(ReportedComment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bình luận này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteComment(comment.getCommentId(), success -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                            // Gửi thông báo cho người dùng
                            viewModel.sendNotifications(comment, true, sent -> {
                                if (sent) {
                                    Toast.makeText(requireContext(), "Đã gửi thông báo cho người dùng", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), "Có lỗi xảy ra khi xóa bình luận", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void navigateToPinDetail(String pinId) {
        if (pinId == null || pinId.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin bài đăng", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebasePinService.fetchPinsFromIds(List.of(pinId), new FirebasePinService.OnPinsFetchedFromIdsCallback() {
            @Override
            public void onSuccess(List<Pin> pins) {
                if (pins.isEmpty()) {
                    Toast.makeText(requireContext(), "Không tìm thấy thông tin bài đăng", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Bundle để truyền dữ liệu
                Bundle args = new Bundle();
                args.putParcelableArrayList("pins", new ArrayList<>(pins));
                args.putString("source", "admin");
                args.putInt("initial_position", 0);
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin);
                navController.navigate(R.id.action_manageCommentFragment_to_pinFragment4, args);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
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
        stats.append("Tổng số báo cáo: ").append(reportedComments.size()).append("\n\n");
        stats.append("Trạng thái:\n");
        stats.append("- Đã xử lý: ").append(checkedCount).append("\n");
        stats.append("- Chưa xử lý: ").append(uncheckedCount).append("\n\n");

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