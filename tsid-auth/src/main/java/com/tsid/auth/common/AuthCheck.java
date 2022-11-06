package com.tsid.auth.common;

import com.tsid.auth.exception.AuthServerException;
import com.tsid.auth.exception.ErrCode;
import com.tsid.auth.exception.ErrMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthCheck {

    private static String TSID_AUTH;

    @Value("${tsid.fido.auth}")
    public void setFidoAuth(String auth) {
        this.TSID_AUTH = auth;
    }

    public static void checkFidoAuth(String cheeseAuth) {
        if (!cheeseAuth.equals(TSID_AUTH)) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_AUTH);
        }
    }

}
