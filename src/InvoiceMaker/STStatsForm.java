package InvoiceMaker;

import Invoice.Invoice;
import Invoice.InvoiceStore;
import Lib.DateRange;
import Lib.StFormattedDateField;
import Utils.MathUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static Lib.GuiUtils.popError;

public class STStatsForm implements STTabbedForm {

    private static final String DATE_FORMAT_PROPERTY_NAME = "invoice.maker.date.format";
    private static final String SAVE_DIRECTORY_PROPERTY_NAME = "invoice.maker.save.directory";

    private JPanel topPanel;
    private JButton filterInvoiceByDateButton;
    private JTable invoiceTable;
    private JTextField clientNameTextField;
    private JPanel startDatePanel;
    private JPanel endDatePanel;
    private JButton filterDueTodayButton;
    private JButton filterDueOneWeekButton;
    private JButton listInvoiceTodayButton;
    private JButton listInvoiceLast3DaysButton;
    private JButton listInvoiceThisWeekButton;
    private JButton listInvoiceThisMonthButton;
    private JButton listInvoiceAllTimeButton;
    private JButton createBarChartButton;
    private JComboBox<ESalesBarChartType> barChartTypeComboBox;
    private JButton filterDue3DaysButton;
    private JCheckBox filterDueShowOnlyNotDoneCheckbox;
    private JButton filterDueTomorrowButton;
    private JCheckBox hideDryCleanOnlyCheckBox;
    private JLabel dashboardInTodayNumInvoicesLabel;
    private JLabel dashboardInTodayNumItemsLabel;
    private JLabel dashboardInTodayTotalLabel;
    private JLabel dashboardDueTmrNumInvoicesLabel;
    private JLabel dashboardDue3DaysNumInvoicesLabel;
    private JLabel dashboardDue3DaysNumItemsLabel;
    private JLabel dashboardDueTmrNumItemsLabel;
    private JLabel notDoneLabel;
    private JButton updateDashboardButton;

    private StFormattedDateField startDateSelector;
    private StFormattedDateField endDateSelector;

    private InvoiceMetaDataTableModel invoiceMetaDataTableModel;

    // used to fire invoice selection events
    private final List<InvoiceSelectionListener> invoiceSelectionListeners;

    public void addInvoiceSelectionListener(InvoiceSelectionListener listener) {
        invoiceSelectionListeners.add(listener);
    }

    public void removeInvoiceSelectionListener(InvoiceSelectionListener listener) {
        invoiceSelectionListeners.remove(listener);
    }

    public STStatsForm(Properties config, InvoiceStore invoiceStore) {
        $$$setupUI$$$();

        invoiceSelectionListeners = new ArrayList<>();

        // init date fields
        String datePattern = config.getProperty(DATE_FORMAT_PROPERTY_NAME);
        startDateSelector = new StFormattedDateField(datePattern);
        startDatePanel.setLayout(new BoxLayout(startDatePanel, BoxLayout.X_AXIS));
        startDatePanel.add(startDateSelector.getComponent());

        endDateSelector = new StFormattedDateField(datePattern);
        endDatePanel.setLayout(new BoxLayout(endDatePanel, BoxLayout.X_AXIS));
        endDatePanel.add(endDateSelector.getComponent());

        invoiceTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    if (row >= 0 && row < invoiceMetaDataTableModel.getRowCount()) {
                        Invoice selectedInvoice = invoiceMetaDataTableModel.getInvoiceAtRow(row);
                        invoiceSelectionListeners.stream().forEach(listener -> listener.invoiceSelected(selectedInvoice));
                    }
                }
            }
        });

        filterInvoiceByDateButton.addActionListener(e -> {
            try {
                final DateRange dateRange = new DateRange(startDateSelector.getText(), endDateSelector.getText());
                List<Invoice> loadedInvoices = invoiceStore.filter(invoice -> dateRange.isInRange(invoice.getInvoiceDate()));
                updateInvoiceMetaDataTable(loadedInvoices);

            } catch (ParseException e1) {
                popError("Enter a valid date range");
            }
        });

        listInvoiceTodayButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(
                    invoice -> DateRange.getToday().isInRange(invoice.getInvoiceDate()));
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        listInvoiceLast3DaysButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(
                    invoice -> DateRange.getTodayMinusDays(3).isInRange(invoice.getInvoiceDate()));
            loadedInvoices.sort(Invoice.IN_DATE_COMPARATOR);
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        listInvoiceThisWeekButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(
                    invoice -> DateRange.getTodayMinusDays(7).isInRange(invoice.getInvoiceDate()));
            loadedInvoices.sort(Invoice.IN_DATE_COMPARATOR);
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        listInvoiceThisMonthButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(
                    invoice -> DateRange.getTodayMinusDays(31).isInRange(invoice.getInvoiceDate()));
            loadedInvoices.sort(Invoice.IN_DATE_COMPARATOR);
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        listInvoiceAllTimeButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(invoice -> true);
            loadedInvoices.sort(Invoice.IN_DATE_COMPARATOR);
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        filterDueTodayButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(invoice -> DateRange.getToday().isInRange(invoice.getDueDate()));
            loadedInvoices = filterAndSortByDueDate(loadedInvoices);
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        filterDueTomorrowButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(invoice -> DateRange.getTodayPlusDays(1).isInRange(invoice.getDueDate()));
            loadedInvoices = filterAndSortByDueDate(loadedInvoices);
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        filterDue3DaysButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(invoice -> DateRange.getTodayPlusDays(3).isInRange(invoice.getDueDate()));
            loadedInvoices = filterAndSortByDueDate(loadedInvoices);
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        filterDueOneWeekButton.addActionListener(e -> {
            List<Invoice> loadedInvoices = invoiceStore.filter(invoice -> DateRange.getTodayPlusDays(7).isInRange(invoice.getDueDate()));
            loadedInvoices = filterAndSortByDueDate(loadedInvoices);
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        clientNameTextField.addActionListener(e1 -> {
            final String searchName = clientNameTextField.getText().trim().toLowerCase();
            List<Invoice> loadedInvoices = invoiceStore.filter(invoice -> invoice.getCustomerAddress().toLowerCase().contains(searchName));
            updateInvoiceMetaDataTable(loadedInvoices);
        });

        createBarChartButton.addActionListener(e -> {
            try {
                new SalesBarChart(invoiceStore, ESalesBarChartType.values()[barChartTypeComboBox.getSelectedIndex()]);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(null, "Failed");
            }
        });

        updateDashboardButton.addActionListener(e -> updateDashboard(new Summary(invoiceStore)));

    }

    private void updateDashboard(Summary summary) {
        dashboardInTodayNumInvoicesLabel.setText(Integer.toString(summary.getInTodayNumInvoices()));
        dashboardInTodayNumItemsLabel.setText(Integer.toString(summary.getInTodayNumItems()));
        dashboardInTodayTotalLabel.setText(MathUtil.formatCurrency(summary.getInTodayTotal()));

        Color labelForeground = summary.getDueTmrNumInvoices() > 0 ? Color.RED : Color.BLUE;
        notDoneLabel.setForeground(labelForeground);

        dashboardDueTmrNumInvoicesLabel.setText(Integer.toString(summary.getDueTmrNumInvoices()));
        dashboardDueTmrNumItemsLabel.setText(Integer.toString(summary.getDueTmrNumItems()));
        dashboardDueTmrNumInvoicesLabel.setForeground(labelForeground);
        dashboardDueTmrNumItemsLabel.setForeground(labelForeground);

        dashboardDue3DaysNumInvoicesLabel.setText(Integer.toString(summary.getDue3DaysNumInvoices()));
        dashboardDue3DaysNumItemsLabel.setText(Integer.toString(summary.getDue3DaysNumItems()));
        labelForeground = summary.getDue3DaysNumInvoices() > 0 ? Color.RED : Color.BLUE;
        dashboardDue3DaysNumInvoicesLabel.setForeground(labelForeground);
        dashboardDue3DaysNumItemsLabel.setForeground(labelForeground);
    }


    private List<Invoice> filterAndSortByDueDate(List<Invoice> invoices) {
        List<Invoice> retVal = invoices;
        if (filterDueShowOnlyNotDoneCheckbox.isSelected()) {
            retVal = invoices.stream().filter(invoice -> !invoice.isDone()).collect(Collectors.toList());
        }
        if (hideDryCleanOnlyCheckBox.isSelected()) {
            retVal = retVal.stream().filter(invoice -> invoice.hasNoneDryCleanItem()).collect(Collectors.toList());
        }
        retVal.sort(Invoice.DUE_DATE_COMPARATOR);
        return retVal;
    }

    private void updateInvoiceMetaDataTable(List<Invoice> loadedInvoices) {
        invoiceMetaDataTableModel = new InvoiceMetaDataTableModel(loadedInvoices);
        invoiceTable.setModel(invoiceMetaDataTableModel);
        // header
        TableCellRenderer headerRenderer = invoiceTable.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
        }
        // column width
        TableColumn column = null;
        for (int colIndex = 0; colIndex < InvoiceMetaDataTableModel.Column.values().length; colIndex++) {
            column = invoiceTable.getColumnModel().getColumn(colIndex);
            if (colIndex == InvoiceMetaDataTableModel.Column.Customer.ordinal()) {
                column.setPreferredWidth(200);
            } else if (colIndex == InvoiceMetaDataTableModel.Column.Items.ordinal()) {
                column.setPreferredWidth(400);
            } else if (colIndex == InvoiceMetaDataTableModel.Column.Total.ordinal() ||
                    colIndex == InvoiceMetaDataTableModel.Column.Paid.ordinal() ||
                    colIndex == InvoiceMetaDataTableModel.Column.Done.ordinal() ||
                    colIndex == InvoiceMetaDataTableModel.Column.PickedUp.ordinal()) {
                column.setPreferredWidth(20);
            } else {
                column.setPreferredWidth(50);
            }
        }

        invoiceMetaDataTableModel.fireTableDataChanged();
    }

    @Override
    public JPanel getTopPanel() {
        return topPanel;
    }

    private void createUIComponents() {
        barChartTypeComboBox = new JComboBox<>();
        for (ESalesBarChartType type : ESalesBarChartType.values()) {
            barChartTypeComboBox.addItem(type);
        }
        barChartTypeComboBox.setSelectedIndex(0);
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
        createUIComponents();
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Invoice List", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(panel1.getFont().getName(), Font.BOLD, panel1.getFont().getSize())));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        invoiceTable = new JTable();
        scrollPane1.setViewportView(invoiceTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("List Invoices Came In");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listInvoiceTodayButton = new JButton();
        listInvoiceTodayButton.setText("Today");
        panel2.add(listInvoiceTodayButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listInvoiceLast3DaysButton = new JButton();
        listInvoiceLast3DaysButton.setText("Last 3 Days");
        panel2.add(listInvoiceLast3DaysButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listInvoiceThisWeekButton = new JButton();
        listInvoiceThisWeekButton.setText("This Week");
        panel2.add(listInvoiceThisWeekButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listInvoiceThisMonthButton = new JButton();
        listInvoiceThisMonthButton.setText("This Month");
        panel2.add(listInvoiceThisMonthButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listInvoiceAllTimeButton = new JButton();
        listInvoiceAllTimeButton.setText("All Time");
        panel2.add(listInvoiceAllTimeButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Start");
        panel3.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startDatePanel = new JPanel();
        startDatePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(startDatePanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(150, -1), null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("End");
        panel3.add(label3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        endDatePanel = new JPanel();
        endDatePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(endDatePanel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(150, -1), null, null, 0, false));
        filterInvoiceByDateButton = new JButton();
        filterInvoiceByDateButton.setText("List Invoice In This Data Range");
        panel3.add(filterInvoiceByDateButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("List by Customer Info");
        panel4.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clientNameTextField = new JTextField();
        panel4.add(clientNameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 7, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("List Invoices Due");
        panel5.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filterDueTodayButton = new JButton();
        filterDueTodayButton.setText("Today");
        panel5.add(filterDueTodayButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filterDueOneWeekButton = new JButton();
        filterDueOneWeekButton.setText("A Week");
        panel5.add(filterDueOneWeekButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filterDue3DaysButton = new JButton();
        filterDue3DaysButton.setText("3 Days");
        panel5.add(filterDue3DaysButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filterDueShowOnlyNotDoneCheckbox = new JCheckBox();
        filterDueShowOnlyNotDoneCheckbox.setText("Show Only Not Done");
        panel5.add(filterDueShowOnlyNotDoneCheckbox, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filterDueTomorrowButton = new JButton();
        filterDueTomorrowButton.setText("Tomorrow");
        panel5.add(filterDueTomorrowButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideDryCleanOnlyCheckBox = new JCheckBox();
        hideDryCleanOnlyCheckBox.setText("Hide Dry Clean Only");
        panel5.add(hideDryCleanOnlyCheckBox, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(6, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel6, new GridConstraints(0, 1, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Dashboard", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(panel6.getFont().getName(), Font.BOLD, panel6.getFont().getSize())));
        final JLabel label6 = new JLabel();
        label6.setText("In Today");
        panel6.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Due Tomorrow");
        panel6.add(label7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Due In 3 Days");
        panel6.add(label8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dashboardInTodayNumInvoicesLabel = new JLabel();
        dashboardInTodayNumInvoicesLabel.setFont(new Font(dashboardInTodayNumInvoicesLabel.getFont().getName(), dashboardInTodayNumInvoicesLabel.getFont().getStyle(), 16));
        dashboardInTodayNumInvoicesLabel.setForeground(new Color(-65536));
        dashboardInTodayNumInvoicesLabel.setHorizontalAlignment(11);
        dashboardInTodayNumInvoicesLabel.setText("-");
        panel6.add(dashboardInTodayNumInvoicesLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Invoices");
        panel6.add(label9, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dashboardInTodayNumItemsLabel = new JLabel();
        dashboardInTodayNumItemsLabel.setFont(new Font(dashboardInTodayNumItemsLabel.getFont().getName(), dashboardInTodayNumItemsLabel.getFont().getStyle(), 16));
        dashboardInTodayNumItemsLabel.setForeground(new Color(-65536));
        dashboardInTodayNumItemsLabel.setHorizontalAlignment(11);
        dashboardInTodayNumItemsLabel.setText("-");
        panel6.add(dashboardInTodayNumItemsLabel, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Items");
        panel6.add(label10, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dashboardInTodayTotalLabel = new JLabel();
        dashboardInTodayTotalLabel.setFont(new Font(dashboardInTodayTotalLabel.getFont().getName(), dashboardInTodayTotalLabel.getFont().getStyle(), 16));
        dashboardInTodayTotalLabel.setForeground(new Color(-65536));
        dashboardInTodayTotalLabel.setHorizontalAlignment(11);
        dashboardInTodayTotalLabel.setText("-");
        panel6.add(dashboardInTodayTotalLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("$");
        panel6.add(label11, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dashboardDueTmrNumInvoicesLabel = new JLabel();
        dashboardDueTmrNumInvoicesLabel.setFont(new Font(dashboardDueTmrNumInvoicesLabel.getFont().getName(), dashboardDueTmrNumInvoicesLabel.getFont().getStyle(), 16));
        dashboardDueTmrNumInvoicesLabel.setForeground(new Color(-65536));
        dashboardDueTmrNumInvoicesLabel.setHorizontalAlignment(11);
        dashboardDueTmrNumInvoicesLabel.setText("-");
        panel6.add(dashboardDueTmrNumInvoicesLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Invoices");
        panel6.add(label12, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Items");
        panel6.add(label13, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Items");
        panel6.add(label14, new GridConstraints(5, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Invoices");
        panel6.add(label15, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dashboardDue3DaysNumInvoicesLabel = new JLabel();
        dashboardDue3DaysNumInvoicesLabel.setFont(new Font(dashboardDue3DaysNumInvoicesLabel.getFont().getName(), dashboardDue3DaysNumInvoicesLabel.getFont().getStyle(), 16));
        dashboardDue3DaysNumInvoicesLabel.setForeground(new Color(-65536));
        dashboardDue3DaysNumInvoicesLabel.setHorizontalAlignment(11);
        dashboardDue3DaysNumInvoicesLabel.setText("-");
        panel6.add(dashboardDue3DaysNumInvoicesLabel, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        dashboardDue3DaysNumItemsLabel = new JLabel();
        dashboardDue3DaysNumItemsLabel.setFont(new Font(dashboardDue3DaysNumItemsLabel.getFont().getName(), dashboardDue3DaysNumItemsLabel.getFont().getStyle(), 16));
        dashboardDue3DaysNumItemsLabel.setForeground(new Color(-65536));
        dashboardDue3DaysNumItemsLabel.setHorizontalAlignment(11);
        dashboardDue3DaysNumItemsLabel.setText("-");
        panel6.add(dashboardDue3DaysNumItemsLabel, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        dashboardDueTmrNumItemsLabel = new JLabel();
        dashboardDueTmrNumItemsLabel.setFont(new Font(dashboardDueTmrNumItemsLabel.getFont().getName(), dashboardDueTmrNumItemsLabel.getFont().getStyle(), 16));
        dashboardDueTmrNumItemsLabel.setForeground(new Color(-65536));
        dashboardDueTmrNumItemsLabel.setHorizontalAlignment(11);
        dashboardDueTmrNumItemsLabel.setText("-");
        panel6.add(dashboardDueTmrNumItemsLabel, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        notDoneLabel = new JLabel();
        notDoneLabel.setFont(new Font(notDoneLabel.getFont().getName(), Font.BOLD, notDoneLabel.getFont().getSize()));
        notDoneLabel.setForeground(new Color(-65536));
        notDoneLabel.setText("Not Done Invoices (not including dry clean)");
        panel6.add(notDoneLabel, new GridConstraints(3, 0, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateDashboardButton = new JButton();
        updateDashboardButton.setText("Update");
        panel6.add(updateDashboardButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel6.add(spacer1, new GridConstraints(0, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Charts"));
        createBarChartButton = new JButton();
        createBarChartButton.setText("View Sales Statistics");
        panel7.add(createBarChartButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel7.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        panel7.add(barChartTypeComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }
}
          