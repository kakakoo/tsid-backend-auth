package com.tsid.auth.util;

import com.tsid.auth.exception.AuthServerException;
import com.tsid.auth.exception.ErrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {

    private SecurityUtil() { }

    public static String getCurrentUserUuid() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw  new AuthServerException(ErrCode.GET_PRINCIPAL, "잘못된 토큰입니다.");
        }
        return authentication.getName();
    }
}