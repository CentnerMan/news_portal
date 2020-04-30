package ru.geek.news_portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static ru.geek.news_portal.configs.MailSenderConfig.*;

@SpringBootApplication
public class NewsPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsPortalApplication.class, args);
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(MAIL_HOST);
        sender.setPort(MAIL_PORT);
        sender.setUsername(USERNAME);
        sender.setPassword(PASSWORD);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return sender;
    }
}
