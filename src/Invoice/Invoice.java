package Invoice;

import Lib.TimeUtils;
import Utils.MathUtil;

import java.io.*;
import java.util.*;

public class Invoice implements Serializable {

    private static final long serialVersionUID = 2L;

    public static final Comparator<Invoice> DUE_DATE_COMPARATOR = (o1, o2) -> Long.compare(o1.getDueDate(), o2.getDueDate());
    public static final Comparator<Invoice> IN_DATE_COMPARATOR = (o1, o2) -> Long.compare(o1.getInvoiceDate(), o2.getInvoiceDate());

    private String invoiceNum;
    private String hstNum;
    private long invoiceDate;
    private long dueDate;
    private String customerAddress;
    private String selfAddress;
    private List<ItemListEntry> itemList;
    private double credit;
    private double subtotal;
    private double tax;
    private double total;
    private boolean paid;
    private boolean done;
    private boolean pickedUp;

    private final Set<InvoiceUpdateListener> updateListeners = new HashSet<>();
    public void addUpdateListener(InvoiceUpdateListener listener) {
        updateListeners.add(listener);
    }
    public void removeUpdateListener(InvoiceUpdateListener listener) {
        updateListeners.remove(listener);
    }

    /**
     * public constructor only used to create candidate invoice at start up.
     */
    public Invoice() {
        invoiceNum = "";
        hstNum = "";
        invoiceDate = System.currentTimeMillis();
        dueDate = System.currentTimeMillis();
        customerAddress = "";
        selfAddress = "";
        itemList = new ArrayList<>();
        credit = 0;
        paid = true;
        done = false;
        pickedUp = false;
    }

    public Invoice(String invoiceNum, String hstNum, long invoiceDate, long dueDate,
                   String customerAddress, String selfAddress,
                   List<ItemListEntry> inputList,
                   double credit,
                   boolean paid,
                   boolean done,
                   boolean pickedUp) {
        this.invoiceNum = invoiceNum;
        this.hstNum = hstNum;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.customerAddress = customerAddress;
        this.selfAddress = selfAddress;
        this.itemList = new ArrayList<>(inputList);
        this.credit = credit;
        this.paid = paid;
        this.done = done;
        this.pickedUp = pickedUp;
    }

    public static Invoice copy(Invoice invoice) {
        Invoice retVal = new Invoice(invoice.getInvoiceNum(),
                invoice.getHstNum(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                invoice.getCustomerAddress(),
                invoice.getSelfAddress(),
                invoice.getItemList(),
                invoice.credit,
                invoice.isPaid(),
                invoice.isDone(),
                invoice.isPickedUp());
        retVal.subtotal = invoice.subtotal;
        retVal.tax = invoice.tax;
        retVal.total = invoice.total;
        return retVal;
    }

    /**
     * This function must be called with care. See example in {@link InvoiceMaker.InvoiceMaker}
     * @param newInvoice new invoice.
     */
    public void update(Invoice newInvoice) {
        this.invoiceNum = newInvoice.getInvoiceNum();
        this.hstNum = newInvoice.getHstNum();
        this.invoiceDate = newInvoice.getInvoiceDate();
        this.dueDate = newInvoice.getDueDate();
        this.customerAddress = newInvoice.getCustomerAddress();
        this.selfAddress = newInvoice.getSelfAddress();
        this.paid = newInvoice.isPaid();
        this.done = newInvoice.isDone();
        this.pickedUp = newInvoice.isPickedUp();
        itemList.clear();
        itemList.addAll(newInvoice.getItemList());
        this.credit = newInvoice.credit;
        recalculate();
    }

    public List<ItemListEntry> getItemList() {
        return Collections.unmodifiableList(itemList);
    }

    public int getItemListSize() {
        return itemList.size();
    }

    public int addListEntry(ItemListEntry entry) {
        itemList.add(entry);
        recalculate();
        return itemList.size();
    }

    public int removeListEntry(int index) {
        itemList.remove(index);
        recalculate();
        return itemList.size();
    }

    public void recalculate() {
        subtotal = 0;
        itemList.stream().filter(item -> item instanceof Item).forEach(item -> subtotal += ((Item) item).getPrice());
        tax = subtotal * 0.05;
        total = subtotal + tax - credit;

        // update listeners
        for (InvoiceUpdateListener updateListener : updateListeners) {
            updateListener.invoiceRecalculated();
        }
    }

    public static Invoice read(File file) throws Exception {
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(file))) {
            String invoiceNum = (String) is.readObject();
            String hstNum = (String) is.readObject();
            long invoiceDate = (long) is.readObject();
            long dueDate = (long) is.readObject();
            String clientAddress = (String) is.readObject();
            String selfAddress = (String) is.readObject();
            List<ItemListEntry> itemList = (List<ItemListEntry>) is.readObject();
            double credit = is.readDouble();
            boolean paid = is.readBoolean();
            boolean done = is.readBoolean();
            boolean pickedUp = is.readBoolean();

            Invoice invoice = new Invoice(invoiceNum, hstNum, invoiceDate, dueDate, clientAddress, selfAddress,
                    itemList, credit, paid, done, pickedUp);
            invoice.recalculate();
            return invoice;
        }
    }

    public static void write(File file, Invoice invoice) throws Exception {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file))) {
            os.writeObject(invoice.getInvoiceNum());
            os.writeObject(invoice.getHstNum());
            os.writeObject(invoice.getInvoiceDate());
            os.writeObject(invoice.getDueDate());
            os.writeObject(invoice.getCustomerAddress());
            os.writeObject(invoice.getSelfAddress());
            os.writeObject(invoice.getItemList());
            os.writeDouble(invoice.getCredit());
            os.writeBoolean(invoice.isPaid());
            os.writeBoolean(invoice.isDone());
            os.writeBoolean(invoice.isPickedUp());
        }
    }

    public String getInvoiceNum() {
        return invoiceNum;
    }

    public String getHstNum() {
        return hstNum;
    }

    public long getInvoiceDate() {
        return invoiceDate;
    }

    public String getFormattedInvoiceDate() {
        return TimeUtils.formatDateString(invoiceDate);
    }

    public long getDueDate() {
        return dueDate;
    }

    public String getFormattedDueDate() {
        return TimeUtils.formatDateString(dueDate);
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getSelfAddress() {
        return selfAddress;
    }

    public void setInvoiceNum(String invoiceNum) {
        this.invoiceNum = invoiceNum;
    }

    public void setInvoiceDate(long invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public void setCustomerInfo(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
        recalculate();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTax() {
        return tax;
    }

    public double getTotal() {
        return total;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public boolean hasNoneDryCleanItem() {
        return itemList.stream()
                .filter(entry -> entry instanceof Item)
                .map(item -> (Item) item)
                .anyMatch(item -> {
                    String itemName = item.getName().toLowerCase();
                    return !itemName.startsWith("dry clean") && !itemName.startsWith("dryclean");
                });
    }

    public String summary() {
        String retVal = "Invoice number " + invoiceNum
                + " - " + customerAddress.replaceAll("\\n", " - ")
                + "\n\tIn " + getFormattedInvoiceDate()
                + ", due " + getFormattedDueDate()
                + (credit > 0 ? ("\nCredit " + MathUtil.formatCurrency(credit)) : "")
                + "\n\tTotal " + MathUtil.formatCurrency(total)
                + (paid ? ", PAID" : ", not paid")
                + (done ? ", DONE" : ", not done")
                + (pickedUp ? ", PICKED_UP" : ", not picked up")
                + "\n\tItem List: (" + itemList.size() + ")";

        for (ItemListEntry item : itemList) {
            if (item instanceof Item) {
                retVal += "\n\t\t" + ((Item) item).summary();
            }
        }
        return retVal;
    }

    public String itemListSummary() {
        String retVal = "";
        for (ItemListEntry item : itemList) {
            if (item instanceof Item) {
                retVal += ((Item) item).shortSummary() + "; ";
            }
        }
        retVal = retVal.trim();
        if (retVal.endsWith(";")) {
            retVal = retVal.substring(0, retVal.length() - 1);
        }
        return retVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Invoice invoice = (Invoice) o;

        return invoiceNum.equals(invoice.invoiceNum);

    }

    @Override
    public int hashCode() {
        return invoiceNum.hashCode();
    }
}
