package com.starting.domain.member.exception;

import com.starting.global.exception.business.BusinessException;

public class ExistNicknameException extends BusinessException {

    public ExistNicknameException() {
        super(MemberExEnum.ALREADY_EXIST_NICKNAME);
    }
}
