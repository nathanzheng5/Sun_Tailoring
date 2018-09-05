package Invoice;

import Lib.TimeUtils;

import java.io.*;
import java.util.List;

/**
 * Upgrades the .dat files in the Save directory, when {@link Invoice} object changes.
 */

public class InvoiceSaveFileUpgrade {

    public static void upgrade(String saveFolderPath) throws Exception {
        File saveFolder = new File(saveFolderPath);
        if (!saveFolder.exists()) {
            throw new Exception("Save folder \'" + saveFolderPath + "\' does not exist");
        }

        File[] files = saveFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".dat")) {
                    Invoice invoice;
                    System.out.print("Upgrading " + file.getName() + "...");
                    boolean isLatestVersion = false;
                    try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(file))) {
                        String invoiceNum = (String) is.readObject();
                        String hstNum = (String) is.readObject();
                        long invoiceDate = (Long) is.readObject();
                        long dueDate = (Long) is.readObject();
                        String clientAddress = (String) is.readObject();
                        String selfAddress = (String) is.readObject();
                        List<ItemListEntry> itemList = (List<ItemListEntry>) is.readObject();
                        double credit = is.readDouble();
                        boolean paid = is.readBoolean();
                        boolean done = false;
                        boolean pickedUp = is.readBoolean();
//                        isLatestVersion = true;

                        invoice = new Invoice(invoiceNum, hstNum, invoiceDate, dueDate, clientAddress, selfAddress,
                                itemList, credit, paid, done, pickedUp);
                    }

                    if (isLatestVersion) {
                        System.out.println(" already at latest version");
                    } else {
                        Invoice.write(file, invoice);
                        System.out.println(" done!");
                    }
                } else {
                    System.out.println("Skipped " + file.getName());
                }
            }
        }
    }
}
