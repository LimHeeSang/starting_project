package com.starting.domain.member.service;

import com.starting.domain.member.dto.MemberLoginRequestDto;
import com.starting.domain.member.dto.MemberLoginResponseDto;
import com.starting.domain.member.dto.MemberReissueRequestDto;
import com.starting.domain.member.dto.UserMemberSignupRequestDto;
import com.starting.domain.member.entity.Member;
import com.starting.domain.member.entity.RefreshToken;
import com.starting.domain.member.entity.UserMember;
import com.starting.domain.member.exception.ExistMemberException;
import com.starting.domain.member.exception.NotExistMemberException;
import com.starting.domain.member.exception.NotExistTokenException;
import com.starting.domain.member.exception.NotValidTokenException;
import com.starting.domain.member.repository.MemberRepository;
import com.starting.domain.member.repository.UserMemberRepository;
import com.starting.golbal.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class UserMemberService {

    private final UserMemberRepository userMemberRepository;
    private final MemberRepository memberRepository;

    private final JwtProvider jwtProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 회원가입
     */
    public Long memberJoin(UserMemberSignupRequestDto memberRequestDto) {
        validateDuplicateMember(memberRequestDto);

        UserMember userMember = memberRequestDto.toEntity(bCryptPasswordEncoder);

        UserMember savedMember = userMemberRepository.save(userMember);
        return savedMember.getId();
    }

    private void validateDuplicateMember(UserMemberSignupRequestDto memberRequestDto) {
        if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
            throw new ExistMemberException();
        }
    }

    /**
     * 로그인
     */
    public MemberLoginResponseDto login(MemberLoginRequestDto loginRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        Member findMember = memberRepository.findByEmail(authentication.getName()).orElseThrow(NotExistMemberException::new);

        MemberLoginResponseDto loginResponseDto = jwtProvider.createToken(authentication);
        findMember.saveRefreshToken(loginResponseDto.getRefreshToken());

        return loginResponseDto;
    }

    /**
     * 토큰 재발급
     */
    public MemberLoginResponseDto reissue(MemberReissueRequestDto reissueRequestDto) {
        if (!jwtProvider.validateToken(reissueRequestDto.getAccessToken())) {
            throw new NotValidTokenException();
        }

        Authentication authentication = jwtProvider.getAuthentication(reissueRequestDto.getAccessToken());
        Member findMember = memberRepository.findByEmailWithRefreshToken(authentication.getName()).orElseThrow(NotExistMemberException::new);
        RefreshToken refreshToken = findMember.getRefreshToken().orElseThrow(NotExistTokenException::new);

        if (!refreshToken.isEqualTokenValue(reissueRequestDto.getRefreshToken())) {
            throw new NotValidTokenException();
        }

        MemberLoginResponseDto newTokenResponseDto = jwtProvider.createToken(authentication);
        refreshToken.updateRefreshToken(newTokenResponseDto.getRefreshToken());

        return newTokenResponseDto;
    }
}