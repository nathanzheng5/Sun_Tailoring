package Misc;

import Lib.StFormattedDateField;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DateSelectorTest {
    @Test
    public void testSetDate() {
        String datePattern = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        StFormattedDateField dateField = new StFormattedDateField(datePattern);
        Calendar calendar = Calendar.getInstance();
        assertEquals(sdf.format(calendar.getTime()), dateField.getFormattedDate());

        try {
            dateField.setDate(sdf.parse("Aug 15, 2016").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals("Aug 15, 2016", dateField.getFormattedDate());

        // running the following will prompt a format error
//        datePicker.setDate("2016-08-15");
    }
}
