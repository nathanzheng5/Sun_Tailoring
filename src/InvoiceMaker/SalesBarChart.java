package InvoiceMaker;

import Invoice.Invoice;
import Invoice.InvoiceStore;
import Lib.DateRange;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created on 2017-03-02.
 */
public class SalesBarChart extends JFrame {

    public SalesBarChart(InvoiceStore invoiceStore, ESalesBarChartType type) throws Exception {
        super();

        setTitle("Sales Bar Chart");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        CategoryDataset dataSet = createDataSet(invoiceStore, type);

        JFreeChart barChart = ChartFactory.createBarChart(type.name,
                "Time",
                "Sales ($)",
                dataSet,
                PlotOrientation.VERTICAL,
                true,       // legend
                true,       // tooltip
                false);     // urls

        // rotate the x-axis by 45 degrees
        barChart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ChartPanel chartPanel = new ChartPanel(barChart);
        setContentPane(chartPanel);
        // set full screen
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private CategoryDataset createDataSet(InvoiceStore invoiceStore, ESalesBarChartType type) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<String> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();

        for (int iMonth = 0; iMonth < 12; iMonth++) {
            Calendar monthBeginning = Calendar.getInstance();
            monthBeginning.add(Calendar.MONTH, iMonth * -1);
            monthBeginning.set(Calendar.DAY_OF_MONTH, monthBeginning.getActualMinimum(Calendar.DAY_OF_MONTH));

            Calendar monthEnd = Calendar.getInstance();
            monthEnd.add(Calendar.MONTH, iMonth * -1);
            monthEnd.set(Calendar.DAY_OF_MONTH, monthBeginning.getActualMaximum(Calendar.DAY_OF_MONTH));

            DateRange dateRange = new DateRange(monthBeginning, monthEnd);

            List<Invoice> filteredInvoices = invoiceStore.filter(invoice -> dateRange.isInRange(invoice.getInvoiceDate()));
            double sum = 0;
            for (Invoice filteredInvoice : filteredInvoices) {
                sum += filteredInvoice.getTotal();
            }

            String monthYear = monthBeginning.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
            monthYear += " " + monthBeginning.get(Calendar.YEAR);

            xValues.add(monthYear);
            yValues.add(sum);
        }

        // add in reverse order
        for (int i = 0; i < xValues.size(); i++) {
            String xValue = xValues.get(xValues.size() - 1 - i);
            double yValue = yValues.get(yValues.size() - 1 - i);
            dataset.addValue(yValue, "Sales", xValue);
        }

        return dataset;
    }

}
