package com.tsid.auth.exception;

public class ErrMsg {

    public final static String TRY_AGAIN = "다시 시도해 주세요.";

    public final static String INVALID_AUTH = "파라미터 오류_a";
    public final static String INVALID_ROUTE = "파라미터 오류_r";
    public final static String INVALID_PARAMETER = "파라미터 오류_p";

    public final static String USER_NOT_EXIST = "존재하지않는 사용자입니다.";

    public final static String FORBIDDEN_TSID = "사용처에 등록된 직원이 아닙니다.";

    public final static String INVALID_PARAMETER_RESPONSE_TYPE = "invalid response_type value";
    public final static String INVALID_COMPANY = "invalid company, check your request";
    public final static String INVALID_STATE_CODE = "invalid state code, using other code";
    public final static String CERT_NOT_EXIST = "oauth not exist";

    public final static String INVALID_GRANT_TYPE = "invalid grant_type";
    public final static String INVALID_COMPANY_INFO = "invalid company keys, check your company codes";
    public final static String INVALID_USER = "invalid user";
    public final static String INVALID_AGREEMENT = "user don't agree terms";
    public final static String INVALID_REFRESH_TOKEN = "refresh token is expired";
    public final static String STATE_CODE_CONFLICT = "state code is exist";

}
