package com.starting.domain.member.service;

import com.starting.domain.member.dto.*;
import com.starting.domain.member.entity.Member;
import com.starting.domain.member.entity.RefreshToken;
import com.starting.domain.member.entity.UserMember;
import com.starting.domain.member.exception.*;
import com.starting.domain.member.repository.MemberRepository;
import com.starting.domain.member.repository.UserMemberRepository;
import com.starting.global.oauth.util.EmailUtil;
import com.starting.global.security.JwtProvider;
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
     * 이메일 로그인
     */
    public MemberLoginResponseDto login(MemberLoginRequestDto loginRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        Member findMember = memberRepository.findByEmailWithRefreshToken(authentication.getName()).orElseThrow(NotExistMemberException::new);

        MemberLoginResponseDto loginResponseDto = jwtProvider.createToken(authentication);
        findMember.updateRefreshToken(loginResponseDto.getRefreshToken());

        return loginResponseDto;
    }

    /**
     * 토큰 재발급
     */
    public MemberLoginResponseDto reissue(MemberReissueRequestDto reissueRequestDto) {
        validateAccessToken(reissueRequestDto);

        Authentication authentication = jwtProvider.getAuthentication(reissueRequestDto.getAccessToken());
        Member findMember = getMember(authentication);

        RefreshToken refreshToken = findMember.getRefreshToken().orElseThrow(NotExistTokenException::new);
        validateRefreshToken(reissueRequestDto, refreshToken);

        MemberLoginResponseDto newTokenResponseDto = jwtProvider.createToken(authentication);
        refreshToken.updateRefreshToken(newTokenResponseDto.getRefreshToken());

        return newTokenResponseDto;
    }

    private void validateAccessToken(MemberReissueRequestDto reissueRequestDto) {
        if (!jwtProvider.validateToken(reissueRequestDto.getAccessToken())) {
            throw new NotValidTokenException();
        }
    }

    private void validateRefreshToken(MemberReissueRequestDto reissueRequestDto, RefreshToken refreshToken) {
        if (!refreshToken.isEqualTokenValue(reissueRequestDto.getRefreshToken())) {
            throw new NotValidTokenException();
        }
    }

    private Member getMember(Authentication authentication) {
        if (EmailUtil.isEmailRegex(authentication.getName())) {
            return memberRepository.findByEmailWithRefreshToken(authentication.getName()).orElseThrow(NotExistMemberException::new);
        }
        return memberRepository.findByUserIdWithRefreshToken(authentication.getName()).orElseThrow(NotExistMemberException::new);
    }

    /**
     * 유저 정보 입력 여부 확인
     */
    public boolean isInputUserData(Long memberId) {
        UserMember findMember = userMemberRepository.findById(memberId).orElseThrow(NotExistMemberException::new);
        return findMember.isInputUserData();
    }

    /**
     * 유저 정보 입력
     */
    public void inputUserData(Long memberId, InputUserDataRequestDto inputUserDataRequestDto) {
        UserMember findMember = userMemberRepository.findById(memberId).orElseThrow(NotExistMemberException::new);
        if (findMember.isInputUserData()) {
            throw new AlreadyInputUserdata();
        }
        if (userMemberRepository.existsByNickName(inputUserDataRequestDto.getNickname())) {
            throw new ExistNicknameException();
        }

        findMember.inputUserData(inputUserDataRequestDto);
    }

    /**
     * 닉네임 중복 여부 확인
     */
    public boolean isDuplicateNickname(Long memberId, String nickname) {
        userMemberRepository.findById(memberId).orElseThrow(NotExistMemberException::new);
        return userMemberRepository.existsByNickName(nickname);
    }

    /**
     * 유저 정보 조회
     */
    public UserMemberInfoResponseDto getUserMemberInfo(Long memberId) {
        UserMember findMember = userMemberRepository.findById(memberId).orElseThrow(NotExistMemberException::new);
        return UserMemberInfoResponseDto.of(findMember);
    }
}