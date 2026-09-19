package com.zxinfotek.tms.infra.mail;

import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.CommonErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                       @Value("${spring.mail.username:}") String from) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
    }

    public boolean available() {
        return mailSenderProvider.getIfAvailable() != null;
    }

    public void send(String to, String subject, String content) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            throw new BizException(CommonErrorCode.COMMON_004, "msg.infra.mailNotConfigured");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            sender.send(message);
        } catch (RuntimeException e) {
            log.error("邮件发送失败，收件人={}", to, e);
            throw new BizException(CommonErrorCode.COMMON_004, "msg.infra.mailSendFailed");
        }
    }
}
