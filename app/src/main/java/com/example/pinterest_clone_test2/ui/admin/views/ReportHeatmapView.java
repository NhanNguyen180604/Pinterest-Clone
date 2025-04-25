package com.example.pinterest_clone_test2.ui.admin.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.pinterest_clone_test2.R;

/**
 * Custom view để hiển thị biểu đồ nhiệt (heatmap) của các báo cáo
 * theo ngày trong tuần và giờ
 */
public class ReportHeatmapView extends View {
    // Dữ liệu heatmap [dayOfWeek][hour]
    private int[][] heatmapData;

    // Giá trị tối đa trong dữ liệu heatmap
    private int maxValue = 0;

    // Tên ngày và giờ
    private final String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private final String[] hourLabels = {"0", "6", "12", "18", "23"};

    // Các biến cho vẽ
    private final Paint cellPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Rect textBounds = new Rect();

    // Màu sắc heatmap
    private final int[] heatColors = new int[5];

    public ReportHeatmapView(Context context) {
        super(context);
        init(context);
    }

    public ReportHeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReportHeatmapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Khởi tạo Paint cho các cell
        cellPaint.setStyle(Paint.Style.FILL);

        // Khởi tạo Paint cho văn bản
        textPaint.setColor(ContextCompat.getColor(context, R.color.dark_grey));
        textPaint.setTextSize(spToPx(12));
        textPaint.setAntiAlias(true);

        // Khởi tạo Paint cho lưới
        gridPaint.setColor(ContextCompat.getColor(context, R.color.light_grey));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dpToPx(0.5f));

        // Khởi tạo màu sắc heatmap
        heatColors[0] = ContextCompat.getColor(context, R.color.heatmap_background);
        heatColors[1] = ContextCompat.getColor(context, R.color.heatmap_very_low);
        heatColors[2] = ContextCompat.getColor(context, R.color.heatmap_low);
        heatColors[3] = ContextCompat.getColor(context, R.color.heatmap_medium);
        heatColors[4] = ContextCompat.getColor(context, R.color.heatmap_high);
    }

    /**
     * Cập nhật dữ liệu heatmap
     * @param data Mảng 2 chiều [ngày][giờ] chứa số lượng báo cáo
     */
    public void setHeatmapData(int[][] data) {
        if (data == null || data.length != 7 || data[0].length != 24) {
            throw new IllegalArgumentException("Heatmap data must be a 7x24 array");
        }

        this.heatmapData = data;

        // Tìm giá trị lớn nhất
        maxValue = 0;
        for (int[] row : data) {
            for (int value : row) {
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }

        invalidate(); // Vẽ lại view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (heatmapData == null) {
            return;
        }

        // Kích thước view
        int width = getWidth();
        int height = getHeight();

        // Kích thước và vị trí của lưới
        float labelPaddingLeft = dpToPx(30); // Khoảng cách cho nhãn ngày
        float labelPaddingTop = dpToPx(25);  // Khoảng cách cho nhãn giờ
        float labelPaddingRight = dpToPx(10); // Padding bên phải
        float labelPaddingBottom = dpToPx(10); // Padding bên dưới

        float gridWidth = width - labelPaddingLeft - labelPaddingRight;
        float gridHeight = height - labelPaddingTop - labelPaddingBottom;

        // Kích thước của mỗi ô trong lưới
        float cellWidth = gridWidth / 24; // 24 giờ
        float cellHeight = gridHeight / 7; // 7 ngày

        // Vẽ nhãn ngày (bên trái)
        for (int day = 0; day < 7; day++) {
            String dayName = dayNames[day];
            textPaint.getTextBounds(dayName, 0, dayName.length(), textBounds);
            float textWidth = textBounds.width();
            float textHeight = textBounds.height();

            float x = labelPaddingLeft - textWidth - dpToPx(5);
            float y = labelPaddingTop + day * cellHeight + cellHeight / 2 + textHeight / 2;

            canvas.drawText(dayName, x, y, textPaint);
        }

        // Vẽ nhãn giờ (bên trên)
        int hourStep = 6; // Hiển thị mỗi 6 giờ
        for (int i = 0; i < hourLabels.length; i++) {
            int hour = i * 6;
            if (hour > 23) hour = 23;

            String hourLabel = hourLabels[i];
            textPaint.getTextBounds(hourLabel, 0, hourLabel.length(), textBounds);
            float textWidth = textBounds.width();
            float textHeight = textBounds.height();

            float x = labelPaddingLeft + hour * cellWidth - textWidth / 2;
            float y = labelPaddingTop - dpToPx(5);

            canvas.drawText(hourLabel, x, y, textPaint);
        }

        // Vẽ các ô heatmap
        for (int day = 0; day < 7; day++) {
            for (int hour = 0; hour < 24; hour++) {
                int value = heatmapData[day][hour];

                // Tính màu sắc dựa trên giá trị
                cellPaint.setColor(getColorForValue(value));

                // Tính vị trí và kích thước của ô
                float left = labelPaddingLeft + hour * cellWidth;
                float top = labelPaddingTop + day * cellHeight;
                float right = left + cellWidth;
                float bottom = top + cellHeight;

                RectF cellRect = new RectF(left, top, right, bottom);

                // Vẽ ô
                canvas.drawRect(cellRect, cellPaint);

                // Vẽ đường viền lưới
                canvas.drawRect(cellRect, gridPaint);
            }
        }
    }

    /**
     * Xác định màu sắc cho giá trị dựa trên thang màu
     * @param value Giá trị cần xác định màu
     * @return Mã màu RGB
     */
    private int getColorForValue(int value) {
        if (maxValue <= 0) {
            return heatColors[0]; // Màu nền mặc định
        }

        if (value <= 0) {
            return heatColors[0];
        }

        // Phân loại theo tỉ lệ phần trăm
        float percentage = (float) value / maxValue;

        if (percentage <= 0.1f) {
            return heatColors[0];
        } else if (percentage <= 0.25f) {
            return heatColors[1];
        } else if (percentage <= 0.5f) {
            return heatColors[2];
        } else if (percentage <= 0.75f) {
            return heatColors[3];
        } else {
            return heatColors[4];
        }
    }

    /**
     * Chuyển đổi sp sang px
     */
    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * Chuyển đổi dp sang px
     */
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}