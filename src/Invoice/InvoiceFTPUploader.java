package Invoice;

import AddressBook.ContactInfo;
import InvoiceMaker.StConfig;
import InvoiceMaker.StLogger;
import Lib.GuiUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created on 2017-01-04.
 */
public class InvoiceFTPUploader {
    private static final String SERVER = "suntailoring.ca";
    private static final int PORT = 21;
    private static final String USER = "sunt6277";
    // todo: do not store password
    private static final String PASSWORD = StConfig.getInstance().getProperty("st.gmail.password");

    private static final String OUTPUT_DIRECTORY_PROPERTY_NAME = "invoice.maker.output.directory";
    private static final String REMOTE_OUTPUT_DIR = "/invoices/";

    private final FTPClient ftpClient;
    private final Properties config;

    public InvoiceFTPUploader(Properties config) {
        ftpClient = new FTPClient();
        this.config = config;
    }

    public void connect() throws IOException {
        StLogger.logAction("FTP log in...");

        ftpClient.connect(SERVER, PORT);
        ftpClient.login(USER, PASSWORD);
        ftpClient.enterLocalPassiveMode();

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    }

    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            StLogger.logAction("FTP log out...");

            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

    /**
     * Uploads an invoice onto FTP.
     * @param datFile saved invoice .dat file
     * @throws IOException if error occurs.
     * @throws UnsupportedOperationException if error occurs.
     */
    public void upload(File datFile) throws IOException, UnsupportedOperationException {
        String error;
        if (!datFile.exists()) {
            error = datFile.getName() + " - .dat file does not exist";
            StLogger.logAction(error);
            throw new UnsupportedOperationException(error);
        }

        Invoice invoice;
        try {
            invoice = Invoice.read(datFile);
        } catch (Exception e) {
            error = "Cannot open invoice dat file " + datFile.getName();
            StLogger.logAction(error);
            throw new IOException(error);
        }

        ContactInfo contactInfo = ContactInfo.parse(invoice.getCustomerAddress());
        String formattedPhone = GuiUtils.formatPhoneNumber(contactInfo.getPhone());

        // skip if phone number is not entered or is not formatted properly
        String[] phoneStrings = formattedPhone.split("-");
        if (phoneStrings.length < 3) {
            error = invoice.getInvoiceNum() + " customer phone number is not filled";
            StLogger.logAction(error);
            throw new UnsupportedOperationException(error);
        }

        String last4Digits = phoneStrings[2];
        assert last4Digits.length() == 4;

        String outputDirectory = config.getProperty(OUTPUT_DIRECTORY_PROPERTY_NAME);
        String htmlFileName = outputDirectory + invoice.getInvoiceNum() + ".html";
        File htmlFile = new File(htmlFileName);
        if (!htmlFile.exists()) {
            error = invoice.getInvoiceNum() + " output html file does not exist";
            StLogger.logAction(error);
            throw new IOException(error);
        }

        try (InputStream is = new FileInputStream(htmlFile)) {
            String remoteFileName = REMOTE_OUTPUT_DIR + invoice.getInvoiceNum() + "_" + last4Digits + ".html";
            System.out.print("Uploading " + remoteFileName + "...");

            boolean done = ftpClient.storeFile(remoteFileName, is);
            if (done) {
                System.out.print("done!\n");
            } else {
                error = "Failed to upload " + remoteFileName + " to FTP";
                StLogger.logAction(error);
                throw new IOException(error);
            }

        } catch (IOException e) {
            error = "Failed to upload " + invoice.getInvoiceNum() + " to FTP";
            StLogger.logAction(error);
            throw new IOException(error, e);
        }
    }
}
