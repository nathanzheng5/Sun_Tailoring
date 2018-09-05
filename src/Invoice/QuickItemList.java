package Invoice;

import Lib.Persistable;
import Utils.MathUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static InvoiceMaker.StLogger.logAction;

public class QuickItemList implements Persistable {

    private static final long serialVersionUID = 1L;

    private static final String CSV_FILE_HEADER = "Name,Unit Price,";
    private static final Pattern LINE_PATTERN = Pattern.compile("(.*),(.*),");

    private final List<Item> quickItems;

    private final File file;

    public QuickItemList(File file) {
        this.file = file;
        quickItems = new ArrayList<>();
        load();
    }

    @Override
    public boolean save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(CSV_FILE_HEADER);
            for (Item item : quickItems) {
                pw.print(item.getName() + ",");
                pw.println(MathUtil.formatCurrency(item.getUnitPrice()) + ",");
            }

            logAction("Saved quick items to " + file.getAbsolutePath());
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean load() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line = br.readLine().trim();
            if (line.equals(CSV_FILE_HEADER)) {
                line = br.readLine();
                while (line != null && !line.trim().isEmpty()) {
                    Matcher matcher = LINE_PATTERN.matcher(line.trim());
                    if (matcher.matches()) {
                        String name = matcher.group(1).trim();
                        int quantity = 1;
                        double unitPrice = Double.parseDouble(matcher.group(2).trim());
                        Item item = new Item(name, quantity, unitPrice);
                        quickItems.add(item);
                    }
                    line = br.readLine();
                }
            }

            logAction("Loaded " + file.getAbsolutePath());
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getList() {
        return Collections.unmodifiableList(quickItems);
    }

    public void addItem(Item item) {
        quickItems.add(item);
    }

    public void removeEntry(int i) {
        quickItems.remove(i);
    }

    public void sort() {
        Collections.sort(quickItems, (o1, o2) -> o1.getName().compareTo(o2.getName()));
    }
}
