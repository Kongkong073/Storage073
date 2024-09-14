package com.storage073.service;

import com.storage073.entity.Constants;
import com.storage073.mapper.EmailCheckCodeMapper;
import com.storage073.model.EmailCheckCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Service
public class CheckCodeService {

    @Autowired
    private EmailCheckCodeMapper emailCheckCodeMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    public String generateEmailCheckCode(String email) {
        String checkCode = generateRandomCode(5);

        // 设置过期时间为当前时间加1分钟
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 3);
        Date expireAt = calendar.getTime();

        EmailCheckCode emailCheckCode = new EmailCheckCode();
        emailCheckCode.setEmail(email);
        emailCheckCode.setCheckcode(checkCode);
        emailCheckCode.setCreatedAt(new Date());
        emailCheckCode.setExpireAt(expireAt);
        emailCheckCode.setStatus((byte) 1);

        // 使之前发送的验证码失效
        emailCheckCodeMapper.invalidatePreviousCheckCodes(email, checkCode);

        emailCheckCodeMapper.insertCheckCode(emailCheckCode);

        sendEmail(email, checkCode);
        return checkCode;
    }

    private void sendEmail(String email, String checkCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(email);
        message.setSubject("Your Verification Code");
        message.setText("Your verification code is: " + checkCode + ".\n This code will expire in "
                + Constants.EMAIL_CHECK_CODE_EXPIRE_TIME + " minutes.");
        mailSender.send(message);
    }

    @Async("taskExecutor")
    public void sendEmailAsync(String email, String checkCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(email);
        message.setSubject("Your Verification Code");
        message.setText("Your verification code is: " + checkCode);
        mailSender.send(message);
    }
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public boolean validateCheckCode(String email, String inputCode) {
        // 使所有已过期的验证码失效
        emailCheckCodeMapper.invalidateExpiredCheckCodes(email);

        EmailCheckCode emailCheckCode = emailCheckCodeMapper.findByEmailAndCheckcode(email, inputCode);

        if (emailCheckCode != null) {
            emailCheckCodeMapper.updateStatus(email, inputCode, (byte) 0); // 验证后使该验证码失效
            return true;
        }
        return false;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Async("taskExecutor")
    public void cleanExpiredCheckCodes() {
        emailCheckCodeMapper.deleteExpiredCheckCodes();
    }
}
