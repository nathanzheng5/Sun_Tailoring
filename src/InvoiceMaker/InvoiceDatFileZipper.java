package InvoiceMaker;

import Invoice.Invoice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class InvoiceDatFileZipper {

    public static String zipFile(List<Invoice> invoices, String outputFileName) {
        List<String> datFileNames = invoices.stream()
                .map(invoice -> "Saves/" + invoice.getInvoiceNum() + ".dat")
                .collect(Collectors.toList());

        try {
            FileOutputStream fos = new FileOutputStream(outputFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            datFileNames.forEach(datFileName -> {
                try {
                    add(zos, datFileName);
                } catch (Exception e) {
                    System.err.println("Couldn't zip file " + datFileName);
                }
            });


            zos.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputFileName;
    }

    private static void add(ZipOutputStream zos, String fileName) throws Exception {
        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }

        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

}
