package Invoice;

import InvoiceMaker.InvoiceFilter;
import InvoiceMaker.StLogger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Created on 2016-12-26.
 */
public class InvoiceStoreImpl implements InvoiceStore {

    private static final String SAVE_DIRECTORY_PROPERTY_NAME = "invoice.maker.save.directory";

    // todo: would be faster if invoice number is a numeric
    private final Map<String, Invoice> invoiceNumMap;

    private final Properties config;
    private final InvoiceFTPUploader ftpUploader;

    public InvoiceStoreImpl(Properties config) {

        invoiceNumMap = new HashMap<>(100000);
        this.config = config;
        this.ftpUploader = new InvoiceFTPUploader(config);

        // read all invoices from memory
        String saveDirectory = config.getProperty(SAVE_DIRECTORY_PROPERTY_NAME);
        File saveFolder = new File(saveDirectory);
        assert saveFolder.exists();

        File[] datFiles = saveFolder.listFiles();
        if (datFiles != null) {
            for (File datFile : datFiles) {
                if (datFile.isFile()) {
                    try {
                        Invoice invoice = Invoice.read(datFile);
                        invoiceNumMap.put(invoice.getInvoiceNum(), invoice);

                    } catch (Exception e) {
                        System.err.println("Failed to load invoice " + datFile.getName());
//                        e.printStackTrace();
                    }
                }
            }
        }
        StLogger.logAction("Invoice Store loaded " + invoiceNumMap.size() + " invoices");
    }

    @Nullable
    @Override
    public synchronized Invoice get(String invoiceNum) {
        Invoice invoice = invoiceNumMap.get(invoiceNum);
        if (invoice == null) {
            return null;
        }

        // return a copy to protect data integrity
        return Invoice.copy(invoice);
    }

    @Override
    public synchronized void save(Invoice invoice) throws Exception {
        // create a copy and put into map
        Invoice copy = Invoice.copy(invoice);
        invoiceNumMap.put(copy.getInvoiceNum(), copy);

        // also save to disk
        String saveDirectory = config.getProperty(SAVE_DIRECTORY_PROPERTY_NAME);
        File saveFile = new File(saveDirectory + copy.getInvoiceNum() + ".dat");
        Invoice.write(saveFile, copy);
    }

    @Override
    public void upload(Invoice invoice) throws Exception {
        String saveDirectory = config.getProperty(SAVE_DIRECTORY_PROPERTY_NAME);
        File datFile = new File(saveDirectory + invoice.getInvoiceNum() + ".dat");
        if (!datFile.exists()) {
            return;
        }

        try {
            ftpUploader.connect();
            ftpUploader.upload(datFile);
        } finally {
            ftpUploader.disconnect();
        }
    }

    @Override
    public String sync() throws Exception {
        // todo: consider moving this into a worker thread
        try {
            ftpUploader.connect();
        } catch (Exception e) {
            throw new Exception("Failed to connect to FTP", e);
        }

        int successCount = 0;
        LinkedHashMap<String, String> failedInvoices = new LinkedHashMap<>();

        String saveDirectory = config.getProperty(SAVE_DIRECTORY_PROPERTY_NAME);
        try {
            for (Invoice invoice : invoiceNumMap.values()) {
                try {
                    ftpUploader.upload(new File(saveDirectory + invoice.getInvoiceNum() + ".dat"));
                    successCount++;

                } catch (Exception e) {
                    String reason = e.getMessage();
                    if (reason.length() > 100) {
                        reason = reason.substring(0, 97) + "...";
                    }
                    failedInvoices.put(invoice.getInvoiceNum(), reason);
                }
            }
        } finally {
            ftpUploader.disconnect();
        }

        // build summary
        StringBuilder sb = new StringBuilder("Sync completed:\n");
        sb.append("Successfully uploaded ").append(successCount).append(" invoices\n");
        sb.append("Failed to upload the following invoices:\n");
        for (String failedInvoice : failedInvoices.keySet()) {
            sb.append(failedInvoice).append(": Reason: ").append(failedInvoices.get(failedInvoice)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public synchronized List<Invoice> filter(InvoiceFilter filter) {
        List<Invoice> retVal = new ArrayList<>(invoiceNumMap.size());

        for (Invoice invoice : invoiceNumMap.values()) {
            try {
                if (filter.test(invoice)) {
                    retVal.add(Invoice.copy(invoice));
                }
            } catch (Exception ignore) {}
        }

        return retVal;
    }


}
