package Invoice;

import AddressBook.ContactInfo;
import Utils.MathUtil;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Properties;

/**
 * Created on 2017-01-19.
 */
public class InvoicePrinter implements Printable {

    private static float BIG_FONT_SIZE = 16;
    private static float MEDIUM_FONT_SIZE = 10;
    private static float SMALL_FONT_SIZE = 8;
    private static int MAX_NUM_CHARS_PER_LINE = 35;

    private final Invoice invoice;

    private int x = 0;
    private int y = 0;

    public InvoicePrinter(Invoice invoice, Properties config) {
        this.invoice = invoice;

        String property = config.getProperty("invoice.printer.big.font.size");
        if (property != null) {
            BIG_FONT_SIZE = Float.parseFloat(property);
        }

        property = config.getProperty("invoice.printer.medium.font.size");
        if (property != null) {
            MEDIUM_FONT_SIZE = Float.parseFloat(property);
        }

        property = config.getProperty("invoice.printer.small.font.size");
        if (property != null) {
            SMALL_FONT_SIZE = Float.parseFloat(property);
        }

        property = config.getProperty("invoice.printer.max.num.chars.per.line");
        if (property != null) {
            MAX_NUM_CHARS_PER_LINE = Integer.parseInt(property);
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // title "Invoice 123456"
        String txt = "Invoice " + invoice.getInvoiceNum();
        y = (int) (BIG_FONT_SIZE + 1);
        g2d.setFont(g2d.getFont().deriveFont(BIG_FONT_SIZE));
        graphics.drawString(txt, x, y);

        // invoice date, due date, paid, customer name, phone
        txt = "Date: " + invoice.getFormattedInvoiceDate();
        g2d.setFont(g2d.getFont().deriveFont(SMALL_FONT_SIZE));
        y += (int) SMALL_FONT_SIZE + 5;
        graphics.drawString(txt, x, y);

        txt = "Pick up: " + invoice.getFormattedDueDate() + " - " + (invoice.isPaid() ? "Paid" : "Not Paid");
        y += (int) SMALL_FONT_SIZE + 1;
        graphics.drawString(txt, x, y);

        ContactInfo contactInfo = ContactInfo.parse(invoice.getCustomerAddress());
        txt = contactInfo.getName();
        if (contactInfo.getPhone() != null && !contactInfo.getPhone().isEmpty()) {
            txt += " - " + contactInfo.getPhone();
        }
        y += (int) SMALL_FONT_SIZE + 1;
        graphics.drawString(txt, x, y);

        // draw a dash
        drawDash(g2d, graphics);

        // item list
        for (ItemListEntry item : invoice.getItemList()) {
            if (item instanceof Item) {
                txt = ((Item) item).getName();
                if (txt.length() > MAX_NUM_CHARS_PER_LINE) {
                    txt = txt.substring(0, MAX_NUM_CHARS_PER_LINE - 3) + "...";
                }
                txt += " x " + ((Item) item).getQuantity();
                y += (int) SMALL_FONT_SIZE + 1;
                graphics.drawString(txt, x, y);
            }
        }

        // draw a dash
        drawDash(g2d, graphics);

        // subtotal, tax, credit, and total
        txt = "Subtotal: $ " + MathUtil.formatCurrency(invoice.getSubtotal());
        y += (int) SMALL_FONT_SIZE + 3;
        graphics.drawString(txt, x, y);

        txt = "Tax: $ " + MathUtil.formatCurrency(invoice.getTax());
        y += (int) SMALL_FONT_SIZE + 1;
        graphics.drawString(txt, x, y);

        if (invoice.getCredit() > 0) {
            txt = "Credit: $ " + MathUtil.formatCurrency(invoice.getCredit());
            y += (int) SMALL_FONT_SIZE + 1;
            graphics.drawString(txt, x, y);
        }

        g2d.setFont(g2d.getFont().deriveFont(MEDIUM_FONT_SIZE));
        txt = "Total: $ " + MathUtil.formatCurrency(invoice.getTotal());
        y += (int) MEDIUM_FONT_SIZE + 1;
        graphics.drawString(txt, x, y);

        // draw a dash
        drawDash(g2d, graphics);

        // Self info
        g2d.setFont(g2d.getFont().deriveFont(MEDIUM_FONT_SIZE));
        txt = "Sun Tailoring - 604-683-6817";
        y += (int) MEDIUM_FONT_SIZE + 1;
        graphics.drawString(txt, x, y);

        txt = "www.suntailoringvancouver.com";
        y += (int) MEDIUM_FONT_SIZE + 1;
        graphics.drawString(txt, x, y);

        return PAGE_EXISTS;
    }

    private void drawDash(Graphics2D g2d, Graphics graphics) {
        // draw a dash
        g2d.setFont(g2d.getFont().deriveFont(SMALL_FONT_SIZE));
        y += (int) SMALL_FONT_SIZE + 1;
        graphics.drawString("--------------------------------------------------", x, y);
    }
}
