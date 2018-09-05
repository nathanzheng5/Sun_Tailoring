package InvoiceMaker;

import Invoice.Invoice;
import Utils.MathUtil;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class InvoiceMetaDataTableModel extends AbstractTableModel {

    private final List<Invoice> invoices;

    public enum Column {
        InvoiceNumber,
        InvoiceDate,
        DueDate,
        Customer,
        Items,
        Total,
        Paid,
        Done,
        PickedUp
    }

    public InvoiceMetaDataTableModel(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    public Invoice getInvoiceAtRow(int row) {
        return invoices.get(row);
    }

    @Override
    public int getRowCount() {
        return invoices.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Column column = Column.values()[columnIndex];
        Invoice invoice = invoices.get(rowIndex);
        switch (column) {
            case InvoiceNumber:
                return invoice.getInvoiceNum();
            case InvoiceDate:
                return invoice.getFormattedInvoiceDate();
            case DueDate:
                return invoice.getFormattedDueDate();
            case Customer:
                return invoice.getCustomerAddress().replaceAll("\n", " - ");
            case Items:
                return invoice.itemListSummary();
            case Total:
                return MathUtil.formatCurrency(invoice.getTotal());
            case Paid:
                return invoice.isPaid() ? "Paid" : "Not Paid";
            case Done:
                return invoice.isDone() ? "Done" : "Not Done";
            case PickedUp:
                return invoice.isPickedUp() ? "Picked Up" : "Not Picked Up";
            default:
                assert false;
                return "";
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        return (Column.values())[columnIndex].toString();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}
