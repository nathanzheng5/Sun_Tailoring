package Lib;

import junit.framework.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created on 2017-03-24.
 */
public class TimeUtilsTests {

    @Test
    public void testParseDateToMillis() throws Exception {
        Calendar today = Calendar.getInstance();

        for (SimpleDateFormat formatter : TimeUtils.formatters) {
            String dateString = formatter.format(today.getTime());
            long millis = TimeUtils.parseDateToMillis(dateString);
            System.out.println("Testing date string '" + dateString + "' parsed to millis " + millis);

            Calendar testCalendar = Calendar.getInstance();
            testCalendar.setTimeInMillis(millis);
            Assert.assertEquals(today.get(Calendar.YEAR), testCalendar.get(Calendar.YEAR));
            Assert.assertEquals(today.get(Calendar.MONTH), testCalendar.get(Calendar.MONTH));
            Assert.assertEquals(today.get(Calendar.DATE), testCalendar.get(Calendar.DATE));
        }
    }
}
