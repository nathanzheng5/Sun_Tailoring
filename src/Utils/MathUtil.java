package Utils;

import java.text.DecimalFormat;

public class MathUtil {

    public static final DecimalFormat currencyFormatter = new DecimalFormat("0.00");

    public static String formatCurrency(double amount) {
        return currencyFormatter.format(amount);
    }

}
