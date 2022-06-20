package com.starteeing.domain.member.exception;

import com.starteeing.golbal.exception.common.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberExEnum implements ExceptionEnum {
    ALREADY_EXIST_MEMBER(HttpStatus.BAD_REQUEST, "M0001", "이미 존재하는 회원입니다."),
    NOT_EXIST_MEMBER(HttpStatus.BAD_REQUEST, "M0002", "존재하지 않는 회원입니다."),
    NOT_VALID_TOKEN(HttpStatus.UNAUTHORIZED, "M0003", "유효하지 않은 토큰입니다."),
    NOT_EXIST_TOKEN(HttpStatus.UNAUTHORIZED, "M0004", "토큰이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}