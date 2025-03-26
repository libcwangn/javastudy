package com.back.service;

import com.back.entity.dto.Account;
import com.back.entity.vo.request.ConfirmResetVo;
import com.back.entity.vo.request.EmailRegisterVo;
import com.back.entity.vo.request.EmailResetVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByUsernameOrEmail(String text);
    String registerEmailVerifyCode(String type, String email,String ip);
    String registerEmailAccount(EmailRegisterVo vo);
    String resetConfirm(ConfirmResetVo vo);
    String resetEmailAccountPassword(EmailResetVo vo);
}
