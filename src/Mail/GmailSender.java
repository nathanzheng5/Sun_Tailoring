package Mail;

import InvoiceMaker.StConfig;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.ArrayList;
import java.util.Properties;

public class GmailSender extends MailSender {

    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";

    private final Properties properties;

    private final String user;
    private final String password;

    public static final GmailSender DEFAULT = new GmailSender("suntailoringvancouver@gmail.com",
            StConfig.getInstance().getProperty("st.gmail.password"));

    public GmailSender(String user, String password) {
        super(user);
        this.user = user;
        this.password = password;

        properties = System.getProperties();
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.user", user);
        properties.put("mail.smtp.password", password);
        properties.put("mail.smtp.port", PORT);
        properties.put("mail.smtp.auth", true);
    }

    @Override
    protected Session createSession() {
        return Session.getInstance(properties, null);
    }

    @Override
    protected void send(Session session, Message message) throws MessagingException {
        Transport transport = session.getTransport("smtp");
        transport.connect("smtp.gmail.com", user, password);
        System.out.println("Transport: " + transport.toString());
        transport.sendMessage(message, message.getAllRecipients());
    }
}
