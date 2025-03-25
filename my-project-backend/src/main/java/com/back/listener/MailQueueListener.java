package com.back.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {//邮件监听队列
    @Resource
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
     String username;

    public void sendMailMessage(Map<String,Object> data) {
        String email =  data.get("email").toString();
        Integer code=(Integer) data.get("code");
        String type=(String) data.get("type");
        SimpleMailMessage message = switch (type){
            case "register" -> createSimpleMailMessage("欢迎注册我们的网站",
                    "邮件注册码为:"+code+"有效时间3分钟,为了保障您的安全,请不要向他人泄露验证码",email);
            case "reset"->createSimpleMailMessage("密码重置邮件","正在重置密码,验证码"+code,email);
            default -> null;
        };
        if(message==null) return;
        mailSender.send(message);
    }

    private SimpleMailMessage createSimpleMailMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}
