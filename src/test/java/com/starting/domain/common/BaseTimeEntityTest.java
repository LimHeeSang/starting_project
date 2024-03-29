package com.starting.domain.common;

import com.starting.domain.member.entity.SchoolInfo;
import com.starting.domain.member.entity.UserMember;
import com.starting.domain.member.repository.UserMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BaseTimeEntityTest {

    @Autowired
    UserMemberRepository userMemberRepository;

    @Test
    void create() {
        LocalDateTime now = LocalDateTime.of(2022, 5, 18, 4, 54, 0);
        UserMember member = createUserMember("aaa@naver.com", "userA", "010-1234-0000", "12340000");
        userMemberRepository.save(member);

        UserMember findMember = userMemberRepository.findByNickName("userA").get();

        assertThat(findMember.getCreatedDate()).isAfter(now);
        assertThat(findMember.getModifiedDate()).isAfter(now);
    }

    private UserMember createUserMember(String email, String nickName, String phoneNumber, String schoolNumber) {
        SchoolInfo schoolInfo = SchoolInfo.builder()
                .school("순천향대")
                .department("정보보호학과")
                .uniqSchoolNumber(schoolNumber)
                .build();

        return UserMember.builder()
                .name("홍길동")
                .email(email)
                .nickName(nickName)
                .password("1234")
                .birthOfDate(LocalDate.of(1998, 9, 4))
                .phoneNumber(phoneNumber)
                .mbti("estj")
                .temperature(37.5D)
                .schoolInfo(schoolInfo)
                .build();
    }
}