package AddressBook;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class AddressBookTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final List<ContactInfo> addresses;

    public enum Column {
        Name,
        Phone,
        Email
    }

    public AddressBookTableModel(List<ContactInfo> addresses) {
        this.addresses = addresses;
    }

    public ContactInfo get(int row) {
        assert row >= 0 && row < addresses.size();
        return addresses.get(row);
    }

    @Override
    public int getRowCount() {
        return addresses.size();
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
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Column column = Column.values()[columnIndex];
        ContactInfo contactInfo = addresses.get(rowIndex);
        switch (column) {
            case Name:
                return contactInfo.getName();
            case Phone:
                return contactInfo.getPhone();
            case Email:
                return contactInfo.getEmail();
            default:
                break;
        }
        assert false;
        return null;
    }
}
