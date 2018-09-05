package InvoiceMaker;

import Invoice.Group;
import Invoice.Invoice;
import Invoice.Item;
import Invoice.ItemListEntry;
import Lib.GuiUtils;
import Utils.MathUtil;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

import static Lib.GuiUtils.popError;

public class ItemListTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final Invoice invoice;

    public enum Column {
        Description,
        Quantity,
        UnitPrice,
        Price
    }

    public ItemListTableModel(Invoice invoice) {
        this.invoice = invoice;
    }

    public boolean isRowGroup(int rowIndex) {
        return invoice.getItemList().get(rowIndex) instanceof Group;
    }

    public boolean isZeroPriceItem(int rowIndex) {
        ItemListEntry entry = invoice.getItemList().get(rowIndex);
        return !(entry instanceof Group) && ((Item) entry).getPrice() <= 0.0;
    }

    public List<Integer> searchGroup(String token) {
        List<Integer> result = new ArrayList<>();
        String tokenTrimLower = token.trim().toLowerCase();

        for (int i = 0; i < invoice.getItemList().size(); i++) {
            ItemListEntry entry = invoice.getItemList().get(i);
            if (entry instanceof Group) {
                String groupName = ((Group) entry).getName().trim().toLowerCase();
                if (groupName.contains(tokenTrimLower)) {
                    result.add(i);
                }
            }
        }

        return result;
    }

    @Override
    public int getRowCount() {
        return invoice.getItemListSize();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
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
        if (rowIndex >= invoice.getItemList().size()) {
            GuiUtils.popError("Unexpected error. Shut down and restart the program. Contact Nathan");
            System.err.println("ItemListTableModel.isCellEditable rowIndex " + rowIndex + " >= invoice " + invoice.getInvoiceNum() + " item list size " + invoice.getItemList().size());
            assert false;
            return false;
        }

        Column column = Column.values()[columnIndex];
        ItemListEntry item = invoice.getItemList().get(rowIndex);
        if (item instanceof Group) {
            return column == Column.Description;
        } else if (item instanceof Item) {
            return column != Column.Price;
        }
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= invoice.getItemList().size()) {
            GuiUtils.popError("Unexpected error. Shut down and restart the program. Contact Nathan");
            System.err.println("ItemListTableModel.getValueAt rowIndex " + rowIndex + " >= invoice " + invoice.getInvoiceNum() + " item list size " + invoice.getItemList().size());
            assert false;
            return false;
        }

        Column column = Column.values()[columnIndex];
        ItemListEntry item = invoice.getItemList().get(rowIndex);
        switch (column) {
            case Description:
                if (item instanceof Group) {
                    return ((Group) item).getName();
                } else if (item instanceof Item) {
                    return "    " + ((Item) item).getName().trim();
                }
                break;
            case Quantity:
                if (item instanceof Group) {
                    return "";
                } else if (item instanceof Item) {
                    return Integer.toString(((Item) item).getQuantity());
                }
                break;
            case UnitPrice:
                if (item instanceof Group) {
                    return "";
                } else if (item instanceof Item) {
                    return MathUtil.formatCurrency(((Item) item).getUnitPrice());
                }
                break;
            case Price:
                if (item instanceof Group) {
                    return "";
                } else if (item instanceof Item) {
                    return MathUtil.formatCurrency(((Item) item).getPrice());
                }
                break;
            default:
                break;
        }
        assert false;
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex >= invoice.getItemList().size()) {
            GuiUtils.popError("Unexpected error. Shut down and restart the program. Contact Nathan");
            System.err.println("ItemListTableModel.setValueAt rowIndex " + rowIndex + " >= invoice " + invoice.getInvoiceNum() + " item list size " + invoice.getItemList().size());
            assert false;
            return;
        }

        Column column = Column.values()[columnIndex];
        ItemListEntry item = invoice.getItemList().get(rowIndex);
        switch (column) {
            case Description:
                if (item instanceof Group) {
                    ((Group) item).setName((String) aValue);
                } else if (item instanceof Item) {
                    ((Item) item).setName(((String) aValue).trim());
                }
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            case Quantity:
                assert item instanceof Item;
                try {
                    int quantity = Integer.parseInt((String) aValue);
                    ((Item) item).setQuantity(quantity);
                    fireTableRowsUpdated(rowIndex, rowIndex);
                } catch (NumberFormatException e) {
                    popError("Enter an number for quantity");
                }

                break;
            case UnitPrice:
                assert item instanceof Item;
                try {
                    double unitPrice = Double.parseDouble((String) aValue);
                    ((Item) item).setUnitPrice(unitPrice);
                    fireTableRowsUpdated(rowIndex, rowIndex);
                } catch (NumberFormatException e) {
                    popError("Enter a number for unit price");
                }
                break;
            default:
                assert false;
                break;
        }
        invoice.recalculate();
    }
}
