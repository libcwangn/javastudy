package com.back.controller;

import com.back.entity.RestBean;
import com.back.entity.vo.request.ConfirmResetVo;
import com.back.entity.vo.request.EmailRegisterVo;
import com.back.entity.vo.request.EmailResetVo;
import com.back.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService accountService;

    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request) {
        return this.messageHandle(()->
                accountService.registerEmailVerifyCode(type,email,request.getRemoteAddr()));
    }
    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Validated EmailRegisterVo vo){
       return this.messageHandle(()->
               accountService.registerEmailAccount(vo));
    }
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Validated ConfirmResetVo vo){
        return this.messageHandle(vo,accountService::resetConfirm);
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetConfirm(@RequestBody @Validated EmailResetVo vo){
        return this.messageHandle(vo,accountService::resetEmailAccountPassword);
    }

    private <T> RestBean<Void> messageHandle(T vo, Function<T,String> function){
        return messageHandle(()->function.apply(vo));
    }

    private  RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message ==null?RestBean.success():RestBean.failure(400,message);
    }
}
