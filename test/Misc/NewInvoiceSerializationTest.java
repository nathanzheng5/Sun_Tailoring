package Misc;

import Invoice.Invoice;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewInvoiceSerializationTest {

    @Test
    public void test() throws Exception {
        Files.walk(Paths.get("Saves")).map(Path::toFile).filter(File::isFile).forEach(file -> {
            try {
                Invoice invoice = Invoice.read(file);
                try (ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(
                        new File("SavesUpgrade/" + invoice.getInvoiceNum() + ".dat")))) {
                    invoice.write(fos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } );
    }
}
