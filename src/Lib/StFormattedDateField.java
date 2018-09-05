package Lib;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class StFormattedDateField extends JTextField implements DateSelector {

    protected final SimpleDateFormat sdf;

    protected int year;
    protected int month;
    protected int day;

    public StFormattedDateField(String datePattern) {
        sdf = new SimpleDateFormat(datePattern);
        Calendar cal = Calendar.getInstance();
        day = cal.get(Calendar.DATE);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        setText(getFormattedDate());
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getFormattedDate() {
        Calendar cal = new GregorianCalendar(year, month, day);
        return sdf.format(cal.getTime());
    }

    @Override
    public void setDate(long dateInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateInMillis);
        day = cal.get(Calendar.DATE);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        setText(sdf.format(cal.getTime()));
    }

    public boolean isValid(String dateString) {
        try {
            sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
