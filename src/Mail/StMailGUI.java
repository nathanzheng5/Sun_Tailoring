package Mail;

import AddressBook.AddressBook;
import AddressBook.AddressBookGUI;
import AddressBook.AddressSelectionListener;
import InvoiceMaker.StFileFilter;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import java.util.List;

import static Lib.GuiUtils.popError;

public class StMailGUI extends JFrame {

    private JPanel topPanel;
    private JButton recipientButton;
    private JTextField recipientTextField;
    private JButton sendButton;
    private JTextField subjectTextField;
    private JButton attachButton;
    private JTextField attachmentTextField;
    private JTextArea bodyTextArea;
    private JButton ccButton;
    private JTextField forwardTextField;

    private Set<MailEventListener> mailEventListeners = new HashSet<>();

    private final String mailOption;

    public void addMailEventListener(MailEventListener listener) {
        mailEventListeners.add(listener);
    }

    private MailEventListener.Status status = MailEventListener.Status.CANCELLED;

    private static final String MAIL_SUBJECT_PROPERTY_NAME = "invoice.maker.default.mail.subject";
    private static final String MAIL_MESSAGE_PROPERTY_NAME = "invoice.maker.default.mail.message";
    private static final String MAIL_OPTION_PROPERTY_NAME = "invoice.maker.default.mail.option";

    public StMailGUI(AddressBook addressBook, String defaultTo, String defaultCc, String defaultAttachment, String invoiceNum, Properties config) {

        mailOption = config.getProperty(MAIL_OPTION_PROPERTY_NAME, "gmail");

        setTitle("Mail - " + mailOption);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(topPanel);
        pack();
        setVisible(true);

        // trigger mailCancelled() event when window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                for (MailEventListener listener : mailEventListeners) {
                    listener.mailSent(status);
                }
                mailEventListeners.clear(); // may not be necessary
            }
        });

        addComponentListeners(addressBook);

        recipientTextField.setText(defaultTo);
        forwardTextField.setText(defaultCc);
        attachmentTextField.setText(defaultAttachment);

        String subject = String.format(config.getProperty(MAIL_SUBJECT_PROPERTY_NAME), invoiceNum);
        subjectTextField.setText(subject);
        bodyTextArea.setText(config.getProperty(MAIL_MESSAGE_PROPERTY_NAME));
    }

    private void addComponentListeners(final AddressBook addressBook) {
        final AddressSelectionListener addressSelectionHandler = contactInfo -> {
            if (contactInfo != null) {
                recipientTextField.setText(contactInfo.getEmail());
            }
        };
        recipientButton.addActionListener(e -> {
            AddressBookGUI addressBookGUI = new AddressBookGUI(addressBook);
            addressBookGUI.addSelectionListener(addressSelectionHandler);
        });

        final AddressSelectionListener ccSelectionHandler = contactInfo -> {
            if (contactInfo != null) {
                forwardTextField.setText(contactInfo.getEmail());
            }
        };
        ccButton.addActionListener(e -> {
            AddressBookGUI addressBookGUI = new AddressBookGUI(addressBook);
            addressBookGUI.addSelectionListener(ccSelectionHandler);
        });

        attachButton.addActionListener(e -> {
            File directory = new File("./InvoiceOutputs");
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Invoice .html file");
            fileChooser.setFileFilter(new StFileFilter(".html"));
            fileChooser.setCurrentDirectory(directory);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                attachmentTextField.setText(file.getAbsolutePath());
            }
        });

        sendButton.addActionListener(e -> {
            String to = recipientTextField.getText().trim();
            if (to.isEmpty()) {
                popError("Enter an email address");
                return;
            }

            List<String> ccEmails = new ArrayList<>();
            String ccString = forwardTextField.getText().trim();
            if (!ccString.isEmpty()) {
                String[] ccStrings = ccString.split(";");
                for (String ccEmail : ccStrings) {
                    ccEmails.add(ccEmail.trim());
                }
            }
            String subject = subjectTextField.getText().trim();
            String bodyText = bodyTextArea.getText().trim();
            String attachmentFileString = attachmentTextField.getText().trim();
            if (attachmentFileString.isEmpty()) {
                attachmentFileString = null;
            }

            MailSender sender = GmailSender.DEFAULT;

            try {
                topPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                sender.sendMail(to, ccEmails, subject, bodyText, attachmentFileString);

                status = MailEventListener.Status.SUCCESS;
                topPanel.setCursor(Cursor.getDefaultCursor());

                dispose();  // window listener will trigger mailSent()

            } catch (MailException exception) {
                popError("Mail send failed - " + exception.getMessage());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        TestMailGUIListener listener = new TestMailGUIListener();
        String to = "nathanzheng87@hotmail.com";
        String cc = "nathanzheng87@gmail.com";
        String attachment = ".\\test\\Mail\\testAttachment";
        File file = new File(attachment);
        attachment = file.getAbsolutePath();
        StMailGUI gui = new StMailGUI(new AddressBook(), to, cc, attachment, "12345", new Properties());
        gui.addMailEventListener(listener);
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
        topPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        recipientButton = new JButton();
        recipientButton.setText("To...");
        panel1.add(recipientButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        recipientTextField = new JTextField();
        panel1.add(recipientTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Send");
        panel1.add(sendButton, new GridConstraints(0, 2, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Subject");
        panel1.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        subjectTextField = new JTextField();
        panel1.add(subjectTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        attachButton = new JButton();
        attachButton.setText("Attach...");
        panel1.add(attachButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        attachmentTextField = new JTextField();
        panel1.add(attachmentTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(500, -1), null, 0, false));
        ccButton = new JButton();
        ccButton.setText("CC...");
        panel1.add(ccButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        forwardTextField = new JTextField();
        panel1.add(forwardTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        topPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(800, 300), null, 0, false));
        bodyTextArea = new JTextArea();
        bodyTextArea.setWrapStyleWord(false);
        scrollPane1.setViewportView(bodyTextArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

    private static class TestMailGUIListener implements MailEventListener {
        @Override
        public void mailSent(Status status) {
            System.out.println("Mail Sent: " + status);
        }
    }
}
