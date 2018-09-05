package InvoiceMaker;

import AddressBook.*;
import Html.InvoiceHtml;
import Invoice.*;
import Lib.GuiUtils;
import Lib.SimpleAction;
import Lib.StDatePicker;
import Lib.TimeUtils;
import Mail.GmailSender;
import Mail.MailEventListener;
import Mail.MailException;
import Mail.StMailGUI;
import Utils.MathUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
import java.util.regex.Matcher;

import static InvoiceMaker.StLogger.logAction;
import static InvoiceMaker.StLogger.logDetailedAction;
import static Lib.GuiUtils.popError;

public class InvoiceMaker extends JFrame implements InvoiceUpdateListener, InvoiceSelectionListener {

    private static final long TWO_HOURS = 2 * 3600 * 1000;

    private static final SimpleDateFormat INVOICE_NUM_DATE_FORMAT = new SimpleDateFormat("yyMMdd");

    private static final String DATE_FORMAT_PROPERTY_NAME = "invoice.maker.date.format";
    private static final String HST_NUM_PROPERTY_NAME = "invoice.maker.hst.number";
    public static final String SAVE_DIRECTORY_PROPERTY_NAME = "invoice.maker.save.directory";
    public static final String OUTPUT_DIRECTORY_PROPERTY_NAME = "invoice.maker.output.directory";
    public static final String FAKE_SAVE_DIRECTORY_PROPERTY_NAME = "invoice.maker.fake.save.directory";
    private static final String VARIANCE_PROPERTY_NAME = "invoice.maker.variance";
    private static final String DEFAULT_MAIL_CC_PROPERTY_NAME = "invoice.maker.default.mail.cc";
    private static final String DEFAULT_DUE_PROPERTY_NAME = "invoice.maker.default.due.days";
    private static final String DEFAULT_FONT_SIZE_PROPERTY_NAME = "invoice.maker.default.font.size";
    private static final String ADVANCED_USER_PROPERTY_NAME = "invoice.maker.advanced.user";

    private JPanel topPanel;
    private JTextField invoiceNumTextField;
    private JComboBox<Item> quickJacketComboBox;
    private JComboBox<Item> quickPantComboBox;
    private JComboBox<Item> quickShirtComboBox;
    private JComboBox<Item> quickDryCleanComboBox;
    private JComboBox<Item> quickDressComboBox;
    private JComboBox<Item> quickOthersComboBox;
    private JTable itemListTable;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JButton saveButton;
    private JButton emailButton;
    private JButton addressBookButton;
    private JTabbedPane tabbedPane;
    private JRadioButton setAddressesRadioButton;
    private JRadioButton setQuickJacketsRadioButton;
    private JRadioButton setQuickPantsRadioButton;
    private JRadioButton setQuickShirtsRadioButton;
    private JRadioButton setQuickDressRadioButton;
    private JRadioButton setQuickDryCleanRadioButton;
    private JRadioButton setQuickOthersRadioButton;
    private JTable settingsTable;
    private JButton settingsAddButton;
    private JButton settingsSortButton;
    private JButton settingsSaveButton;
    private JPanel defaultPanel;
    private JPanel settingsPanel;
    private JTextField openInvoiceTextField;
    private JButton newInvoiceButton;
    private JButton saveCustomerInfoButton;
    private JPanel invoiceDatePanel;
    private JPanel dueDatePanel;
    private JCheckBox isPaidCheckBox;
    private JCheckBox isDoneCheckBox;
    private JCheckBox isPickedUpCheckBox;
    private JTextField customerNameTextField;
    private JTextField customerPhoneTextField;
    private JTextField customerEmailTextField;
    private JTextField advancedInvoiceDateField;
    private JTextField advancedDueDateField;
    private JButton uploadButton;
    private JButton syncButton;
    private JButton printButton;
    private JTextField creditTextField;
    private JButton applyCreditButton;
    private JButton emailSummaryButton;

    private StDatePicker invoiceDatePicker;
    private StDatePicker dueDatePicker;

    private final Invoice invoice;
    private AddressBook addressBook;

    private QuickItemList quickJacketsList;
    private QuickItemList quickPantsList;
    private QuickItemList quickShirtsList;
    private QuickItemList quickDressList;
    private QuickItemList quickDryCleanList;
    private QuickItemList quickOthersList;
    private QuickItemsSettingsTableModel quickJacketsSettingsTableModel;
    private QuickItemsSettingsTableModel quickPantsSettingsTableModel;
    private QuickItemsSettingsTableModel quickShirtsSettingsTableModel;
    private QuickItemsSettingsTableModel quickDressSettingsTableModel;
    private QuickItemsSettingsTableModel quickDryCleanSettingsTableModel;
    private QuickItemsSettingsTableModel quickOthersSettingsTableModel;

    private final ItemListTableModel itemListTableModel;

    private static final String ICON_FILE_PATH = "./Settings/icon50x50.png";
    private final Properties config = StConfig.getInstance();

    private final InvoiceStore invoiceStore;

    private final Timer autoEmailSummaryTimer;

    // the constructor is called inside SwingUtils.invokeLater()
    public InvoiceMaker() throws Exception {
        $$$setupUI$$$();

        if ((new File(ICON_FILE_PATH)).exists()) {
            ImageIcon icon = new ImageIcon(ICON_FILE_PATH);
            setIconImage(icon.getImage());
        }

        invoiceStore = new InvoiceStoreImpl(config);
        autoEmailSummaryTimer = new Timer();
        autoEmailSummaryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Summary summary = new Summary(invoiceStore);

                GmailSender sender = GmailSender.DEFAULT;
                try {
                    String to = "nathanzheng87@gmail.com";
                    String emailTitle = "Sun Tailoring Summary " + TimeUtils.formatDateTimeString(Calendar.getInstance());
                    sender.sendMail(to, new ArrayList<>(), emailTitle, summary.getSummaryEmail(), null);
                } catch (MailException e) {
                    System.err.println("auto email summary failed");
                }
            }
        }, TWO_HOURS, TWO_HOURS);

        invoice = new Invoice();
        invoice.addUpdateListener(this);
        setTitle("Invoice Maker - " + config.getProperty(VARIANCE_PROPERTY_NAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                autoEmailSummaryTimer.cancel();
                invoice.removeUpdateListener(InvoiceMaker.this);
                StLogger.close();
            }
        });

        setExtendedState(Frame.MAXIMIZED_BOTH);
        setContentPane(topPanel);
        setVisible(true);

        initDateFields();

        initializeAddressBook();

        initializeQuickItemList();
        prepareInvoice();
        itemListTableModel = new ItemListTableModel(invoice);
        initItemListTable();
        updateFieldsWithInvoice();

        initializeSettings();

        initializeStats();

        initializeDiag();

        showAdvancedFieldsIfNecessary();

        addListeners();
    }

    private void showAdvancedFieldsIfNecessary() {
        boolean isAdvancedUser = Boolean.parseBoolean(config.getProperty(ADVANCED_USER_PROPERTY_NAME, "false"));
        advancedInvoiceDateField.setVisible(isAdvancedUser);
        advancedDueDateField.setVisible(isAdvancedUser);
    }

    private void initDateFields() {
        String datePattern = config.getProperty(DATE_FORMAT_PROPERTY_NAME);
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);

        // somehow must set layout, otherwise throws a null pointer
        invoiceDatePanel.setLayout(new BoxLayout(invoiceDatePanel, BoxLayout.X_AXIS));
        invoiceDatePicker = new StDatePicker(sdf);
        invoiceDatePanel.add(invoiceDatePicker.getComponent());

        dueDatePanel.setLayout(new BoxLayout(dueDatePanel, BoxLayout.X_AXIS));
        dueDatePicker = new StDatePicker(sdf);
        dueDatePanel.add(dueDatePicker.getComponent());
    }

    private void initializeStats() {
        STStatsForm statsForm = new STStatsForm(config, invoiceStore);
        tabbedPane.add("    Stats    ", statsForm.getTopPanel());

        statsForm.addInvoiceSelectionListener(this);
    }

    private void initializeDiag() {
        STDiagForm diagForm = new STDiagForm(config, invoiceStore);
        tabbedPane.add("    Trouble Shoot    ", diagForm.getTopPanel());
    }

    private void initializeAddressBook() {
        addressBook = new AddressBook();
    }

    private void initializeQuickItemList() {
        quickJacketsList = new QuickItemList(new File("./Settings/QuickJackets.csv"));
        loadQuickItemCombobox(quickJacketComboBox, quickJacketsList);

        quickPantsList = new QuickItemList(new File("./Settings/QuickPants.csv"));
        loadQuickItemCombobox(quickPantComboBox, quickPantsList);

        quickShirtsList = new QuickItemList(new File("./Settings/QuickShirts.csv"));
        loadQuickItemCombobox(quickShirtComboBox, quickShirtsList);

        quickDressList = new QuickItemList(new File("./Settings/QuickDress.csv"));
        loadQuickItemCombobox(quickDressComboBox, quickDressList);

        quickDryCleanList = new QuickItemList(new File("./Settings/QuickDryClean.csv"));
        loadQuickItemCombobox(quickDryCleanComboBox, quickDryCleanList);

        quickOthersList = new QuickItemList(new File("./Settings/QuickOthers.csv"));
        loadQuickItemCombobox(quickOthersComboBox, quickOthersList);
    }

    private void loadQuickItemCombobox(JComboBox<Item> comboBox, QuickItemList list) {
        comboBox.removeAllItems();
        list.getList().forEach(comboBox::addItem);
        comboBox.setSelectedIndex(0);
    }

    private void prepareInvoice() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        String invoiceNumBase = INVOICE_NUM_DATE_FORMAT.format(now);
        int invoiceNumAppendix = 0;
        // test if the invoice number already exists. If so, add the appendix until the invoice number doesn't exist
        String invoiceNum = invoiceNumBase + invoiceNumAppendix;
        while (invoiceStore.get(invoiceNum) != null) {
            invoiceNumAppendix++;
            invoiceNum = invoiceNumBase + invoiceNumAppendix;
        }

        String hstNum = config.getProperty(HST_NUM_PROPERTY_NAME);

        long invoiceDate = now.getTime();
        calendar.add(Calendar.DATE, Integer.parseInt(config.getProperty(DEFAULT_DUE_PROPERTY_NAME)));
        long dueDate = calendar.getTimeInMillis();
        assert addressBook != null;
        String customerInfo = "";
        String selfAddress = addressBook.getEntry(0).getAddress().toString();
        double credit = 0;
        boolean isPaid = true;
        boolean isDone = false;
        boolean isPickedUp = false;

        updateCurrentInvoice(new Invoice(invoiceNum, hstNum, invoiceDate, dueDate,
                customerInfo, selfAddress, new ArrayList<>(), credit, isPaid, isDone, isPickedUp));
    }

    private void initItemListTable() {
        itemListTable.setModel(itemListTableModel);
        // column width
        int tableWidth = itemListTable.getWidth();
        for (int i = 0; i < itemListTableModel.getColumnCount(); i++) {
            ItemListTableModel.Column column = ItemListTableModel.Column.values()[i];
            if (column == ItemListTableModel.Column.Description) {
                itemListTable.getColumnModel().getColumn(i).setPreferredWidth(tableWidth / itemListTableModel.getColumnCount() * 2);
            }
        }

        // header
        TableCellRenderer headerRenderer = itemListTable.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
        }
        // cell renderer
        for (ItemListTableModel.Column column : ItemListTableModel.Column.values()) {
            itemListTable.getColumnModel().getColumn(column.ordinal()).setCellRenderer(new CustomCellRenderer(itemListTableModel));
        }
        // single click edit
        float fontSize = Float.parseFloat(config.getProperty(DEFAULT_FONT_SIZE_PROPERTY_NAME, "12"));
        JTextField cellEditorTextfield = new JTextField();
        cellEditorTextfield.setFont(cellEditorTextfield.getFont().deriveFont(fontSize));
        DefaultCellEditor singleClickEditor = new DefaultCellEditor(cellEditorTextfield);
        singleClickEditor.setClickCountToStart(1);
        for (int i = 0; i < itemListTable.getColumnCount(); i++) {
            itemListTable.setDefaultEditor(itemListTable.getColumnClass(i), singleClickEditor);
        }

        // set cell height
        itemListTable.setRowHeight(30);
        itemListTable.setFont(itemListTable.getFont().deriveFont(fontSize));

        // todo tab to move the next cell
    }

    // todo: search for if this method can be called outside edt
    private void updateFieldsWithInvoice() {
        invoiceNumTextField.setText(invoice.getInvoiceNum());
        invoiceDatePicker.setDate(invoice.getInvoiceDate());
        dueDatePicker.setDate(invoice.getDueDate());

        String customerAddress = invoice.getCustomerAddress();
        ContactInfo contactInfo = ContactInfo.parse(customerAddress);
        customerNameTextField.setText(contactInfo.getName());
        customerPhoneTextField.setText(GuiUtils.formatPhoneNumber(contactInfo.getPhone()));
        customerEmailTextField.setText(contactInfo.getEmail());
        customerNameTextField.setBackground(Color.WHITE);
        customerPhoneTextField.setBackground(Color.WHITE);
        customerEmailTextField.setBackground(Color.WHITE);

        isPaidCheckBox.setSelected(invoice.isPaid());
        isPaidCheckBox.setForeground(invoice.isPaid() ? Color.BLACK : Color.RED);
        isDoneCheckBox.setSelected(invoice.isDone());
        isDoneCheckBox.setForeground(invoice.isDone() ? Color.BLACK : Color.RED);
        isPickedUpCheckBox.setSelected(invoice.isPickedUp());

        creditTextField.setText(MathUtil.formatCurrency(invoice.getCredit()));

        itemListTableModel.fireTableDataChanged();

        logAction("Updated fields with invoice \'" + invoice.getInvoiceNum() + "\'");
    }

    private void initializeSettings() {
        setAddressesRadioButton.setSelected(true);
        initAddressBookSettings();
        initQuickItemsSettings();
    }

    private void initAddressBookSettings() {
        AddressBookSettingsTableModel addressBookTableModel = new AddressBookSettingsTableModel(addressBook);
        settingsTable.setModel(addressBookTableModel);
        // column width
        int tableWidth = settingsTable.getWidth();
        for (int i = 0; i < settingsTable.getColumnCount(); i++) {
            AddressBookSettingsTableModel.Column column = AddressBookSettingsTableModel.Column.values()[i];
            if (column == AddressBookSettingsTableModel.Column.Email) {
                settingsTable.getColumnModel().getColumn(i).setPreferredWidth((int) (tableWidth / 8 * 1.5));
            }
        }

        // header
        TableCellRenderer headerRenderer = settingsTable.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
        }
        // cell renderer
        for (AddressBookTableModel.Column column : AddressBookTableModel.Column.values()) {
            settingsTable.getColumnModel().getColumn(column.ordinal()).setCellRenderer(new DefaultTableCellRenderer());
        }
        // cell editor
        for (int i = 0; i < settingsTable.getColumnCount(); i++) {
            settingsTable.setDefaultEditor(settingsTable.getColumnClass(i), new DefaultCellEditor(new JTextField()));
        }
        addressBookTableModel.fireTableDataChanged();
    }

    private void initQuickItemsSettings() {
        quickJacketsSettingsTableModel = new QuickItemsSettingsTableModel(quickJacketsList);
        quickPantsSettingsTableModel = new QuickItemsSettingsTableModel(quickPantsList);
        quickShirtsSettingsTableModel = new QuickItemsSettingsTableModel(quickShirtsList);
        quickDressSettingsTableModel = new QuickItemsSettingsTableModel(quickDressList);
        quickDryCleanSettingsTableModel = new QuickItemsSettingsTableModel(quickDryCleanList);
        quickOthersSettingsTableModel = new QuickItemsSettingsTableModel(quickOthersList);
    }

    private void selectQuickItemsSettings(int index) {
        QuickItemsSettingsTableModel tableModel = null;
        switch (index) {
            case 0:
                tableModel = quickJacketsSettingsTableModel;
                break;
            case 1:
                tableModel = quickPantsSettingsTableModel;
                break;
            case 2:
                tableModel = quickShirtsSettingsTableModel;
                break;
            case 3:
                tableModel = quickDressSettingsTableModel;
                break;
            case 4:
                tableModel = quickDryCleanSettingsTableModel;
                break;
            case 5:
                tableModel = quickOthersSettingsTableModel;
                break;
            default:
                assert false;
                break;
        }
        assert tableModel != null;
        settingsTable.setModel(tableModel);
        // header
        TableCellRenderer headerRenderer = settingsTable.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
        }
        // cell renderer
        for (QuickItemsSettingsTableModel.Column column : QuickItemsSettingsTableModel.Column.values()) {
            settingsTable.getColumnModel().getColumn(column.ordinal()).setCellRenderer(new DefaultTableCellRenderer());
        }
        // cell editor
        for (int i = 0; i < settingsTable.getColumnCount(); i++) {
            settingsTable.setDefaultEditor(settingsTable.getColumnClass(i), new DefaultCellEditor(new JTextField()));
        }
        tableModel.fireTableDataChanged();
    }

    private SimpleAction getCreateNewInvoiceAction() {
        return new SimpleAction(true, topPanel) {
            @Override
            protected void action() throws Exception {
                prepareInvoice();
            }

            @Override
            protected void onCompletion() {
                updateFieldsWithInvoice();
                tabbedPane.setSelectedIndex(0);
            }

            @Override
            protected String getCompletionMsg() {
                return "Created new invoice \'" + invoice.getInvoiceNum() + "\'";
            }
        };
    }

    private SimpleAction getSetDateAction(StDatePicker datePicker) {
        return new SimpleAction(false, topPanel) {
            @Override
            protected void action() throws Exception {
                String dateString = datePicker.getFormattedDate();
                if (datePicker == invoiceDatePicker) {
                    invoice.setInvoiceDate(TimeUtils.parseDateToMillis(dateString));
                } else if (datePicker == dueDatePicker) {
                    invoice.setDueDate(TimeUtils.parseDateToMillis(dateString));
                }
            }

            @Override
            protected void onCompletion() {
                String fieldName = datePicker == invoiceDatePicker ? "invoice date" : "due date";
                logAction("Set " + fieldName + " for Invoice \'" + invoice.getInvoiceNum() + "\' to " + datePicker.getFormattedDate());
            }
        };
    }

    private SimpleAction getApplyCreditAction() {
        return new SimpleAction(false, topPanel) {
            @Override
            protected void action() throws Exception {
                try {
                    double credit = Double.parseDouble(creditTextField.getText().trim());
                    if (credit >= 0) {
                        invoice.setCredit(credit);
                    }
                } catch (NumberFormatException e) {
                    GuiUtils.popError("Enter a valid number!");
                }
            }

            @Override
            protected void onCompletion() {
                logAction("Applied credit " + invoice.getCredit() + " for Invoice \'" + invoice.getInvoiceNum());
            }
        };
    }

    private SimpleAction getSetPaidAction() {
        return new SimpleAction(false, topPanel) {
            @Override
            protected void action() throws Exception {
                invoice.setPaid(isPaidCheckBox.isSelected());
            }

            @Override
            protected void onCompletion() {
                isPaidCheckBox.setForeground(invoice.isPaid() ? Color.BLACK : Color.RED);
            }

            @Override
            protected String getCompletionMsg() {
                return "Set invoice paid to " + isPaidCheckBox.isSelected();
            }
        };
    }

    private SimpleAction getSetDoneAction() {
        return new SimpleAction(false, topPanel) {
            @Override
            protected void action() throws Exception {
                invoice.setDone(isDoneCheckBox.isSelected());
            }

            @Override
            protected void onCompletion() {
                isDoneCheckBox.setForeground(invoice.isDone() ? Color.BLACK : Color.RED);
            }

            @Override
            protected String getCompletionMsg() {
                return "Set invoice done to " + isDoneCheckBox.isSelected();
            }
        };
    }

    private SimpleAction getSetPickedUpAction() {
        return new SimpleAction(false, topPanel) {
            @Override
            protected void action() throws Exception {
                invoice.setPickedUp(isPickedUpCheckBox.isSelected());
            }

            @Override
            protected String getCompletionMsg() {
                return "Set invoice picked up to " + isPickedUpCheckBox.isSelected();
            }
        };
    }

    private SimpleAction getSaveCustomerInfoAction() {
        return new SimpleAction(true, topPanel) {
            private final String EMPTY_CUSTOMER_FIELD = "Empty customer field";
            private final String CANCEL_SAVE = "Save cancelled";

            @Override
            protected void action() throws Exception {
                String name = customerNameTextField.getText().trim();
                if (name.isEmpty()) {
                    throw new Exception(EMPTY_CUSTOMER_FIELD);
                }
                String phone = GuiUtils.formatPhoneNumber(customerPhoneTextField.getText());
                String email = customerEmailTextField.getText();
                ContactInfo contactInfo = new ContactInfo(name, phone, email);
                // check if the contact info already exists by name
                if (addressBook.contains(contactInfo)) {
                    String confirmMsg = "Contact \'" + contactInfo.getName() + "\' may have already existed in address book." +
                            "\nClick OK to continue adding this to the address book as a different contact."
                            + "\nOtherwise, click No, and select the existing contact from the address book";
                    int dialogResult = JOptionPane.showConfirmDialog(null, confirmMsg, "Address may have already existed", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.NO_OPTION) {
                        throw new Exception(CANCEL_SAVE);
                    }
                }

                String confirmMsg = "Contact info will be saved as:";
                confirmMsg += "\n\nName: " + contactInfo.getName();
                confirmMsg += "\nPhone: " + contactInfo.getPhone();
                confirmMsg += "\nEmail: " + contactInfo.getEmail();
                confirmMsg += "\n\nDoes this look correct? If yes, click on OK;" +
                        "\nOtherwise, click Cancel, and check the contact info format. Then try to save again.";
                int dialogResult = JOptionPane.showConfirmDialog(null,
                        confirmMsg, "Please Confirm", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.NO_OPTION) {
                    throw new Exception(CANCEL_SAVE);
                }

                addressBook.addEntry(contactInfo);
                addressBook.save();
            }

            @Override
            protected void onCompletion() {
                customerNameTextField.setBackground(Color.WHITE);
                if (setAddressesRadioButton.isSelected()) {
                    initAddressBookSettings();
                }
            }

            @Override
            protected String getCompletionMsg() {
                return "Customer info successfully saved.";
            }

            @Override
            protected void onError(Exception error) {
                if (error.getMessage().equals(EMPTY_CUSTOMER_FIELD)) {
                    customerNameTextField.setBackground(Color.PINK);
                }
            }

            @Override
            protected String getErrorMsg(Exception error) {
                if (error.getMessage().equals(EMPTY_CUSTOMER_FIELD)) {
                    return "Customer info must be filled.";
                } else if (error.getMessage().equals(CANCEL_SAVE)) {
                    return "Cancelled save customer info";
                } else {
                    return "Save customer info failed - contact administrator";
                }
            }
        };
    }

    private SimpleAction getQuickItemSelectionAction(JComboBox<Item> quickItemComboBox) {
        return new SimpleAction(false, topPanel) {

            @Override
            protected void action() throws Exception {
                if (tabbedPane.getSelectedIndex() != 0) return;

                Item selectedItem = (Item) quickItemComboBox.getSelectedItem();
                if (selectedItem != null) {
                    addEntry(new Item(selectedItem.getName(), selectedItem.getQuantity(), selectedItem.getUnitPrice()));
                }
            }
        };
    }

    private static String contactInfoToString(ContactInfo contactInfo) {
        return contactInfo.getName() +
                (contactInfo.getPhone().isEmpty() ? "" : "\n" + contactInfo.getPhone()) +
                (contactInfo.getEmail().isEmpty() ? "" : "\n" + contactInfo.getEmail());
    }

    private SimpleAction getSaveAction() {
        return new SimpleAction(true, topPanel) {
            private final String EMPTY_CUSTOMER_INFO = "Empty Customer Info";
            private final String CANCEL_SAVE = "Save cancelled";

            @Override
            protected void action() throws Exception {
                String name = customerNameTextField.getText();
                if (name.isEmpty()) {
                    throw new Exception(EMPTY_CUSTOMER_INFO);
                }
                String phone = GuiUtils.formatPhoneNumber(customerPhoneTextField.getText());
                String email = customerEmailTextField.getText();
                ContactInfo contactInfo = new ContactInfo(name, phone, email);
                invoice.setCustomerInfo(contactInfoToString(contactInfo));

                // check if the current invoice exists
                if (invoiceStore.get(invoice.getInvoiceNum()) != null) {
                    int dialogResult = JOptionPane.showConfirmDialog(null,
                            "Invoice \'" + invoice.getInvoiceNum() + "\' already exists. Would you like to replace it?",
                            "Warning", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.NO_OPTION) {
                        throw new Exception(CANCEL_SAVE);
                    }
                }

                // save customer info if not already exist
                if (!addressBook.contains(contactInfo)) {
                    addressBook.addEntry(contactInfo);
                    addressBook.save();

                    // fire the settings table if necessary
                    TableModel settingsTableModel = settingsTable.getModel();
                    if (settingsTableModel instanceof AddressBookSettingsTableModel) {
                        ((AddressBookSettingsTableModel) settingsTableModel).fireTableDataChanged();
                    }
                }

                // now actually save the .dat file and generate .html file
                invoiceStore.save(invoice);
                InvoiceHtml.createHtml(invoice);
            }

            @Override
            protected void onCompletion() {
                customerNameTextField.setBackground(Color.WHITE);
                logDetailedAction("Invoice summary: " + invoice.summary());
            }

            @Override
            protected String getCompletionMsg() {
                return "Saved Invoice '" + invoice.getInvoiceNum() + "'";
            }

            @Override
            protected void onError(Exception error) {
                String errorMsg = error.getMessage();
                if (errorMsg.equals(EMPTY_CUSTOMER_INFO)) {
                    customerNameTextField.setBackground(Color.PINK);
                } else {
                    customerNameTextField.setBackground(Color.WHITE);
                }
            }

            @Override
            protected String getErrorMsg(Exception error) {
                String errorMsg = error.getMessage();
                switch (errorMsg) {
                    case EMPTY_CUSTOMER_INFO:
                        return "Customer info must be filled.";
                    case CANCEL_SAVE:
                        return "";
                    default:
                        return "Save failed - try again or contact system administrator";
                }
            }
        };
    }

    private SimpleAction getUploadInvoiceAction() {
        return new SimpleAction(true, topPanel) {
            private final String INVOICE_NOT_SAVED = "The current invoice is not saved. Press Save button first before uploading";

            @Override
            protected void action() throws Exception {
                if (invoiceStore.get(invoice.getInvoiceNum()) == null) {
                    throw new Exception(INVOICE_NOT_SAVED);
                }

                invoiceStore.upload(invoice);
            }

            @Override
            protected String getCompletionMsg() {
                return "Uploaded Invoice '" + invoice.getInvoiceNum() + "'";
            }

            @Override
            protected String getErrorMsg(Exception error) {
                String errorMsg = error.getMessage();
                switch (errorMsg) {
                    case INVOICE_NOT_SAVED:
                        return errorMsg;
                    default:
                        return "Upload failed - try again or contact system administrator. Details:\n" +
                                errorMsg;
                }
            }
        };
    }

    private SimpleAction getPrintInvoiceAction() {
        return new SimpleAction(false, topPanel) {
            @Override
            protected void action() throws Exception {
                InvoicePrinter invoicePrinter = new InvoicePrinter(invoice, config);

                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintable(invoicePrinter);

                PrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
                set.add(OrientationRequested.PORTRAIT);
                set.add(MediaSizeName.INVOICE);
                try {
                    job.print(set);
                } catch (PrinterException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private SimpleAction getSyncAction() {
        return new SimpleAction(true, topPanel) {
            String summary;

            @Override
            protected void action() throws Exception {
                summary = invoiceStore.sync();
            }

            @Override
            protected String getCompletionMsg() {
                return summary;
            }
        };
    }

    private SimpleAction getBrowseInvoiceAction() {
        return new SimpleAction(false, topPanel) {
            private final String CANCEL_OPEN = "Cancel open";

            @Override
            protected void action() throws Exception {
                String saveDirectory = config.getProperty(SAVE_DIRECTORY_PROPERTY_NAME);
                File directory = new File(saveDirectory);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Open Invoice .dat file");
                fileChooser.setFileFilter(new StFileFilter(".dat"));
                fileChooser.setCurrentDirectory(directory);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    Invoice readInvoice = Invoice.read(file);
                    updateCurrentInvoice(readInvoice);
                } else {
                    throw new Exception(CANCEL_OPEN);
                }
            }

            @Override
            protected void onCompletion() {
                updateFieldsWithInvoice();
                tabbedPane.setSelectedIndex(0);
            }

            @Override
            protected String getErrorMsg(Exception error) {
                if (error.getMessage().equals(CANCEL_OPEN)) {
                    return "";
                }
                return "Error occurred when opening invoice file. Contact system administrator.";
            }
        };
    }

    private SimpleAction getOpenInvoiceAction() {
        return new SimpleAction(false, topPanel) {
            private final String INVOICE_DNE = "Invoice does not exist";

            @Override
            protected void action() throws Exception {
                Invoice readInvoice = invoiceStore.get(openInvoiceTextField.getText());
                if (readInvoice == null) {
                    throw new Exception("Invoice " + openInvoiceTextField.getText() + " does not exist");
                }
                updateCurrentInvoice(readInvoice);
            }

            @Override
            protected void onCompletion() {
                openInvoiceTextField.setText("");
                openInvoiceTextField.setBackground(Color.WHITE);
                updateFieldsWithInvoice();
                tabbedPane.setSelectedIndex(0);
            }

            @Override
            protected String getCompletionMsg() {
                return "Opened invoice " + openInvoiceTextField.getText();
            }

            @Override
            protected void onError(Exception error) {
                openInvoiceTextField.setText("");
                openInvoiceTextField.setBackground(Color.PINK);
            }

            @Override
            protected String getErrorMsg(Exception error) {
                if (error.getMessage().equals(INVOICE_DNE)) {
                    return "Invoice " + openInvoiceTextField.getText() + " does not exist";
                } else {
                    return "Open invoice " + openInvoiceTextField.getText() + " failed";
                }
            }
        };
    }

    private void addListeners() {
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() != 0) {
                invoiceDatePicker.setVisible(false);
                dueDatePicker.setVisible(false);
            } else {
                invoiceDatePicker.setVisible(true);
                dueDatePicker.setVisible(true);
            }
        });

        newInvoiceButton.addActionListener(getCreateNewInvoiceAction());
        invoiceNumTextField.setEditable(false);

        Action invoiceDateSetAction = getSetDateAction(invoiceDatePicker);
        invoiceDatePicker.addActionListener(invoiceDateSetAction);
        advancedInvoiceDateField.addActionListener(e -> {
            invoiceDatePicker.setMonthDay(advancedInvoiceDateField.getText());
            invoiceDateSetAction.actionPerformed(e);
            advancedInvoiceDateField.setText("");
        });

        Action dueDateSetAction = getSetDateAction(dueDatePicker);
        dueDatePicker.addActionListener(dueDateSetAction);
        advancedDueDateField.addActionListener(e -> {
            dueDatePicker.setMonthDay(advancedDueDateField.getText());
            dueDateSetAction.actionPerformed(e);
            advancedDueDateField.setText("");
        });

        customerPhoneTextField.addActionListener(e ->
                customerPhoneTextField.setText(GuiUtils.formatPhoneNumber(customerPhoneTextField.getText().trim())));

        applyCreditButton.addActionListener(getApplyCreditAction());

        isPaidCheckBox.addActionListener(getSetPaidAction());
        isDoneCheckBox.addActionListener(getSetDoneAction());
        isPickedUpCheckBox.addActionListener(getSetPickedUpAction());

        final AddressSelectionListener addressSelectionHandler = contactInfo -> {
            if (contactInfo != null) {
                customerNameTextField.setText(contactInfo.getName());
                customerPhoneTextField.setText(GuiUtils.formatPhoneNumber(contactInfo.getPhone()));
                customerEmailTextField.setText(contactInfo.getEmail());
                customerNameTextField.setBackground(Color.WHITE);
                customerPhoneTextField.setBackground(Color.WHITE);
                customerEmailTextField.setBackground(Color.WHITE);

                invoice.setCustomerInfo(contactInfoToString(contactInfo));
                logAction("Selected " + contactInfo.getName() + " from address book.");
            }
        };

        addressBookButton.addActionListener(e -> {
            assert addressBook != null;
            AddressBookGUI addressBookGUI = new AddressBookGUI(addressBook);
            addressBookGUI.addSelectionListener(addressSelectionHandler);
        });

        saveCustomerInfoButton.addActionListener(getSaveCustomerInfoAction());

        quickJacketComboBox.addActionListener(getQuickItemSelectionAction(quickJacketComboBox));
        quickPantComboBox.addActionListener(getQuickItemSelectionAction(quickPantComboBox));
        quickShirtComboBox.addActionListener(getQuickItemSelectionAction(quickShirtComboBox));
        quickDressComboBox.addActionListener(getQuickItemSelectionAction(quickDressComboBox));
        quickDryCleanComboBox.addActionListener(getQuickItemSelectionAction(quickDryCleanComboBox));
        quickOthersComboBox.addActionListener(getQuickItemSelectionAction(quickOthersComboBox));

        Action removeItemAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeItem();
            }
        };
        itemListTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "RemoveItem");
        itemListTable.getActionMap().put("RemoveItem", removeItemAction);

        saveButton.addActionListener(getSaveAction());

        uploadButton.addActionListener(getUploadInvoiceAction());
        // todo: set visible to true
        uploadButton.setVisible(false);

        printButton.addActionListener(getPrintInvoiceAction());

        syncButton.addActionListener(getSyncAction());
        // todo: set visible to true
        syncButton.setVisible(false);

        openInvoiceTextField.addActionListener(getOpenInvoiceAction());
        // todo: mahalo - continue

        final MailEventListener mailEventHandler = status -> logAction("Mail sending " + status);
        emailButton.addActionListener(e -> {
            assert addressBook != null;
            String defaultTo = "";
            Matcher matcher = ContactInfo.EMAIL_PATTERN.matcher(customerEmailTextField.getText());
            if (matcher.find()) {
                defaultTo = matcher.group(1);
            }

            String defaultCc = config.getProperty(DEFAULT_MAIL_CC_PROPERTY_NAME);

            // try to find the generated output
            String outputDirectory = config.getProperty(OUTPUT_DIRECTORY_PROPERTY_NAME);
            String attachmentString = outputDirectory + invoice.getInvoiceNum() + ".html";
            File file = new File(attachmentString);
            if (!file.exists()) {
                attachmentString = "";
            } else {
                attachmentString = file.getAbsolutePath();
            }

            StMailGUI mailGUI = new StMailGUI(addressBook, defaultTo, defaultCc, attachmentString, invoice.getInvoiceNum(), config);
            mailGUI.addMailEventListener(mailEventHandler);
        });

        settingsSaveButton.addActionListener(e -> {
            TableModel tableModel = settingsTable.getModel();
            if (tableModel instanceof AddressBookSettingsTableModel) {
                addressBook.save();
                logAction("Saved address book");

            } else if (tableModel == quickJacketsSettingsTableModel) {
                quickJacketsList.save();
                loadQuickItemCombobox(quickJacketComboBox, quickJacketsList);

            } else if (tableModel == quickPantsSettingsTableModel) {
                quickPantsList.save();
                loadQuickItemCombobox(quickPantComboBox, quickPantsList);

            } else if (tableModel == quickShirtsSettingsTableModel) {
                quickShirtsList.save();
                loadQuickItemCombobox(quickShirtComboBox, quickShirtsList);

            } else if (tableModel == quickDressSettingsTableModel) {
                quickDressList.save();
                loadQuickItemCombobox(quickDressComboBox, quickDressList);

            } else if (tableModel == quickDryCleanSettingsTableModel) {
                quickDryCleanList.save();
                loadQuickItemCombobox(quickDryCleanComboBox, quickDryCleanList);

            } else if (tableModel == quickOthersSettingsTableModel) {
                quickOthersList.save();
                loadQuickItemCombobox(quickOthersComboBox, quickOthersList);
            }
        });

        settingsAddButton.addActionListener(e -> {
            TableModel tableModel = settingsTable.getModel();
            if (tableModel instanceof AddressBookSettingsTableModel) {
                Address address = new Address("Name", "100 Main St.", "Vancouver", "BC", "V1V 1V1");
                addressBook.addEntry(new ContactInfo("Name", address, "123-456-7890", ""));
                int row = addressBook.getNumEntries() - 1;
                ((AddressBookSettingsTableModel) tableModel).fireTableRowsInserted(row, row);

            } else if (tableModel == quickJacketsSettingsTableModel) {
                Item item = new Item("Jacket - do something", 1, 0);
                quickJacketsList.addItem(item);
                int row = quickJacketsList.getList().size() - 1;
                ((QuickItemsSettingsTableModel) tableModel).fireTableRowsInserted(row, row);

            } else if (tableModel == quickPantsSettingsTableModel) {
                Item item = new Item("Pant - do something", 1, 0);
                quickPantsList.addItem(item);
                int row = quickPantsList.getList().size() - 1;
                ((QuickItemsSettingsTableModel) tableModel).fireTableRowsInserted(row, row);

            } else if (tableModel == quickShirtsSettingsTableModel) {
                Item item = new Item("Shirt - do something", 1, 0);
                quickShirtsList.addItem(item);
                int row = quickShirtsList.getList().size() - 1;
                ((QuickItemsSettingsTableModel) tableModel).fireTableRowsInserted(row, row);

            } else if (tableModel == quickDressSettingsTableModel) {
                Item item = new Item("Dress - do something", 1, 0);
                quickDressList.addItem(item);
                int row = quickDressList.getList().size() - 1;
                ((QuickItemsSettingsTableModel) tableModel).fireTableRowsInserted(row, row);

            } else if (tableModel == quickDryCleanSettingsTableModel) {
                Item item = new Item("Dry clean - do something", 1, 0);
                quickDryCleanList.addItem(item);
                int row = quickDryCleanList.getList().size() - 1;
                ((QuickItemsSettingsTableModel) tableModel).fireTableRowsInserted(row, row);

            } else if (tableModel == quickOthersSettingsTableModel) {
                Item item = new Item("Other - do something", 1, 0);
                quickOthersList.addItem(item);
                int row = quickOthersList.getList().size() - 1;
                ((QuickItemsSettingsTableModel) tableModel).fireTableRowsInserted(row, row);
            }
        });

        Action settingRemoveAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableModel tableModel = settingsTable.getModel();
                int row = settingsTable.getSelectedRow();
                if (row < 0) {
                    popError("Select a row in the table to remove.");
                    return;
                }

                if (tableModel instanceof AddressBookSettingsTableModel) {
                    addressBook.removeEntry(row);
                    ((AddressBookSettingsTableModel) tableModel).fireTableRowsDeleted(row, row);

                } else if (tableModel == quickJacketsSettingsTableModel) {
                    quickJacketsList.removeEntry(row);
                    ((QuickItemsSettingsTableModel) tableModel).fireTableRowsDeleted(row, row);

                } else if (tableModel == quickPantsSettingsTableModel) {
                    quickPantsList.removeEntry(row);
                    ((QuickItemsSettingsTableModel) tableModel).fireTableRowsDeleted(row, row);

                } else if (tableModel == quickShirtsSettingsTableModel) {
                    quickShirtsList.removeEntry(row);
                    ((QuickItemsSettingsTableModel) tableModel).fireTableRowsDeleted(row, row);

                } else if (tableModel == quickDressSettingsTableModel) {
                    quickDressList.removeEntry(row);
                    ((QuickItemsSettingsTableModel) tableModel).fireTableRowsDeleted(row, row);

                } else if (tableModel == quickDryCleanSettingsTableModel) {
                    quickDryCleanList.removeEntry(row);
                    ((QuickItemsSettingsTableModel) tableModel).fireTableRowsDeleted(row, row);

                } else if (tableModel == quickOthersSettingsTableModel) {
                    quickOthersList.removeEntry(row);
                    ((QuickItemsSettingsTableModel) tableModel).fireTableRowsDeleted(row, row);
                }
            }
        };
        settingsTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "RemoveItem");
        settingsTable.getActionMap().put("RemoveItem", settingRemoveAction);

        settingsSortButton.addActionListener(e -> {
            TableModel tableModel = settingsTable.getModel();
            if (tableModel instanceof AddressBookSettingsTableModel) {
                addressBook.sort();
                ((AddressBookSettingsTableModel) tableModel).fireTableDataChanged();

            } else if (tableModel == quickJacketsSettingsTableModel) {
                quickJacketsList.sort();
                ((QuickItemsSettingsTableModel) tableModel).fireTableDataChanged();

            } else if (tableModel == quickPantsSettingsTableModel) {
                quickPantsList.sort();
                ((QuickItemsSettingsTableModel) tableModel).fireTableDataChanged();

            } else if (tableModel == quickShirtsSettingsTableModel) {
                quickShirtsList.sort();
                ((QuickItemsSettingsTableModel) tableModel).fireTableDataChanged();

            } else if (tableModel == quickDressSettingsTableModel) {
                quickDressList.sort();
                ((QuickItemsSettingsTableModel) tableModel).fireTableDataChanged();

            } else if (tableModel == quickDryCleanSettingsTableModel) {
                quickDryCleanList.sort();
                ((QuickItemsSettingsTableModel) tableModel).fireTableDataChanged();

            } else if (tableModel == quickOthersSettingsTableModel) {
                quickOthersList.sort();
                ((QuickItemsSettingsTableModel) tableModel).fireTableDataChanged();
            }
        });

        setAddressesRadioButton.addActionListener(e -> initAddressBookSettings());

        setQuickJacketsRadioButton.addActionListener(e -> selectQuickItemsSettings(0));

        setQuickPantsRadioButton.addActionListener(e -> selectQuickItemsSettings(1));

        setQuickShirtsRadioButton.addActionListener(e -> selectQuickItemsSettings(2));

        setQuickDressRadioButton.addActionListener(e -> selectQuickItemsSettings(3));

        setQuickDryCleanRadioButton.addActionListener(e -> selectQuickItemsSettings(4));

        setQuickOthersRadioButton.addActionListener(e -> selectQuickItemsSettings(5));

        emailSummaryButton.addActionListener(e -> emailSummary());

    }

    private void emailSummary() {
        Summary summary = new Summary(invoiceStore);
        summary.zip7DayUndone();

        // send mail
        GmailSender sender = GmailSender.DEFAULT;
        String emailTitle = "Sun Tailoring Summary " + TimeUtils.formatDateString(Calendar.getInstance());
        topPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String to = "nathanzheng87@gmail.com";

        String zipFileName = summary.getZipFileName();
        File zipFile = new File(zipFileName);
        try {
            if (zipFile.exists()) {
                sender.sendMail(to, new ArrayList<>(), emailTitle, summary.getSummaryEmail(), zipFileName);
            } else {
                sender.sendMail(to, new ArrayList<>(), emailTitle, summary.getSummaryEmail(), null);
            }

            topPanel.setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(null, "Successfully sent " + emailTitle);

        } catch (MailException exception) {
            popError("Mail send failed - " + exception.getMessage());
        } finally {
            zipFile.delete();
        }
    }

    private void addEntry(ItemListEntry entry) {
        invoice.addListEntry(entry);
        itemListTableModel.fireTableRowsInserted(invoice.getItemListSize() - 1, invoice.getItemListSize() - 1);
        // select the last row
        itemListTable.changeSelection(invoice.getItemListSize() - 1, 0, false, false);
        logAction("Added " + entry.getClass().getSimpleName());
    }

    private void removeItem() {
        int selectedRow = itemListTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < invoice.getItemListSize()) {
            invoice.removeListEntry(selectedRow);
            itemListTableModel.fireTableRowsDeleted(selectedRow, selectedRow);
            logAction("Removed item");
        } else {
            logAction("Invalid remove item operation");
        }
    }

    @Override
    public void invoiceRecalculated() {
        updateInvoiceMoneyFields();
    }

    // this method can be called outside EDT
    private void updateInvoiceMoneyFields() {
        SwingUtilities.invokeLater(() -> {
            subtotalLabel.setText(MathUtil.formatCurrency(invoice.getSubtotal()));
            taxLabel.setText(MathUtil.formatCurrency(invoice.getTax()));
            totalLabel.setText(MathUtil.formatCurrency(invoice.getTotal()));
        });
    }

    @Override
    public void invoiceSelected(Invoice selectedInvoice) {
        // todo: check if existing invoice has been saved

        updateCurrentInvoice(selectedInvoice);
        updateFieldsWithInvoice();
        tabbedPane.setSelectedIndex(0);
    }

    private void updateCurrentInvoice(Invoice newInvoice) {
        // must first end all field edits
        TableCellEditor editor = itemListTable.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }

        // then call Invoice#update()
        invoice.update(newInvoice);
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane = new JTabbedPane();
        tabbedPane.setFocusable(false);
        tabbedPane.setFont(new Font(tabbedPane.getFont().getName(), tabbedPane.getFont().getStyle(), 24));
        topPanel.add(tabbedPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        defaultPanel = new JPanel();
        defaultPanel.setLayout(new GridLayoutManager(5, 5, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("    Default    ", defaultPanel);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setFont(new Font(scrollPane1.getFont().getName(), Font.BOLD, scrollPane1.getFont().getSize()));
        defaultPanel.add(scrollPane1, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        itemListTable = new JTable();
        scrollPane1.setViewportView(itemListTable);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        defaultPanel.add(panel1, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setFont(new Font(label1.getFont().getName(), label1.getFont().getStyle(), 20));
        label1.setText("Subtotal");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        subtotalLabel = new JLabel();
        subtotalLabel.setFont(new Font(subtotalLabel.getFont().getName(), subtotalLabel.getFont().getStyle(), 20));
        subtotalLabel.setText(" - ");
        panel1.add(subtotalLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setFont(new Font(label2.getFont().getName(), label2.getFont().getStyle(), 20));
        label2.setText("Tax (5%)");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        taxLabel = new JLabel();
        taxLabel.setFont(new Font(taxLabel.getFont().getName(), taxLabel.getFont().getStyle(), 20));
        taxLabel.setText(" -");
        panel1.add(taxLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setFont(new Font(label3.getFont().getName(), Font.BOLD, 24));
        label3.setText("Total");
        panel1.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        totalLabel = new JLabel();
        totalLabel.setFont(new Font(totalLabel.getFont().getName(), totalLabel.getFont().getStyle(), 24));
        totalLabel.setText(" -");
        panel1.add(totalLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(10, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setFont(new Font(label4.getFont().getName(), label4.getFont().getStyle(), 20));
        label4.setText("Credit");
        panel1.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        creditTextField = new JTextField();
        creditTextField.setFont(new Font(creditTextField.getFont().getName(), creditTextField.getFont().getStyle(), 20));
        creditTextField.setText("0");
        panel1.add(creditTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(20, -1), null, 0, false));
        applyCreditButton = new JButton();
        applyCreditButton.setFont(new Font(applyCreditButton.getFont().getName(), applyCreditButton.getFont().getStyle(), 20));
        applyCreditButton.setText("Apply Credit");
        panel1.add(applyCreditButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        quickPantComboBox = new JComboBox();
        quickPantComboBox.setEnabled(true);
        quickPantComboBox.setFont(new Font(quickPantComboBox.getFont().getName(), quickPantComboBox.getFont().getStyle(), 16));
        defaultPanel.add(quickPantComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        defaultPanel.add(spacer3, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(12, 2, new Insets(0, 0, 0, 0), -1, -1));
        defaultPanel.add(panel2, new GridConstraints(0, 0, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setFont(new Font(label5.getFont().getName(), Font.BOLD, 20));
        label5.setText("Invoice #");
        panel2.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        invoiceNumTextField = new JTextField();
        invoiceNumTextField.setFont(new Font(invoiceNumTextField.getFont().getName(), invoiceNumTextField.getFont().getStyle(), 20));
        panel2.add(invoiceNumTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setFont(new Font(label6.getFont().getName(), Font.BOLD, 20));
        label6.setText("Invoice Date");
        panel2.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        invoiceDatePanel = new JPanel();
        invoiceDatePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        invoiceDatePanel.setFont(new Font(invoiceDatePanel.getFont().getName(), invoiceDatePanel.getFont().getStyle(), 20));
        panel2.add(invoiceDatePanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, new Dimension(150, -1), null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setFont(new Font(label7.getFont().getName(), Font.BOLD, 20));
        label7.setText("Due Date");
        panel2.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        dueDatePanel = new JPanel();
        dueDatePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        dueDatePanel.setFont(new Font(dueDatePanel.getFont().getName(), dueDatePanel.getFont().getStyle(), 20));
        panel2.add(dueDatePanel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, new Dimension(150, -1), null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Customer Info", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(panel3.getFont().getName(), Font.BOLD, panel3.getFont().getSize())));
        addressBookButton = new JButton();
        addressBookButton.setFont(new Font(addressBookButton.getFont().getName(), addressBookButton.getFont().getStyle(), 20));
        addressBookButton.setText("Address Book");
        panel3.add(addressBookButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveCustomerInfoButton = new JButton();
        saveCustomerInfoButton.setFont(new Font(saveCustomerInfoButton.getFont().getName(), saveCustomerInfoButton.getFont().getStyle(), 20));
        saveCustomerInfoButton.setForeground(new Color(-65536));
        saveCustomerInfoButton.setText("Save Customer Info");
        panel3.add(saveCustomerInfoButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setFont(new Font(label8.getFont().getName(), Font.BOLD, 20));
        label8.setText("Name");
        panel3.add(label8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        customerNameTextField = new JTextField();
        customerNameTextField.setFont(new Font(customerNameTextField.getFont().getName(), customerNameTextField.getFont().getStyle(), 20));
        panel3.add(customerNameTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setFont(new Font(label9.getFont().getName(), Font.BOLD, 20));
        label9.setText("Phone");
        panel3.add(label9, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        customerPhoneTextField = new JTextField();
        customerPhoneTextField.setFont(new Font(customerPhoneTextField.getFont().getName(), customerPhoneTextField.getFont().getStyle(), 20));
        panel3.add(customerPhoneTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setFont(new Font(label10.getFont().getName(), Font.BOLD, 20));
        label10.setText("Email");
        panel3.add(label10, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        customerEmailTextField = new JTextField();
        customerEmailTextField.setFont(new Font(customerEmailTextField.getFont().getName(), customerEmailTextField.getFont().getStyle(), 20));
        panel3.add(customerEmailTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setFont(new Font(saveButton.getFont().getName(), Font.BOLD, 20));
        saveButton.setForeground(new Color(-65536));
        saveButton.setText("Save");
        panel4.add(saveButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 60), null, 0, false));
        emailButton = new JButton();
        emailButton.setFont(new Font(emailButton.getFont().getName(), emailButton.getFont().getStyle(), 20));
        emailButton.setText("Email");
        panel4.add(emailButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printButton = new JButton();
        printButton.setFont(new Font(printButton.getFont().getName(), printButton.getFont().getStyle(), 20));
        printButton.setText("Print");
        panel4.add(printButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        advancedDueDateField = new JTextField();
        panel2.add(advancedDueDateField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        advancedInvoiceDateField = new JTextField();
        panel2.add(advancedInvoiceDateField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        syncButton = new JButton();
        syncButton.setText("Sync");
        panel2.add(syncButton, new GridConstraints(11, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uploadButton = new JButton();
        uploadButton.setText("Upload");
        panel2.add(uploadButton, new GridConstraints(10, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        isPaidCheckBox = new JCheckBox();
        isPaidCheckBox.setFont(new Font(isPaidCheckBox.getFont().getName(), isPaidCheckBox.getFont().getStyle(), 20));
        isPaidCheckBox.setText("Paid");
        panel5.add(isPaidCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isPickedUpCheckBox = new JCheckBox();
        isPickedUpCheckBox.setFont(new Font(isPickedUpCheckBox.getFont().getName(), isPickedUpCheckBox.getFont().getStyle(), 20));
        isPickedUpCheckBox.setText("Picked Up");
        panel5.add(isPickedUpCheckBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isDoneCheckBox = new JCheckBox();
        isDoneCheckBox.setEnabled(true);
        isDoneCheckBox.setFont(new Font(isDoneCheckBox.getFont().getName(), isDoneCheckBox.getFont().getStyle(), 20));
        isDoneCheckBox.setText("Done");
        panel5.add(isDoneCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        emailSummaryButton = new JButton();
        emailSummaryButton.setFont(new Font(emailSummaryButton.getFont().getName(), emailSummaryButton.getFont().getStyle(), 20));
        emailSummaryButton.setText("Summary");
        panel2.add(emailSummaryButton, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel2.add(spacer4, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        quickJacketComboBox = new JComboBox();
        quickJacketComboBox.setEnabled(true);
        quickJacketComboBox.setFont(new Font(quickJacketComboBox.getFont().getName(), quickJacketComboBox.getFont().getStyle(), 16));
        defaultPanel.add(quickJacketComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        quickOthersComboBox = new JComboBox();
        quickOthersComboBox.setFont(new Font(quickOthersComboBox.getFont().getName(), quickOthersComboBox.getFont().getStyle(), 16));
        defaultPanel.add(quickOthersComboBox, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        quickDryCleanComboBox = new JComboBox();
        quickDryCleanComboBox.setFont(new Font(quickDryCleanComboBox.getFont().getName(), quickDryCleanComboBox.getFont().getStyle(), 16));
        defaultPanel.add(quickDryCleanComboBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        quickDressComboBox = new JComboBox();
        quickDressComboBox.setFont(new Font(quickDressComboBox.getFont().getName(), quickDressComboBox.getFont().getStyle(), 16));
        defaultPanel.add(quickDressComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        quickShirtComboBox = new JComboBox();
        quickShirtComboBox.setFont(new Font(quickShirtComboBox.getFont().getName(), quickShirtComboBox.getFont().getStyle(), 14));
        defaultPanel.add(quickShirtComboBox, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayoutManager(3, 7, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("    Settings    ", settingsPanel);
        setAddressesRadioButton = new JRadioButton();
        setAddressesRadioButton.setText("Addresses");
        settingsPanel.add(setAddressesRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        setQuickJacketsRadioButton = new JRadioButton();
        setQuickJacketsRadioButton.setText("Quick Jackets");
        settingsPanel.add(setQuickJacketsRadioButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        setQuickPantsRadioButton = new JRadioButton();
        setQuickPantsRadioButton.setText("Quick Pants");
        settingsPanel.add(setQuickPantsRadioButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        setQuickShirtsRadioButton = new JRadioButton();
        setQuickShirtsRadioButton.setText("Quick Shirts");
        settingsPanel.add(setQuickShirtsRadioButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        settingsPanel.add(scrollPane2, new GridConstraints(2, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        settingsTable = new JTable();
        scrollPane2.setViewportView(settingsTable);
        setQuickOthersRadioButton = new JRadioButton();
        setQuickOthersRadioButton.setText("Others");
        settingsPanel.add(setQuickOthersRadioButton, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        settingsPanel.add(panel6, new GridConstraints(0, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        settingsAddButton = new JButton();
        settingsAddButton.setText("Add");
        panel6.add(settingsAddButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingsSortButton = new JButton();
        settingsSortButton.setText("Sort");
        panel6.add(settingsSortButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingsSaveButton = new JButton();
        settingsSaveButton.setText("Save");
        panel6.add(settingsSaveButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel6.add(spacer5, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        setQuickDressRadioButton = new JRadioButton();
        setQuickDressRadioButton.setText("Quick Dress");
        settingsPanel.add(setQuickDressRadioButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        setQuickDryCleanRadioButton = new JRadioButton();
        setQuickDryCleanRadioButton.setText("Quick Dry Clean");
        settingsPanel.add(setQuickDryCleanRadioButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel7, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setFont(new Font(label11.getFont().getName(), Font.BOLD, label11.getFont().getSize()));
        label11.setText("Open Invoice Number");
        panel7.add(label11, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openInvoiceTextField = new JTextField();
        openInvoiceTextField.setFont(new Font(openInvoiceTextField.getFont().getName(), openInvoiceTextField.getFont().getStyle(), 18));
        panel7.add(openInvoiceTextField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 40), null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel7.add(spacer6, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        newInvoiceButton = new JButton();
        newInvoiceButton.setFont(new Font(newInvoiceButton.getFont().getName(), Font.BOLD, 20));
        newInvoiceButton.setForeground(new Color(-16645889));
        newInvoiceButton.setText("New Invoice");
        panel7.add(newInvoiceButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 50), null, 0, false));
        final Spacer spacer7 = new Spacer();
        topPanel.add(spacer7, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(10, -1), null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(setAddressesRadioButton);
        buttonGroup.add(setQuickJacketsRadioButton);
        buttonGroup.add(setQuickPantsRadioButton);
        buttonGroup.add(setQuickShirtsRadioButton);
        buttonGroup.add(setQuickOthersRadioButton);
        buttonGroup.add(setQuickDressRadioButton);
        buttonGroup.add(setQuickDryCleanRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

    private static class CustomCellRenderer extends DefaultTableCellRenderer {
        private final ItemListTableModel tableModel;

        CustomCellRenderer(ItemListTableModel tableModel) {
            this.tableModel = tableModel;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // bold the Groups rows
            if (tableModel.isRowGroup(row)) {
                Font oldFont = cellComponent.getFont();
                cellComponent.setFont(new Font(oldFont.getFontName(), Font.BOLD, oldFont.getSize()));
            }

            // todo: this code is not working properly
//            // red background Item rows with price equal to 0
//            if (tableModel.isZeroPriceItem(row)) {
//                cellComponent.setBackground(Color.PINK);
//            }

            return cellComponent;
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        StLogger.startApplicationLogging();

        SwingUtilities.invokeLater(() -> {
            try {
                new InvoiceMaker();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        });
    }
}
