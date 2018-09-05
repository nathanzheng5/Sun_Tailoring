package Misc;

import Invoice.Invoice;
import Invoice.Item;
import Invoice.InvoicePrinter;
import Lib.TimeUtils;
import org.junit.Test;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created on 2017-01-16.
 */
public class InvoicePrinterTest {
    @Test
    public void testPrint() throws Exception {
        long invoiceDate = TimeUtils.parseDateToMillis("Jan 01, 2017");
        long dueDate = TimeUtils.parseDateToMillis("Jan 07, 2017");
        Invoice invoice = new Invoice("1701011", "hst", invoiceDate, dueDate,
                "Nathan Zheng\n604-657-7930", "", new ArrayList<>(), 0, true, false, true);
        invoice.addListEntry(new Item("dry clean coat", 1, 8));
        invoice.addListEntry(new Item("dry clean shirt", 1, 4));
        invoice.addListEntry(new Item("very long long long long long long long long long long long long long long long", 1, 4));

        InvoicePrinter invoicePrinter = new InvoicePrinter(invoice, new Properties());

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(invoicePrinter);

        PrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
        set.add(OrientationRequested.PORTRAIT);
        set.add(MediaSizeName.INVOICE);
        try {
            job.print(set);
        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }
}
