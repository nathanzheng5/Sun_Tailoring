package Mail;

import InvoiceMaker.StConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

import static junit.framework.Assert.fail;

public class MailTest {

    static final String to = "nathanzheng87@hotmail.com";
    static final String cc = "nathanzheng87@gmail.com";
    static final String password = StConfig.getInstance().getProperty("st.gmail.password");

    static final String subject = "Testing Subject";
    static final String bodyText = "Test email sent using JavaMailAPI";
    static final String attachmentFileName = "./test/Mail/testAttachment.txt";

    private static Properties properties;

    private Session session;

    @BeforeClass
    public static void setupProperties() {
        properties = new Properties();
        properties.put("mail.smtp.host", MailConstants.DEFAULT_SMTP_HOST);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", MailConstants.DEFAULT_SMTP_PORT);

        // enable these three lines if using gmail
//        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        properties.put("mail.smtp.socketFactory", "80");
//        properties.put("mail.smtp.socketFactory.fallback", "false");

        properties.put("mail.smtp.starttls.enable", "true");
    }

    @Before
    public void setupSession() {
        // Get the Session object.
        session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailConstants.DEFAULT_USER, password);
            }
        });
    }

    @Test
    public void testSendSimpleMail() {
        try {
           Message message = new MimeMessage(session);
           message.setFrom(new InternetAddress(MailConstants.DEFAULT_USER));
           message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
           message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
           message.setSubject(subject);
           message.setText(bodyText);

           Transport.send(message);
           System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSendAttachmentMail() {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(MailConstants.DEFAULT_USER));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);

            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(bodyText);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);

            bodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachmentFileName);
            bodyPart.setDataHandler(new DataHandler(source));
            bodyPart.setFileName(attachmentFileName);
            multipart.addBodyPart(bodyPart);

            msg.setContent(multipart);

            Transport.send(msg);
            System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            e.printStackTrace();
            fail();
        }
    }

}
