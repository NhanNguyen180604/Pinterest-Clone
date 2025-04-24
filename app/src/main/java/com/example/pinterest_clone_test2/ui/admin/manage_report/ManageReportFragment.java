package com.example.pinterest_clone_test2.ui.admin.manage_report;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PriorityReportAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentManageReportBinding;
import com.example.pinterest_clone_test2.models.PriorityReport;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportSummary;
import com.example.pinterest_clone_test2.services.firebase.ReportDashboardService;
import com.example.pinterest_clone_test2.ui.admin.views.ReportHeatmapView;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManageReportFragment extends Fragment {

    private FragmentManageReportBinding binding;
    private ManageReportViewModelEnhanced viewModel;
    private PriorityReportAdapter adapter;
    private List<PriorityReport> priorityReports = new ArrayList<>();
    private List<ReportReason> reportReasons;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManageReportViewModelEnhanced.class);

        // Lấy danh sách lý do báo cáo
        reportReasons = ReportReason.GetReasons(requireContext());

        // Thiết lập UI
        setupUI();

        // Thiết lập observers
        setupObservers();

        // Tải dữ liệu
        viewModel.loadDashboardData();
    }

    private void setupUI() {
        // Thiết lập Spinner khoảng thời gian
        setupTimeRangeSpinner();

        // Thiết lập chip filter cho mức độ nghiêm trọng
        setupSeverityFilterChips();

        // Thiết lập RecyclerView báo cáo ưu tiên
        setupPriorityReportsRecyclerView();

        // Khởi tạo biểu đồ nhiệt
        setupHeatmapView();

        // Thiết lập sự kiện nút làm mới
        binding.btnRefreshData.setOnClickListener(v -> viewModel.loadDashboardData());

        // Thiết lập sự kiện nút xem tất cả báo cáo
        binding.btnViewAllReports.setOnClickListener(v -> {
            // TODO: Chuyển đến màn hình danh sách tất cả báo cáo
            Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTimeRangeSpinner() {
        // Tạo adapter cho spinner khoảng thời gian
        String[] timeRangeOptions = {
                getString(R.string.time_range_today),
                getString(R.string.time_range_week),
                getString(R.string.time_range_month),
                getString(R.string.time_range_quarter),
                getString(R.string.time_range_year)
        };

        ArrayAdapter<String> timeRangeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                timeRangeOptions
        );
        timeRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTimeRange.setAdapter(timeRangeAdapter);

        // Thiết lập giá trị mặc định (7 ngày qua)
        binding.spinnerTimeRange.setSelection(1);

        // Thiết lập sự kiện khi chọn khoảng thời gian
        binding.spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ReportDashboardService.TimeRange timeRange;
                switch (position) {
                    case 0:
                        timeRange = ReportDashboardService.TimeRange.TODAY;
                        break;
                    case 1:
                        timeRange = ReportDashboardService.TimeRange.LAST_WEEK;
                        break;
                    case 2:
                        timeRange = ReportDashboardService.TimeRange.LAST_MONTH;
                        break;
                    case 3:
                        timeRange = ReportDashboardService.TimeRange.LAST_QUARTER;
                        break;
                    case 4:
                        timeRange = ReportDashboardService.TimeRange.LAST_YEAR;
                        break;
                    default:
                        timeRange = ReportDashboardService.TimeRange.LAST_WEEK;
                }
                viewModel.setCurrentTimeRange(timeRange);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSeverityFilterChips() {
        // TODO: Thiết lập các chip filter khi chúng ta có ViewModel cần thiết
    }

    private void setupHeatmapView() {
        if (binding.heatmapContainer != null) {
            ReportHeatmapView heatmapView = new ReportHeatmapView(requireContext());
            binding.heatmapContainer.addView(heatmapView);

            // Observe dữ liệu heatmap
            viewModel.getHeatmapData().observe(getViewLifecycleOwner(), heatmapData -> {
                if (heatmapData != null) {
                    heatmapView.setHeatmapData(heatmapData);
                }
            });
        }
    }

    private void setupPriorityReportsRecyclerView() {
        // Khởi tạo adapter
        adapter = new PriorityReportAdapter(requireContext(), priorityReports, new PriorityReportAdapter.OnPriorityReportClickListener() {
            @Override
            public void onReportClick(PriorityReport report) {
                // Chuyển đến màn hình chi tiết báo cáo
                navigateToReportDetail(report);
            }

            @Override
            public void onTakeActionClick(PriorityReport report) {
                // Chuyển đến màn hình xử lý báo cáo
                navigateToHandleReport(report);
            }
        });

        // Thiết lập RecyclerView
        if (binding.rvPriorityReports != null) {
            binding.rvPriorityReports.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvPriorityReports.setAdapter(adapter);
        }
    }

    private void setupObservers() {
        // Observe thông tin tổng hợp báo cáo
        viewModel.getReportSummary().observe(getViewLifecycleOwner(), this::updateSummaryUI);

        // Observe danh sách báo cáo ưu tiên
        viewModel.getPriorityReports().observe(getViewLifecycleOwner(), reports -> {
            priorityReports.clear();
            if (reports != null) {
                priorityReports.addAll(reports);
            }
            adapter.notifyDataSetChanged();

            // Hiển thị hoặc ẩn thông báo "không có báo cáo"
            if (binding.tvNoPriorityReports != null) {
                binding.tvNoPriorityReports.setVisibility(reports == null || reports.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        // Observe trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }

            if (binding.loadingOverlay != null) {
                binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe thông báo lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummaryUI(ReportSummary summary) {
        if (summary == null) return;

        // Cập nhật thông tin tổng hợp
        binding.tvTotalReports.setText(String.valueOf(summary.getTotalReports()));
        binding.tvPendingReports.setText(String.valueOf(summary.getTotalPendingReports()));
        binding.tvAvgProcessingTime.setText(summary.getAverageProcessingTimeInHours() + "h");

        // Cập nhật phần trăm thay đổi
        String totalChange = viewModel.formatChangePercent(summary.getTotalReportsChangePercent());
        binding.tvTotalReportsChange.setText(String.format(getString(R.string.from_previous_period), totalChange));

        String avgTimeChange = viewModel.formatChangePercent(summary.getAverageProcessingTimeChangePercent());
        binding.tvAvgProcessingTimeChange.setText(String.format(getString(R.string.from_previous_period), avgTimeChange));

        // Cập nhật phần trăm báo cáo chưa xử lý
        int pendingPercent = viewModel.calculatePercentage(summary.getTotalPendingReports(), summary.getTotalReports());
        binding.tvPendingReportsPercent.setText(String.format(getString(R.string.of_total), pendingPercent));

        // Cập nhật biểu đồ
        updateChartsUI(summary);
    }

    private void updateChartsUI(ReportSummary summary) {
        updateTrendChart(summary);
        updateReportTypeChart(summary);
        updateReportReasonsChart(summary);
    }

    private void updateTrendChart(ReportSummary summary) {
        if (binding.chartReportsTrend == null) return;

        LineChart chart = binding.chartReportsTrend;
        chart.clear();

        // Thiết lập dữ liệu
        Map<String, Integer> trendData = summary.getTrendData();
        List<String> dates = new ArrayList<>(trendData.keySet());
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < dates.size(); i++) {
            String date = dates.get(i);
            int count = trendData.get(date);
            entries.add(new Entry(i, count));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Số lượng báo cáo");
        dataSet.setColor(getResources().getColor(R.color.red_pinterest, null));
        dataSet.setValueTextColor(getResources().getColor(R.color.dark_grey, null));
        dataSet.setCircleColor(getResources().getColor(R.color.red_pinterest, null));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Thiết lập trục X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));

        // Thiết lập trục Y
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setGranularity(1f);
        yAxis.setAxisMinimum(0f);

        // Ẩn trục Y bên phải
        chart.getAxisRight().setEnabled(false);

        // Thiết lập các tùy chọn khác
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraBottomOffset(10f);

        chart.animateY(1000);
        chart.invalidate();
    }

    private void updateReportTypeChart(ReportSummary summary) {
        if (binding.chartReportsByType == null) return;

        PieChart chart = binding.chartReportsByType;
        chart.clear();

        // Dữ liệu cho biểu đồ
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(summary.getPinReports(), "Pin"));
        entries.add(new PieEntry(summary.getCommentReports(), "Bình luận"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                getResources().getColor(R.color.red_pinterest, null),
                getResources().getColor(R.color.light_blue_600, null)
        );
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        chart.setData(pieData);

        // Thiết lập biểu đồ
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleRadius(0f);
        chart.setUsePercentValues(false);
        chart.setDrawEntryLabels(false);

        // Ẩn legend mặc định
        chart.getLegend().setEnabled(false);

        // Tạo legend tùy chỉnh
        if (binding.layoutReportsByTypeLegend != null) {
            LinearLayout legendLayout = binding.layoutReportsByTypeLegend;
            legendLayout.removeAllViews();

            // Thêm legend items
            addLegendItem(legendLayout, "Pin", R.color.red_pinterest, summary.getPinReports());
            addLegendItem(legendLayout, "Bình luận", R.color.light_blue_600, summary.getCommentReports());
        }

        chart.animateY(1000);
        chart.invalidate();
    }

    private void addLegendItem(LinearLayout container, String label, int colorRes, int count) {
        View legendItem = getLayoutInflater().inflate(R.layout.item_chart_legend, container, false);

        View colorIndicator = legendItem.findViewById(R.id.view_color_indicator);
        TextView tvLabel = legendItem.findViewById(R.id.tv_label);
        TextView tvCount = legendItem.findViewById(R.id.tv_count);

        colorIndicator.setBackgroundColor(getResources().getColor(colorRes, null));
        tvLabel.setText(label);
        tvCount.setText(String.valueOf(count));

        container.addView(legendItem);
    }

    private void updateReportReasonsChart(ReportSummary summary) {
        if (binding.chartReportReasons == null) return;

        HorizontalBarChart chart = binding.chartReportReasons;
        chart.clear();

        Map<String, Integer> reasonsCount = summary.getReportReasonCounts();
        Map<String, String> reasonIdToTitleMap = new LinkedHashMap<>();

        // Tạo map từ reasonId -> title
        for (ReportReason reason : reportReasons) {
            reasonIdToTitleMap.put(reason.getId(), reason.getTitle());
        }

        // Lọc chỉ lấy các reason có số lượng > 0
        Map<String, Integer> filteredReasons = reasonsCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<String> reasonIds = new ArrayList<>(filteredReasons.keySet());
        List<String> reasonTitles = reasonIds.stream()
                .map(id -> reasonIdToTitleMap.getOrDefault(id, "Không xác định"))
                .collect(Collectors.toList());

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < reasonIds.size(); i++) {
            String id = reasonIds.get(i);
            int count = filteredReasons.get(id);
            entries.add(new BarEntry(i, count));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(getResources().getColor(R.color.red_pinterest, null));
        dataSet.setValueTextColor(getResources().getColor(R.color.dark_grey, null));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        chart.setData(barData);

        // Thiết lập trục X (ngang)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(reasonTitles));
        xAxis.setLabelRotationAngle(-45f);

        // Thiết lập trục Y (dọc)
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setGranularity(1f);
        yAxis.setAxisMinimum(0f);

        // Ẩn trục Y bên phải
        chart.getAxisRight().setEnabled(false);

        // Thiết lập các tùy chọn khác
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraBottomOffset(20f);

        chart.animateY(1000);
        chart.invalidate();
    }

    private void navigateToReportDetail(PriorityReport report) {
        // TODO: Chuyển đến màn hình chi tiết báo cáo dựa vào loại báo cáo
        if (report.getReportType() == PriorityReport.ReportType.PIN) {
            // Chuyển đến màn hình chi tiết báo cáo pin
            Toast.makeText(requireContext(), "Xem chi tiết báo cáo PIN " + report.getContentId(), Toast.LENGTH_SHORT).show();
        } else {
            // Chuyển đến màn hình chi tiết báo cáo bình luận
            Toast.makeText(requireContext(), "Xem chi tiết báo cáo COMMENT " + report.getContentId(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHandleReport(PriorityReport report) {
        // TODO: Chuyển đến màn hình xử lý báo cáo
        if (report.getReportType() == PriorityReport.ReportType.PIN) {
            // Chuyển đến ManagePinFragment và truyền ID của pin
            Toast.makeText(requireContext(), "Xử lý báo cáo PIN " + report.getContentId(), Toast.LENGTH_SHORT).show();
        } else {
            // Chuyển đến ManageCommentFragment và truyền ID của comment
            Toast.makeText(requireContext(), "Xử lý báo cáo COMMENT " + report.getContentId(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}