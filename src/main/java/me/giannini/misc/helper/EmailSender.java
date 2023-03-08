package me.giannini.misc.helper;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender {

  public static void main(final String[] args) throws AddressException, MessagingException {
    final Properties prop = new Properties();
    prop.put("mail.smtp.auth", true);
    // prop.put("mail.smtp.starttls.enable", "true");
    // prop.put("mail.smtp.ssl.trust", "smtp.example.com");
    prop.put("mail.smtp.host", "smtp.example.com");
    prop.put("mail.smtp.port", "25");

    final Session session = Session.getInstance(prop);

    final Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress("sender@example.com"));
    message.setRecipients(
        Message.RecipientType.TO, InternetAddress.parse("recipient@example.com"));
    message.setSubject("Mail Subject");

    final String msg = "This is my test email using JavaMailer";

    final MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

    final Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(mimeBodyPart);

    message.setContent(multipart);

    Transport.send(message);
  }
}
