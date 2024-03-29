package com.starting.global.security;

import com.starting.domain.member.dto.MemberLoginResponseDto;
import com.starting.domain.member.dto.OauthLoginResponseDto;
import com.starting.global.oauth.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private static final String ISSUER = "starting";
    private static final String CLAIM_NAME_ROLES = "roles";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String GRANT_TYPE = "Bearer";
    public static final String DEFAULT_CREDENTIALS = "";

    private final SecretKey key;
    private final AppProperties appProperties;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtProvider(AppProperties appProperties, UserDetailsServiceImpl userDetailsService) {
        this.appProperties = appProperties;
        this.key = createSecretKey(appProperties.getJwtTokenSecret());
        this.userDetailsService = userDetailsService;
    }

    private SecretKey createSecretKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Jwt token 생성
     */
    public MemberLoginResponseDto createToken(Authentication authentication) {
        Date now = new Date();
        String accessToken = createAccessToken(authentication, getAuthorities(authentication), now);
        String refreshToken = createRefreshToken(now);

        return MemberLoginResponseDto.builder()
                .grantType(GRANT_TYPE)
                .accessTokenExpireDate(now.getTime() + appProperties.getJwtAccessTokenExpire())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public OauthLoginResponseDto createToken(Authentication authentication, Long memberId) {
        Date now = new Date();
        String accessToken = createAccessToken(authentication, getAuthorities(authentication), now);
        String refreshToken = createRefreshToken(now);

        return OauthLoginResponseDto.builder()
                .memberId(memberId)
                .grantType(GRANT_TYPE)
                .accessTokenExpireDate(now.getTime() + appProperties.getJwtAccessTokenExpire())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String createAccessToken(Authentication authentication, String authorities, Date now) {
        return Jwts.builder()
                .setIssuer(ISSUER)
                .setSubject(authentication.getName())
                .claim(CLAIM_NAME_ROLES, authorities)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + appProperties.getJwtAccessTokenExpire()))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String createRefreshToken(Date now) {
        return Jwts.builder()
                .setIssuer(ISSUER)
                .setExpiration(new Date(now.getTime() + appProperties.getJwtRefreshTokenExpire()))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
    }

    /**
     * Jwt 로 인증정보 조회
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(parseUserName(token));
        return new UsernamePasswordAuthenticationToken(userDetails, DEFAULT_CREDENTIALS, userDetails.getAuthorities());
    }

    private String parseUserName(String token) {
        return parseBody(token).getSubject();
    }

    /**
     * Header 에서 Token Parsing
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Jwt 의 유효성 및 만료일짜 확인
     */
    public boolean validateToken(String token) {
        try {
            Claims body = parseBody(token);
            return !body.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseBody(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}