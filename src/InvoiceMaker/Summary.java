package InvoiceMaker;

import Invoice.Invoice;
import Invoice.InvoiceStore;
import Lib.DateRange;
import Lib.TimeUtils;
import Utils.MathUtil;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class Summary {

    private List<Invoice> inToday;
    private List<Invoice> dueTomorrow;
    private List<Invoice> dueIn3Days;
    private List<Invoice> dueIn7DaysUndone;
    private String zipFileName;

    private int inTodayNumInvoices;
    private int inTodayNumItems;
    private double inTodayTotal;
    private int dueTmrNumInvoices;
    private int dueTmrNumItems;
    private int due3DaysNumInvoices;
    private int due3DaysNumItems;


    public Summary(InvoiceStore invoiceStore) {
        // in today
        DateRange todayRange = DateRange.getToday();
        inToday = invoiceStore.filter(invoice -> todayRange.isInRange(invoice.getInvoiceDate()));

        inTodayNumInvoices = inToday.size();
        inTodayNumItems = 0;
        inTodayTotal = 0;
        inToday.stream().forEach(invoice -> {
            inTodayNumItems += invoice.getItemListSize();
            inTodayTotal += invoice.getTotal();
        });

        // due tomorrow
        DateRange tomorrowRange = DateRange.getTodayPlusDays(1);
        dueTomorrow = invoiceStore.filter(invoice -> tomorrowRange.isInRange(invoice.getDueDate())
                && invoice.hasNoneDryCleanItem());

        // due in 3 days
        DateRange next3DaysRange = DateRange.getTodayPlusDays(3);
        dueIn3Days = invoiceStore.filter(invoice -> next3DaysRange.isInRange(invoice.getDueDate())
                && invoice.hasNoneDryCleanItem());
        dueIn3Days.removeAll(dueTomorrow);
        dueIn3Days.sort(Invoice.DUE_DATE_COMPARATOR);

        // due in 7 days undone
        DateRange next7Days = DateRange.getTodayPlusDays(7);
        dueIn7DaysUndone = invoiceStore.filter(invoice -> next7Days.isInRange(invoice.getDueDate())
                && invoice.hasNoneDryCleanItem() && !invoice.isDone());
        dueIn7DaysUndone.sort(Invoice.DUE_DATE_COMPARATOR);
        zipFileName = "invoices_" + System.currentTimeMillis() + ".zip";

        // remove all Done ones
        dueTomorrow = dueTomorrow.stream().filter(invoice -> !invoice.isDone()).collect(Collectors.toList());
        dueIn3Days = dueIn3Days.stream().filter(invoice -> !invoice.isDone()).collect(Collectors.toList());

        dueTmrNumInvoices = dueTomorrow.size();
        dueTmrNumItems = 0;
        dueTomorrow.stream().forEach(invoice -> dueTmrNumItems += invoice.getItemListSize());

        due3DaysNumInvoices = dueIn3Days.size();
        due3DaysNumItems = 0;
        dueIn3Days.stream().forEach(invoice -> due3DaysNumItems += invoice.getItemListSize());
    }

    public int getDue3DaysNumInvoices() {
        return due3DaysNumInvoices;
    }

    public int getDue3DaysNumItems() {
        return due3DaysNumItems;
    }

    public int getDueTmrNumInvoices() {
        return dueTmrNumInvoices;
    }

    public int getDueTmrNumItems() {
        return dueTmrNumItems;
    }

    public int getInTodayNumInvoices() {
        return inTodayNumInvoices;
    }

    public int getInTodayNumItems() {
        return inTodayNumItems;
    }

    public double getInTodayTotal() {
        return inTodayTotal;
    }

    public void zip7DayUndone() {
        InvoiceDatFileZipper.zipFile(dueIn7DaysUndone, zipFileName);
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public String getSummaryEmail() {
        // build summary
        String separator = "\n=================================\n";
        String title = "Sun Tailoring Summary " + TimeUtils.formatDateString(Calendar.getInstance());
        String summary = title + "\n";
        summary += separator;
        summary += inTodayNumInvoices + " invoices (" + inTodayNumItems + " items) created today. $" + MathUtil.formatCurrency(inTodayTotal )+ "\n";
        summary += dueTmrNumInvoices + " invoices (" + dueTmrNumItems + " items) due tomorrow that's not Done.\n";
        summary += due3DaysNumInvoices + " invoices (" + due3DaysNumItems + " items) due in 3 days that's not Done.\n";


        summary += separator;
        summary += inTodayNumInvoices + " invoices (" + inTodayNumItems + " items) created today.\n";
        for (Invoice invoice : inToday) {
            summary += invoice.summary() + "\n\n";
        }

        summary += separator;
        summary += dueTmrNumInvoices + " invoices (" + dueTmrNumItems + " items) due tomorrow that's not Done.\n";
        for (Invoice invoice : dueTomorrow) {
            summary += invoice.summary() + "\n\n";
        }

        summary += separator;
        summary += due3DaysNumInvoices + " invoices (" + due3DaysNumItems + " items) due in 3 days that's not Done.\n";
        for (Invoice invoice : dueIn3Days) {
            summary += invoice.summary() + "\n\n";
        }
        return summary;
    }

}
