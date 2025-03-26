package com.back.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
@Data
public class EmailResetVo {
    @Email
    @Length(min = 4)
    String email;
    @Length(min=6,max=6)
    String code;
    @Length(min=6,max=20)
    String password;
}
