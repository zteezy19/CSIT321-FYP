package com.uow.FYP_23_S1_11.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(String from, String to, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {
        String senderName = "GoDoctor";

        MimeMessage _message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(_message);

        helper.setFrom(from, senderName);
        helper.setTo(to);
        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(_message);
    }

}
