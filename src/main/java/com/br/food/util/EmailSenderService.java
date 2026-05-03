package com.br.food.util;

import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSenderService {

	private final JavaMailSender javaMailSender;
	private final Environment environment;

	public EmailSenderService(JavaMailSender javaMailSender, Environment environment) {
		this.javaMailSender = javaMailSender;
		this.environment = environment;
	}

	public void send(String to, String subject, String content, Boolean html) {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
		try {
			String from = environment.getProperty("spring.mail.username");
			message.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content, html);
			javaMailSender.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void sendEmailWithAttachment(String to, String subject, String content, byte[] file, String fileName,
			Boolean html) throws MessagingException {

		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, html);
		String from = environment.getProperty("spring.mail.username");

		message.setFrom(from);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(content);

		InputStreamSource inputStreamSource = new ByteArrayResource(file);
		helper.addAttachment(fileName, inputStreamSource);

		javaMailSender.send(message);
	}
}