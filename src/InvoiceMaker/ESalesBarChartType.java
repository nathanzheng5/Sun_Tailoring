package InvoiceMaker;

/**
 * Created on 2017-03-03.
 */
public enum ESalesBarChartType {

    Last12Months("Last 12 Months");

    public final String name;
    ESalesBarChartType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
