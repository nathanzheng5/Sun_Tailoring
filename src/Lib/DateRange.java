package Lib;

import java.text.ParseException;
import java.util.Calendar;

public class DateRange {

    private final Calendar start;
    private final Calendar end;

    public static DateRange getToday() {
        return new DateRange(Calendar.getInstance(), Calendar.getInstance());
    }

    public static DateRange getTodayPlusDays(int numDaysFromToday) {
        Calendar startCal = Calendar.getInstance();

        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.DATE, numDaysFromToday);

        return new DateRange(startCal, endCal);
    }

    public static DateRange getTodayMinusDays(int numDaysFromToday) {
        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.DATE, -1 * numDaysFromToday);

        Calendar endCal = Calendar.getInstance();

        return new DateRange(startCal, endCal);
    }

    private static Calendar dateStringToCalendar(String dateString) throws ParseException{
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(TimeUtils.parseDateToMillis(dateString));
        return startCal;
    }

    public DateRange(String startString, String endString) throws ParseException {
        this(dateStringToCalendar(startString), dateStringToCalendar(endString));
    }

    public DateRange(Calendar startCal, Calendar endCal) {
        start = startCal;
        end = endCal;

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
    }

    public boolean isInRange(long timeMillis) {
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.setTimeInMillis(timeMillis);
        return start.compareTo(testCalendar) <= 0 &&
                end.compareTo(testCalendar) >= 0;
    }

    public boolean isInRange(String dateString) throws ParseException {
        return isInRange(TimeUtils.parseDateToMillis(dateString));
    }

    @Override
    public String toString() {
        return TimeUtils.formatDateString(start) + " - " + TimeUtils.formatDateString(end);
    }
}
