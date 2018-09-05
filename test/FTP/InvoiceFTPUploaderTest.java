package FTP;

import Invoice.InvoiceFTPUploader;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Created on 2017-01-04.
 */
public class InvoiceFTPUploaderTest {
    private static final String CONFIG_FILE_PATH = "./test/FTP/FTPTestConfig.properties";

    private static Properties config;
    private static InvoiceFTPUploader ftpUploader;

    @BeforeClass
    public static void beforeClass() throws Exception{
        config = new Properties();
        try (FileReader reader = new FileReader(CONFIG_FILE_PATH)) {
            config.load(reader);
        }
        ftpUploader = new InvoiceFTPUploader(config);
    }

    @Before
    public void before() throws Exception {
        ftpUploader.connect();
    }

    @After
    public void after() throws Exception {
        ftpUploader.disconnect();
    }

    @Test
    public void testUploadInvoiceNormalCase() throws Exception {
        File datFile = new File("./test/FTP/1701050.dat");
        ftpUploader.upload(datFile);
    }

    @Test
    public void testUploadDatFileNotExist() throws Exception {
        File datFile = new File("./test/FTP/170105.dat");
        try {
            ftpUploader.upload(datFile);
            Assert.fail();
        } catch (Exception ignore) {
            // expected
        }
    }

    @Test
    public void testUploadNoPhoneNumber() throws Exception {
        File datFile = new File("./test/FTP/1701051.dat");
        try {
            ftpUploader.upload(datFile);
            Assert.fail();
        } catch (Exception ignore) {
            // expected
        }
    }
}
