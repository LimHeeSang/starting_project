package com.starting.domain.member.exception;

import com.starting.global.exception.business.BusinessException;

public class NotValidTokenException extends BusinessException {

    public NotValidTokenException() {
        super(MemberExEnum.NOT_VALID_TOKEN);
    }
}