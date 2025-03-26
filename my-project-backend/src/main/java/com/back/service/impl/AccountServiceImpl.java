package com.back.service.impl;

import com.back.entity.dto.Account;
import com.back.entity.vo.request.ConfirmResetVo;
import com.back.entity.vo.request.EmailRegisterVo;
import com.back.entity.vo.request.EmailResetVo;
import com.back.mapper.AccountMapper;
import com.back.service.AccountService;
import com.back.utils.Const;
import com.back.utils.FlowUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Wrapper;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account>implements AccountService {
    @Resource
    AmqpTemplate amqpTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    FlowUtils flowUtils;
    @Resource
    BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        Account account=this.findAccountByUsernameOrEmail(username);
        if(account==null){
            throw new UsernameNotFoundException("username");
        }
        return User.withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
    @Override
    public Account findAccountByUsernameOrEmail(String text) {
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if(!this.verifyLimit(ip)){
                return "请求频繁,请稍后再试";
            }
            Random random = new Random();
            int code= random.nextInt(899999)+100000;
            Map<String,Object> data=Map.of("type",type,"email",email,"code",code);
            amqpTemplate.convertAndSend("mail",data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA+email,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public String registerEmailAccount(EmailRegisterVo vo) {
        String email=vo.getEmail();
       String username=vo.getUsername();
       String key=Const.VERIFY_EMAIL_DATA+email;
       String code=stringRedisTemplate.opsForValue().get(key);
        if(code==null){
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())) {return "验证码错误,请重新输入";}
        if(this.existsAccountByEmail(email)){return "此邮件已经被注册";}
        if(this.existsAccountByUsername(vo.getUsername())){return "此用户名已经被注册";}
        String password=bCryptPasswordEncoder.encode(vo.getPassword());
        Account account=new Account(null, username, password, email, "user");
        if (this.save(account)) {
            stringRedisTemplate.delete(key);
            return null;
        }else {
            return "内部错误,请联系管理员";
        }

    }

    @Override
    public String resetConfirm(ConfirmResetVo vo) {
        String email=vo.getEmail();
        String code= stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if(code==null){
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())) {return "验证码错误,请重新输入";}
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVo vo) {
        String email=vo.getEmail();
        String verify=this.resetConfirm(new ConfirmResetVo(email, vo.getCode()));
        if(verify!=null){return verify;}

        String password=bCryptPasswordEncoder.encode(vo.getPassword());
        boolean update=this.update().eq("email",email).set("password",password).update();
        if(update){
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA+email);
        }
        return null;
    }

    public boolean verifyLimit(String ip) {
        String key=Const.VERIFY_EMAIL_LIMIT+ip;
        return flowUtils.limitOnceCheck(key,60);
    }

    private boolean existsAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }
    private boolean existsAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }
}
