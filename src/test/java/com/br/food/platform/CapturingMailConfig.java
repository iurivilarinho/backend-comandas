package com.br.food.platform;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/** Keeps every e-mail the application "sends" so tests can read the set-password link. */
@Configuration
public class CapturingMailConfig {

	public record CapturedMail(String to, String subject, String html) {
	}

	public static final List<CapturedMail> MAILS = new CopyOnWriteArrayList<>();

	@Bean
	JavaMailSender javaMailSender() {
		return new JavaMailSenderImpl() {
			@Override
			protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) {
				for (MimeMessage message : mimeMessages) {
					try {
						MAILS.add(new CapturedMail(message.getAllRecipients()[0].toString(), message.getSubject(),
								String.valueOf(message.getContent())));
					} catch (MessagingException | IOException e) {
						throw new IllegalStateException(e);
					}
				}
			}
		};
	}
}
