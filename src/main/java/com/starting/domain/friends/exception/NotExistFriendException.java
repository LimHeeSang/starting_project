package com.starting.domain.friends.exception;

import com.starting.global.exception.business.BusinessException;

public class NotExistFriendException extends BusinessException {

    public NotExistFriendException() {
        super(FriendExEnum.NOT_EXIST_FRIEND);
    }
}
