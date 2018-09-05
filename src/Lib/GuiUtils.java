package Lib;

import javax.swing.*;
import java.text.ParseException;

public class GuiUtils {

    public static void popError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void popMsg(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String formatPhoneNumber(String phoneString) {
        String[] parseResult = parsePhone(phoneString);
        if (parseResult != null) {
            return parseResult[0] + "-" + parseResult[1] + "-" + parseResult[2];
        } else {
            String onlyNumbersString = phoneString.trim().replaceAll("[^0-9]", "");
            if (onlyNumbersString.isEmpty()) {
                return "";
            }
            System.err.println("Invalid phone number " + phoneString);
            return phoneString;
        }
    }

    public static String[] parsePhone(String phoneString) {
        String first3;
        String second3;
        String last4;

        String onlyNumbersString = phoneString.trim().replaceAll("[^0-9]", "");
        if (onlyNumbersString.isEmpty()) {
            return null;

        } else if (onlyNumbersString.length() == 7) {
            first3 = "604";
            second3 = onlyNumbersString.substring(0, 3);
            last4 = onlyNumbersString.substring(3, 7);

        } else if (onlyNumbersString.length() == 10) {
            first3 = onlyNumbersString.substring(0, 3);
            second3 = onlyNumbersString.substring(3, 6);
            last4 = onlyNumbersString.substring(6, 10);

        } else if (onlyNumbersString.length() == 11) {
            first3 = onlyNumbersString.substring(1, 4);
            second3 = onlyNumbersString.substring(4, 7);
            last4 = onlyNumbersString.substring(7, 11);

        } else {
            System.err.println("Invalid phone number " + phoneString);
            return null;
        }

        return new String[] {first3, second3, last4};
    }
}
