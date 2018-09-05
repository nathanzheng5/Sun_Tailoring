package InvoiceMaker;

import Invoice.Item;
import Invoice.QuickItemList;
import Utils.MathUtil;

import javax.swing.table.AbstractTableModel;

public class QuickItemsSettingsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final QuickItemList quickItemList;

    public enum Column {
        Name,
        UnitPrice
    }

    public QuickItemsSettingsTableModel(QuickItemList quickItemList) {
        this.quickItemList = quickItemList;
    }

    @Override
    public int getRowCount() {
        return quickItemList.getList().size();
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
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Column column = Column.values()[columnIndex];
        Item item = quickItemList.getList().get(rowIndex);
        switch (column) {
            case Name:
                return item.getName();
            case UnitPrice:
                return MathUtil.formatCurrency(item.getUnitPrice());
            default:
                return "";
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Column column = Column.values()[columnIndex];
        Item item = quickItemList.getList().get(rowIndex);
        try {
            switch (column) {
                case Name:
                    item.setName((String)aValue);
                    break;
                case UnitPrice:
                    item.setUnitPrice(Double.parseDouble((String) aValue));
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
