package InvoiceMaker;

import Invoice.Invoice;
import Invoice.InvoiceSaveFileUpgrade;
import Invoice.InvoiceStore;
import Invoice.Item;
import Lib.DateRange;
import Lib.GuiUtils;
import Mail.MailConstants;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.text.ParseException;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class STDiagForm implements STTabbedForm {
    private JTextField mailTestHostTextField;
    private JTextField mailTestPortTextField;
    private JTextField mailTestUserTextField;
    private JPasswordField mailTestPasswordField;
    private JButton mailTestSendButton;
    private JPanel topPanel;
    private JButton upgradeSaveFileButton;
    private JTextField deleteInvoiceTextField;
    private JTextField archiveInvoiceYearTextField;
    private JTextField archiveInvoiceMonthTextField;
    private JButton archiveButton;
    private JButton generateFakeInvoicesButton;

    private static final Properties properties = new Properties();

    private final InvoiceStore invoiceStore;
    private final Properties config;

    static {
        properties.put("mail.smtp.host", MailConstants.DEFAULT_SMTP_HOST);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", MailConstants.DEFAULT_SMTP_PORT);

        // enable these three lines if using gmail
//        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        properties.put("mail.smtp.socketFactory", "80");
//        properties.put("mail.smtp.socketFactory.fallback", "false");

        properties.put("mail.smtp.starttls.enable", "true");
    }

    private static final String DEFAULT_TO = "nathanzheng87@hotmail.com";
    private static final String DEFAULT_CC = "nathanzheng87@gmail.com";
    private static final String DEFAULT_SUBJECT = "Testing Subject";
    private static final String DEFAULT_BODY = "Test email sent using JavaMailAPI";

    public STDiagForm(Properties config, InvoiceStore invoiceStore) {
        $$$setupUI$$$();

        this.config = config;
        this.invoiceStore = invoiceStore;

        initMail();

        upgradeSaveFileButton.addActionListener(e -> {
            try {
                System.out.println("Upgrading save files....");
                InvoiceSaveFileUpgrade.upgrade(config.getProperty("invoice.maker.save.directory"));
                System.out.println("Done!");
                GuiUtils.popMsg("Finished upgrading all save files");
            } catch (Exception e1) {
                GuiUtils.popError(e1.getMessage());
            }
        });

        deleteInvoiceTextField.addActionListener(e -> {
            String invoiceNum = deleteInvoiceTextField.getText().trim();
            String saveDirectory = config.getProperty(InvoiceMaker.SAVE_DIRECTORY_PROPERTY_NAME);
            File datFile = new File(saveDirectory + invoiceNum + ".dat");
            String outputDirectory = config.getProperty(InvoiceMaker.OUTPUT_DIRECTORY_PROPERTY_NAME);
            File htmlFile = new File(outputDirectory + invoiceNum + ".html");
            if (!datFile.exists() && !htmlFile.exists()) {
                GuiUtils.popError("Invoice does not exist");
                return;
            }

            String msg = "";
            if (datFile.exists()) {
                msg += datFile.getName();
            }
            if (htmlFile.exists()) {
                if (msg.isEmpty()) {
                    msg += htmlFile.getName();
                } else {
                    msg += " and " + htmlFile.getName();
                }
            }
            msg += " will be deleted. Are you sure? ";
            int dialogResult = JOptionPane.showConfirmDialog(null, msg, "Confirm", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                if (datFile.exists()) {
                    datFile.delete();
                }
                if (htmlFile.exists()) {
                    htmlFile.delete();
                }
                GuiUtils.popMsg("Deleted invoice " + invoiceNum);
            }
        });

        archiveButton.addActionListener(e -> {
            archiveInvoice();
        });

        generateFakeInvoicesButton.addActionListener(e -> {
            try {
                int numInvoicesGenerated = generateFakeInvoices();
                System.out.println("Generated " + numInvoicesGenerated + " invoices ");

            } catch (Exception e1) {
                JOptionPane.showMessageDialog(null, "failed");
            }
        });
    }

    private int generateFakeInvoices() throws Exception {
        String saveDir = config.getProperty(InvoiceMaker.FAKE_SAVE_DIRECTORY_PROPERTY_NAME);

        int n = 0;
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 365; i++) {
            calendar.add(Calendar.DATE, -1);    // push back a day

            int numInvoicesThisDay = new Random().nextInt(5);
            for (int iInvoice = 0; iInvoice < numInvoicesThisDay; iInvoice++) {
                Invoice invoice = new Invoice();
                String invoiceNum = "Fake" + i + "_" + iInvoice;
                invoice.setInvoiceNum(invoiceNum);
                invoice.setCustomerInfo("Customer " + i);
                invoice.setInvoiceDate(calendar.getTimeInMillis());
                Calendar dueCalendar = Calendar.getInstance();
                dueCalendar.setTime(calendar.getTime());
                dueCalendar.add(Calendar.DATE, 3);
                invoice.setDueDate(dueCalendar.getTimeInMillis());

                Item item = new Item("fake item", 1, new Random().nextDouble() * 50);
                invoice.addListEntry(item);

                File saveFile = new File(saveDir + invoice.getInvoiceNum() + ".dat");
                Invoice.write(saveFile, invoice);
                n++;
            }
        }

        JOptionPane.showMessageDialog(null, "Generated " + n + " fake invoices");
        return n;
    }

    private void archiveInvoice() {
        int year = Integer.parseInt(archiveInvoiceYearTextField.getText());
        int month = Integer.parseInt(archiveInvoiceMonthTextField.getText());
        YearMonth yearMonth = YearMonth.of(year, month);
        int monthEndDate = yearMonth.lengthOfMonth();

        try {
            DateRange dateRange = new DateRange("" + year + String.format("%02d", month) + "01",
                    "" + year + String.format("%02d", month) + String.format("%02d", monthEndDate));
            List<Invoice> invoicesToBeArchived = invoiceStore.filter(invoice -> dateRange.isInRange(invoice.getInvoiceDate()));
            if (invoicesToBeArchived.isEmpty()) {
                return;
            }

            // prepare directory if it doesn't exist
            String saveDir = config.getProperty(InvoiceMaker.SAVE_DIRECTORY_PROPERTY_NAME);
            String archiveDir = saveDir + year + String.format("%02d", month) + "/";
            File folder = new File(archiveDir);
            if (!folder.exists()) {
                folder.mkdir();
            }

            // copy files from save folder to archive folder
            for (Invoice invoice : invoicesToBeArchived) {
                File datFile = new File(saveDir + invoice.getInvoiceNum() + ".dat");
                if (datFile.exists()) {
                    boolean success = datFile.renameTo(new File(archiveDir + invoice.getInvoiceNum() + ".dat"));
                    if (success) {
                        System.out.println("archived invoice " + invoice.getInvoiceNum());
                    } else {
                        System.out.println("failed to archive invoice " + invoice.getInvoiceNum());
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void initMail() {
        mailTestHostTextField.setText(MailConstants.DEFAULT_SMTP_HOST);
        mailTestPortTextField.setText(MailConstants.DEFAULT_SMTP_PORT);
        mailTestUserTextField.setText(MailConstants.DEFAULT_USER);

        mailTestSendButton.addActionListener(e -> {
            Session session;
            try {
                session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        String pw = new String(mailTestPasswordField.getPassword());
                        return new PasswordAuthentication(MailConstants.DEFAULT_USER, pw);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(MailConstants.DEFAULT_USER));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(DEFAULT_TO));
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(DEFAULT_CC));
                message.setSubject(DEFAULT_SUBJECT);
                message.setText(DEFAULT_BODY);

                Transport.send(message);

                String successMsg = "Success - sent mail to " + DEFAULT_TO + ", forwarded to " + DEFAULT_CC;
                JOptionPane.showMessageDialog(null, successMsg);

            } catch (Exception e1) {
                e1.printStackTrace();

                String failedMsg = "Failed - check user name and password. If still doesn't work, contact administrator.";
                JOptionPane.showMessageDialog(null, failedMsg, "Error", JOptionPane.ERROR_MESSAGE);

            } finally {
                // do nothing for now - session doesn't have close() method.
            }
        });
    }


    @Override
    public JPanel getTopPanel() {
        return topPanel;
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
        topPanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Email", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(panel1.getFont().getName(), panel1.getFont().getStyle(), panel1.getFont().getSize())));
        final JLabel label1 = new JLabel();
        label1.setText("Host");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mailTestHostTextField = new JTextField();
        panel1.add(mailTestHostTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Port");
        panel1.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mailTestPortTextField = new JTextField();
        panel1.add(mailTestPortTextField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("User");
        panel1.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mailTestUserTextField = new JTextField();
        panel1.add(mailTestUserTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Password");
        panel1.add(label4, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mailTestPasswordField = new JPasswordField();
        panel1.add(mailTestPasswordField, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        mailTestSendButton = new JButton();
        mailTestSendButton.setText("Test");
        panel1.add(mailTestSendButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        topPanel.add(spacer1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        topPanel.add(spacer2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        upgradeSaveFileButton = new JButton();
        upgradeSaveFileButton.setText("Upgrade Save Files");
        panel2.add(upgradeSaveFileButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setFont(new Font(label5.getFont().getName(), Font.BOLD, 20));
        label5.setForeground(new Color(-65536));
        label5.setText("DO NOT USE THIS PAGE!!!!!!!");
        topPanel.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Delete Invoice");
        panel3.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteInvoiceTextField = new JTextField();
        panel3.add(deleteInvoiceTextField, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Archive Invoice");
        panel3.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        archiveInvoiceYearTextField = new JTextField();
        panel3.add(archiveInvoiceYearTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        archiveInvoiceMonthTextField = new JTextField();
        panel3.add(archiveInvoiceMonthTextField, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        archiveButton = new JButton();
        archiveButton.setText("Archive");
        panel3.add(archiveButton, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Year");
        panel3.add(label8, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Month");
        panel3.add(label9, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateFakeInvoicesButton = new JButton();
        generateFakeInvoicesButton.setText("Generate Fake Invoices for the past 365 days in the FakeSaves folder");
        panel3.add(generateFakeInvoicesButton, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }
}
