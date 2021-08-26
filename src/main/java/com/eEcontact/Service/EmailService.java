package com.eEcontact.Service;


import java.util.Properties;
import javax.mail.*;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	


	
	public boolean sendEmail(String message,String subject,String to) {
		 //variable for gmail host
		boolean f=false;
		
		
		String host="smtp.gmail.com";
	    String from="bhanutest17@gmail.com";
		
		//get the system properties
		
		Properties properties = System.getProperties();
		
		System.out.println("PROPERTIES"+properties);
		
		//setting important information to properties object
		
		//host set
		
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//Step 1 to get session object.....
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
				return new javax.mail.PasswordAuthentication("bhanutest17@gmail.com", "Bhanu@7785");
			}
		
		
		
		});
		 session.setDebug(true);
		
		//step2: compose the message [text,multimmedia]
		 
		 
		MimeMessage mimeMessage = new  MimeMessage(session);
		try {
			
			//from email
			mimeMessage.setFrom(from);
			
			//adding recipients of meaasge
			
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			
			mimeMessage.setSubject(subject);
			
			//adding text to message
			mimeMessage.setText(message);
			
			//send
			//Step 3: send the message using Transport Class
			
			Transport.send(mimeMessage);
		 System.out.println("Email Sent Successfully");
			f=true;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
		
	}

}
