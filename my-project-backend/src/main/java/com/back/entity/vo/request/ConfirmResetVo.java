package com.back.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
@Data
@AllArgsConstructor
public class ConfirmResetVo {
    @Email
    String email;
    @Length(min=6,max=6)
    String code;

}

