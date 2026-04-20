package com.lenyan.lenaiagent.tools;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;

@Component
@Slf4j
public class EmailSendingTool {

    private JavaMailSender mailSender;

    @Value("${spring.mail.username:2456437685@qq.com}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.host:smtp.qq.com}")
    private String host;

    @Value("${spring.mail.port:465}")
    private int port;

    private synchronized JavaMailSender getMailSender() {
        if (mailSender == null) {
            log.info("初始化邮件发送器: host={}, port={}, username={}", host, port, fromEmail);
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(host);
            sender.setPort(port);
            sender.setUsername(fromEmail);
            sender.setPassword(password);

            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.debug", "true"); // 开启调试模式，查看详细日志

            mailSender = sender;
            log.info("邮件发送器初始化完成");
        }
        return mailSender;
    }

    @Tool(description = "Send a simple text email")
    public String sendTextEmail(
            @ToolParam(description = "Recipient email address") String toEmail,
            @ToolParam(description = "Email subject") String subject,
            @ToolParam(description = "Email content") String content) {

        try {
            log.info("准备发送邮件: to={}, subject={}", toEmail, subject);
            JavaMailSender sender = getMailSender();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            sender.send(message);
            log.info("邮件发送成功: to={}", toEmail);
            return "Text email sent successfully to " + toEmail;
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, error={}", toEmail, e.getMessage(), e);
            return "Failed to send email: " + e.getMessage();
        }
    }

    @Tool(description = "Send an HTML email")
    public String sendHtmlEmail(
            @ToolParam(description = "Recipient email address") String toEmail,
            @ToolParam(description = "Email subject") String subject,
            @ToolParam(description = "HTML content") String htmlContent) {

        try {
            log.info("准备发送HTML邮件: to={}, subject={}", toEmail, subject);
            JavaMailSender sender = getMailSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            sender.send(message);
            log.info("HTML邮件发送成功: to={}", toEmail);
            return "HTML email sent successfully to " + toEmail;
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败: to={}, error={}", toEmail, e.getMessage(), e);
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
            log.info("准备发送带附件邮件: to={}, subject={}, attachment={}", toEmail, subject, attachmentPath);
            JavaMailSender sender = getMailSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content);

            File file = new File(attachmentPath);
            if (!file.exists()) {
                return "Failed to send email: Attachment file not found: " + attachmentPath;
            }

            FileSystemResource fileResource = new FileSystemResource(file);
            helper.addAttachment(fileResource.getFilename(), fileResource);

            sender.send(message);
            log.info("带附件邮件发送成功: to={}", toEmail);
            return "Email with attachment sent successfully to " + toEmail;
        } catch (MessagingException e) {
            log.error("带附件邮件发送失败: to={}, error={}", toEmail, e.getMessage(), e);
            return "Failed to send email with attachment: " + e.getMessage();
        }
    }
}
