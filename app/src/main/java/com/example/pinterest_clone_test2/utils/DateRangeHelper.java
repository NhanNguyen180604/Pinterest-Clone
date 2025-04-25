package com.example.pinterest_clone_test2.utils;

import com.example.pinterest_clone_test2.services.firebase.ReportDashboardService;

import java.util.Calendar;
import java.util.Date;

/**
 * Lớp tiện ích để tính toán khoảng thời gian cho các báo cáo
 */
public class DateRangeHelper {

    /**
     * Tính toán khoảng thời gian dựa trên TimeRange
     * Trả về mảng [startDate, endDate, previousStartDate, previousEndDate]
     */
    public static Date[] calculateDateRange(ReportDashboardService.TimeRange timeRange) {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime(); // Thời điểm hiện tại

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date startDate;
        Date previousStartDate;
        Date previousEndDate;

        switch (timeRange) {
            case LAST_WEEK:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                startDate = calendar.getTime();

                // Khoảng thời gian trước đó (tuần trước nữa)
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;

            case LAST_MONTH:
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();

                // Khoảng thời gian trước đó (tháng trước nữa)
                calendar.add(Calendar.MONTH, -1);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.MONTH, 1);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;

            case LAST_QUARTER:
                calendar.add(Calendar.MONTH, -3);
                startDate = calendar.getTime();

                // Khoảng thời gian trước đó (quý trước nữa)
                calendar.add(Calendar.MONTH, -3);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.MONTH, 3);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;

            case LAST_YEAR:
                calendar.add(Calendar.YEAR, -1);
                startDate = calendar.getTime();

                // Khoảng thời gian trước đó (năm trước nữa)
                calendar.add(Calendar.YEAR, -1);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.YEAR, 1);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;

            case TODAY:
            default:
                startDate = calendar.getTime(); // Đầu ngày hôm nay

                // Khoảng thời gian trước đó (hôm qua)
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;
        }

        return new Date[]{startDate, endDate, previousStartDate, previousEndDate};
    }
}