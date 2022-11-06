package com.tsid.auth.util;

import com.tsid.auth.exception.AuthServerException;
import com.tsid.auth.exception.ErrCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class TokenUtil {

    private static final String AUTH_TYPE = "Bearer";

    private final Key key;

    private static final String ISSURE_VALUE = "https://auth.tsidtech.com";
    public static final long ACCESS_TOKEN_VALID_TIME = 7776000000L;
    public static final long REFRESH_TOKEN_VALID_TIME = 8640000000L;

    public TokenUtil(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public org.springframework.security.core.Authentication getSecurityAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        UserDetails principal = new User(claims.getSubject(), claims.getSubject(), authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public Claims getClaims(String accessToken){
        String jwt = resolveToken(accessToken);
        return parseClaims(jwt);
    }

    public TokenDto.Token generateToken(String client_id, String uuid){
        long now = (new Date()).getTime();

        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_VALID_TIME);
        Date refreshTokenExpireIn = new Date(now + REFRESH_TOKEN_VALID_TIME);

        String accessToken = Jwts.builder()
                .compact();

        String refreshToken = UUID.randomUUID().toString();

        return TokenDto.Token.builder()
                .build();
    }

    // Request Header 에서 토큰 정보를 꺼내오기
    public String resolveToken(String accessToken) {
        if (StringUtils.hasText(accessToken) && accessToken.startsWith(AUTH_TYPE)) {
            return accessToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
            throw new AuthServerException(ErrCode.PARSE_CLAIM, "잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            throw new AuthServerException(ErrCode.PARSE_CLAIM, "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
            throw new AuthServerException(ErrCode.PARSE_CLAIM, "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
            throw new AuthServerException(ErrCode.PARSE_CLAIM, "JWT 토큰이 잘못되었습니다.");
        }
    }
}