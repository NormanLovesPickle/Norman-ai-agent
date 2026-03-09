package com.norman.normanaiagent.tools;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;
import java.util.Properties;

public class EmailSendingTool {

    private JavaMailSender mailSender;
    private final String fromEmail;
    private final String password;
    private final String host;
    private final int port;

    public EmailSendingTool(String fromEmail, String password, String host, int port) {
        this.fromEmail = fromEmail != null ? fromEmail : "";
        this.password = password != null ? password : "";
        this.host = host != null ? host : "smtp.qq.com";
        this.port = port > 0 ? port : 465;
    }

    private void initMailSender() {
        if (mailSender == null && org.springframework.util.StringUtils.hasText(password)) {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(host);
            sender.setPort(port);
            sender.setUsername(fromEmail);
            sender.setPassword(password);
            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.debug", "false");
            mailSender = sender;
        }
    }

    @Tool(description = "Send a simple text email")
    public String sendTextEmail(
            @ToolParam(description = "Recipient email address") String toEmail,
            @ToolParam(description = "Email subject") String subject,
            @ToolParam(description = "Email content") String content) {
        try {
            initMailSender();
            if (mailSender == null) {
                return "邮件发送失败: 请配置 spring.mail 相关参数";
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            return "Text email sent successfully to " + toEmail;
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }

    @Tool(description = "Send an HTML email")
    public String sendHtmlEmail(
            @ToolParam(description = "Recipient email address") String toEmail,
            @ToolParam(description = "Email subject") String subject,
            @ToolParam(description = "HTML content") String htmlContent) {
        try {
            initMailSender();
            if (mailSender == null) {
                return "邮件发送失败: 请配置 spring.mail 相关参数";
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            return "HTML email sent successfully to " + toEmail;
        } catch (MessagingException e) {
            return "Failed to send HTML email: " + e.getMessage();
        }
    }

    @Tool(description = "Send an email with attachment")
    public String sendEmailWithAttachment(
            @ToolParam(description = "Recipient email address") String toEmail,
            @ToolParam(description = "Email subject") String subject,
            @ToolParam(description = "Email content") String content,
            @ToolParam(description = "File path of attachment") String attachmentPath) {
        try {
            initMailSender();
            if (mailSender == null) {
                return "邮件发送失败: 请配置 spring.mail 相关参数";
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content);
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);
            mailSender.send(message);
            return "Email with attachment sent successfully to " + toEmail;
        } catch (MessagingException e) {
            return "Failed to send email with attachment: " + e.getMessage();
        }
    }
}
