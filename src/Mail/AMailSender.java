package Mail;

import javax.mail.*;
import java.util.Properties;

// todo: this uses service@suntailoring.ca, is to be retired
public class AMailSender extends MailSender {

    private static final String password = "this is a password";

    private final Properties properties;

    public AMailSender() {
        super(MailConstants.DEFAULT_USER);

        properties = new Properties();
        properties.put("mail.smtp.host", MailConstants.DEFAULT_SMTP_HOST);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", MailConstants.DEFAULT_SMTP_PORT);

        // enable these three lines if using gmail todo: does these even work? We have GmailSender now
        //        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        //        properties.put("mail.smtp.socketFactory", "80");
        //        properties.put("mail.smtp.socketFactory.fallback", "false");

        properties.put("mail.smtp.starttls.enable", "true");
    }

    @Override
    protected Session createSession() {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailConstants.DEFAULT_USER, password);
            }
        });
    }

    @Override
    protected void send(Session session, Message message) throws MessagingException {
        Transport.send(message);
    }
}
