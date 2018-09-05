package Misc;

import Lib.DateRange;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DateRangeTest {

    @Test
    public void testDateRange() throws Exception {
        DateRange dateRange = new DateRange("20101225", "20101226");
        assertTrue(dateRange.isInRange("20101225"));
        assertTrue(dateRange.isInRange("20101226"));
        assertFalse(dateRange.isInRange("20101224"));
        assertFalse(dateRange.isInRange("20101227"));

        dateRange = new DateRange("20101225", "20101230");
        assertTrue(dateRange.isInRange("20101227"));
        assertFalse(dateRange.isInRange("20101231"));

        dateRange = DateRange.getToday();
        // todo: replace with today's date
        assertTrue(dateRange.isInRange("20170629"));

        dateRange = DateRange.getTodayPlusDays(3);
        // todo: replace with 3 days from today
        assertTrue(dateRange.isInRange("20170629"));
        assertTrue(dateRange.isInRange("20170630"));
        assertTrue(dateRange.isInRange("20170701"));
        assertTrue(dateRange.isInRange("20170702"));
        assertFalse(dateRange.isInRange("20170703"));
    }
}
