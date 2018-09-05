package Misc;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static junit.framework.Assert.assertEquals;

public class DateTest {

    @Test
    public void testDateFormat() throws Exception {
        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        calendar1.setTime(sdf.parse("20101225"));

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(sdf.parse("20101225"));
        assertEquals(0, calendar1.compareTo(calendar2));

        calendar2.setTime(sdf.parse("20101226"));
        assertEquals(-1, calendar1.compareTo(calendar2));

        calendar2.setTime(sdf.parse("20101224"));
        assertEquals(1, calendar1.compareTo(calendar2));

    }
}
