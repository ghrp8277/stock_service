package com.example.stockservice.util;

import com.example.stockservice.constants.TimeframeConstants;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static String calculateStartDate(String timeframe) {
        LocalDate startDate = LocalDate.now();

        switch (timeframe) {
            case TimeframeConstants.ONE_MONTH:
                startDate = startDate.minusMonths(1);
                break;
            case TimeframeConstants.ONE_YEAR:
                startDate = startDate.minusYears(1);
                break;
            case TimeframeConstants.THREE_YEARS:
                startDate = startDate.minusYears(3);
                break;
            case TimeframeConstants.FIVE_YEARS:
                startDate = startDate.minusYears(5);
                break;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return startDate.format(formatter);
    }
}
