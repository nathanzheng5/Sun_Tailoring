package Misc;

import Invoice.Invoice;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class NewInvoiceSerializationTest {

    @Test
    public void test() throws Exception {
        File file = new File("Saves/1809050.dat");
        Invoice invoice = Invoice.read(file);
        try (ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(new File("Saves/1809050Upgraded.dat")))) {
            invoice.write(fos);
        }
    }
}
