package AddressBook;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

public class AddressBookGUI extends JFrame {

    private JPanel topPanel;
    private JTable addressBookTable;
    private JTextField filterTextField;

    private Set<AddressSelectionListener> selectionListeners = new HashSet<>();

    public void addSelectionListener(AddressSelectionListener listener) {
        selectionListeners.add(listener);
    }

    private AddressBookTableModel addressBookTableModel;

    private ContactInfo selectedContact = null;

    public AddressBookGUI(final AddressBook addressBook) {
        setTitle("Address Book");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(topPanel);
        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                for (AddressSelectionListener listener : selectionListeners) {
                    listener.contactSelected(selectedContact);
                }
                selectionListeners.clear();
            }
        });

        initTable(addressBook);

        filterTextField.addActionListener(e -> {
            String filterText = filterTextField.getText().trim().toLowerCase();
            if (filterText.isEmpty()) {
                return;
            }

            List<ContactInfo> selectedContactInfo = new ArrayList<>();
            for (ContactInfo contactInfo : addressBook.getCustomerInfos()) {
                if (contactInfo.getName().toLowerCase().contains(filterText)) {
                    selectedContactInfo.add(contactInfo);
                    continue;
                }
                if (contactInfo.getPhone().contains(filterText)) {
                    selectedContactInfo.add(contactInfo);
                }
            }

            addressBookTableModel = new AddressBookTableModel(selectedContactInfo);
            addressBookTable.setModel(addressBookTableModel);
        });

        addressBookTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    if (row >= 0 && row < addressBookTable.getRowCount()) {
                        selectedContact = addressBookTableModel.get(row);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a row in the table first!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private void initTable(AddressBook addressBook) {
        addressBookTableModel = new AddressBookTableModel(addressBook.getCustomerInfos());
        addressBookTable.setModel(addressBookTableModel);
        // column width
        for (int i = 0; i < addressBookTableModel.getColumnCount(); i++) {
            AddressBookTableModel.Column column = AddressBookTableModel.Column.values()[i];
            switch (column) {
                case Name:
                    addressBookTable.getColumnModel().getColumn(i).setPreferredWidth(150);
                    break;
//                case Address:
//                    addressBookTable.getColumnModel().getColumn(i).setMinWidth(200);
                case Phone:
                    addressBookTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                    break;
                case Email:
                    addressBookTable.getColumnModel().getColumn(i).setPreferredWidth(200);
                    break;
                default:
                    break;
            }
        }
        // row height
//        for (int i = 0; i < addressBook.getNumEntries(); i++) {
//            addressBookTable.setRowHeight(80);
//        }
        // header
        TableCellRenderer headerRenderer = addressBookTable.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
        }
        // cell renderer
//        for (AddressBookTableModel.Column column : AddressBookTableModel.Column.values()) {
//            addressBookTable.getColumnModel().getColumn(column.ordinal()).setCellRenderer(new TextAreaRenderer());
//        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        TestAddressSelectionListener listener = new TestAddressSelectionListener();
        AddressBook addressBook = new AddressBook();
        AddressBookGUI gui = new AddressBookGUI(addressBook);
        gui.addSelectionListener(listener);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayoutManager(2, 4, new Insets(5, 5, 5, 5), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        topPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(800, -1), null, 0, false));
        addressBookTable = new JTable();
        scrollPane1.setViewportView(addressBookTable);
        filterTextField = new JTextField();
        filterTextField.setFont(new Font(filterTextField.getFont().getName(), filterTextField.getFont().getStyle(), 20));
        topPanel.add(filterTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setFont(new Font(label1.getFont().getName(), label1.getFont().getStyle(), 20));
        label1.setText("Search");
        topPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setFont(new Font(label2.getFont().getName(), label2.getFont().getStyle(), 20));
        label2.setText("Double click on a row to select");
        topPanel.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        topPanel.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

    private static class TestAddressSelectionListener implements AddressSelectionListener {
        @Override
        public void contactSelected(@Nullable ContactInfo contactInfo) {
            if (contactInfo != null) {
                System.out.println("Selected contact " + contactInfo.getName() + /*" - " + contactInfo.getAddress() + */" - " + contactInfo.getPhone() + " - " + contactInfo.getEmail());
            } else {
                System.out.println("None selected");
            }
        }
    }
}
