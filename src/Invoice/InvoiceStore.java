package Invoice;

import InvoiceMaker.InvoiceFilter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Created on 2016-12-26.
 */

/**
 * Stores invoices. This storage has the following responsibilities: <ul>
 *     <li>serves as the invoice getter throughout the whole application</li>
 *     <li>takes care of saving invoices into the disk, and possibly FTP server</li>
 * </ul>
 * This class should be the only class that opens / saves the invoice .dat file
 */
public interface InvoiceStore {

    /**
     * Persists the invoice into disk or FTP.
     * @param invoice the invoice to be stored.
     * @throws Exception when error occurs.
     */
    void save(Invoice invoice) throws Exception;

    /**
     * Uploads the invoice HTML output file to FTP.
     * @param invoice the invoice to be uploaded.
     * @throws Exception when error occurs.
     */
    void upload(Invoice invoice) throws Exception;

    /**
     * Syncs all invoice HTML output files to FTP.
     * @throws Exception when error occurs.
     * @return a summary.
     */
    String sync() throws Exception;

    /**
     * @param invoiceNum the key to the invoice.
     * @return the invoice with the specified invoice number, or null if the invoice number does not exist.
     */
    @Nullable Invoice get(String invoiceNum);

    /**
     * @param filter applied filter.
     * @return the invoices matching the filter.
     */
    List<Invoice> filter(InvoiceFilter filter);

}
