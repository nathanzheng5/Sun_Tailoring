package Html;

import Invoice.*;
import Lib.TimeUtils;
import org.junit.Test;

import java.util.ArrayList;

public class InvoiceHtmlTest {

    @Test
    public void testCreateHtml() throws Exception {
        String clientAddress = "Harry\n1234 Baker St\nBurnaby, BC\nV2E2F3";
        String selfAddress = "Sun Tailoring\n555 W Hastings St\nVancouver, BC\nV1N2V2";
        long invoiceDate = TimeUtils.parseDateToMillis("Jun 1, 2014");
        long dueDate = TimeUtils.parseDateToMillis("Jun 7, 2014");
        Invoice invoice = new Invoice("I1234567", "987654321", invoiceDate, dueDate,
                clientAddress, selfAddress, new ArrayList<>(),
                0, true, false, true);

        invoice.addListEntry(new Group("David Beckham"));
        invoice.addListEntry(new Item("Pant - plain hem", 1, 18));

        InvoiceHtml.createHtml(invoice);
    }

}
