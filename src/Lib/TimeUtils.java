package Lib;

import InvoiceMaker.StConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static final SimpleDateFormat[] formatters = {
            new SimpleDateFormat(StConfig.getInstance().getProperty("invoice.maker.date.format")),
            new SimpleDateFormat("yyyyMMdd"),
//            new SimpleDateFormat("yyyy-MM-dd"),   can't use this one!
            new SimpleDateFormat("yyMMdd")
    };

    public static long parseDateToMillis(String dateString) throws ParseException {
        Date date = null;

        // try all formatters until one of them works
        for (SimpleDateFormat sdf : formatters) {
            try {
                date = sdf.parse(dateString);
            } catch (ParseException ignore) {
            }

            if (date != null) {
                break;
            }
        }
        if (date == null) {
            throw new ParseException("Cannot parse date string " + dateString, 0);
        }

        return date.getTime();
    }

    public static String formatDateString(Calendar calendar) {
        return formatters[0].format(calendar.getTime());
    }

    public static String formatDateString(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        return formatDateString(calendar);
    }

    public static String formatDateTimeString(Calendar calendar) {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(calendar.getTime());
    }
}
