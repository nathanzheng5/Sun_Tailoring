package Misc;

import Lib.StDatePicker;
import Lib.TimeUtils;
import org.junit.Test;

import javax.swing.*;

/**
 * Created on 2016-12-09.
 */
public class StDatePickerTest {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setSize(400, 300);

        StDatePicker stDatePicker = new StDatePicker(TimeUtils.formatters[0]);
        stDatePicker.addActionListener(e -> System.out.println(stDatePicker.getFormattedDate()));
        panel.add(stDatePicker.getComponent());

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    @Test
    public void testSetMonthDay() {
        StDatePicker stDatePicker = new StDatePicker(TimeUtils.formatters[0]);
        stDatePicker.setMonthDay("0228");
        String formattedDate = stDatePicker.getFormattedDate();
        System.out.println(formattedDate);
    }
}
