package InvoiceMaker;

import AddressBook.*;

import javax.swing.table.AbstractTableModel;

public class AddressBookSettingsTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private final AddressBook addressBook;

    public enum Column {
        Name,
        Phone,
        Email
    }

    public AddressBookSettingsTableModel(AddressBook addressBook) {
        this.addressBook = addressBook;
    }

    @Override
    public int getRowCount() {
        return addressBook.getNumEntries();
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
        ContactInfo contactInfo = addressBook.getEntry(rowIndex);
        switch (column) {
            case Name:
                return contactInfo.getName();
            case Phone:
                return contactInfo.getPhone();
            case Email:
                return contactInfo.getEmail();
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Column column = Column.values()[columnIndex];
        ContactInfo contactInfo = addressBook.getEntry(rowIndex);
        switch (column) {
            case Name:
                contactInfo.setName((String)aValue);
                break;
            case Phone:
                contactInfo.setPhone((String)aValue);
                break;
            case Email:
                contactInfo.setEmail((String)aValue);
                break;
            default:
                break;
        }
    }
}
