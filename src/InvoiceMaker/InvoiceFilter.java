package InvoiceMaker;

import Invoice.Invoice;

public interface InvoiceFilter {
    boolean test(Invoice invoice) throws Exception;
}
