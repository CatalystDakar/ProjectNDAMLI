package com.splwg.cm.domain.testcase;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


import org.junit.Test;

import com.splwg.base.api.testers.ContextTestCase;

public class CmSendMail_Test extends ContextTestCase{

    @Test
    public void test() {
      
        try {
            // sets SMTP server properties
            Properties properties = new Properties();
            properties.put("mail.smtp.host", "40.101.80.178");
            //properties.put("mail.smtp.host", "mail.protection.outlook.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.ssl.trust", "40.101.80.178");
            //properties.setProperty("mail.transport.protocol", "smtp");
     
            // creates a new session with an authenticator
            Authenticator auth = new Authenticator()
            {
                public PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication("psrmdev@secusociale.sn", "Passer123");
                }
            };
     
            Session session = Session.getInstance(properties, auth);
     
            // creates a new e-mail message
            Message msg = new MimeMessage(session);
     
            msg.setFrom(new InternetAddress("psrmdev@secusociale.sn"));
            InternetAddress[] toAddresses = { new InternetAddress("denashkumar.m@4iapps.com") };
            msg.setRecipients(Message.RecipientType.TO, toAddresses);
            msg.setSubject("TEST");
            msg.setSentDate(new Date());
            msg.setText("TEST MESSAGE");
     
            // sends the e-mail
            Transport.send(msg);
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      
    }

}