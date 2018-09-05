package Lib;

import org.jdatepicker.DateModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

public class StDatePicker implements DateSelector {

    private final JDatePickerImpl datePicker;
    private final SimpleDateFormat dateFormatter;

    public StDatePicker(SimpleDateFormat dateFormatter) {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);

        this.dateFormatter = dateFormatter;
        DateLabelFormatter dateLabelFormatter = new DateLabelFormatter(dateFormatter);

        datePicker = new JDatePickerImpl(datePanel, dateLabelFormatter);
    }

    public void setVisible(boolean visible) {
        datePicker.setVisible(visible);
    }

    @Override
    public JDatePickerImpl getComponent() {
        return datePicker;
    }

    @Override
    public void addActionListener(ActionListener actionListener) {
        datePicker.addActionListener(actionListener);
    }

    @Override
    public void removeActionListener(ActionListener actionListener) {
        datePicker.removeActionListener(actionListener);
    }

    @Override
    public String getFormattedDate() {
        DateModel<?> model = datePicker.getModel();
        int year = model.getYear();
        int month = model.getMonth();
        int day = model.getDay();

        Calendar calendar = new GregorianCalendar(year, month, day);

        return dateFormatter.format(calendar.getTime());
    }

    @Override
    public void setDate(long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(dateInMillis));
        updateDateModel(calendar);
    }

    /**
     * @param monthDayString must be of format "0813"
     */
    public void setMonthDay(String monthDayString) {
        try {
            if (monthDayString.length() != 4) {
                throw new ParseException("must be of format 0813", 0);
            }
            int month = Integer.parseInt(monthDayString.substring(0, 2)) - 1;
            int day = Integer.parseInt(monthDayString.substring(2, 4));

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, day);
            updateDateModel(calendar);

        } catch (ParseException e) {
            GuiUtils.popError("Failed to parse date string \'" + monthDayString + "\'. Must be formatted as 0813");
        }
    }

    private void updateDateModel(Calendar calendar) {
        DateModel<?> model = datePicker.getModel();
        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        model.setDay(day);
        model.setMonth(month);
        model.setYear(year);
        datePicker.getJFormattedTextField().setText(getFormattedDate());
    }

    private class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

        private final SimpleDateFormat dateFormatter;

        public DateLabelFormatter(SimpleDateFormat dateFormatter) {
            this.dateFormatter = dateFormatter;
        }

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                if (value instanceof Date) {
                    return dateFormatter.format((Date) value);
                } else {
                    Calendar cal = (Calendar) value;
                    return dateFormatter.format(cal.getTime());
                }
            }

            return "";
        }

    }
}
