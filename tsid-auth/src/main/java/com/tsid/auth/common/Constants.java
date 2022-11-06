package com.tsid.auth.common;

public class Constants {

    public static final String SERVER_INFO = "AUTH";
    public static final String SERVER_PACKAGE = "com.tsid.auth";

    public static final String CERT_GRANT_TYPE = "CERT";
    public static final String AUTHORIZE_GRANT_TYPE = "LOGIN";
    public static final String AUTHORIZE_RESPONSE_TYPE = "code";
    public static final int AUTHORIZE_EXPIRE_TIME = 3;

    public static final String TOKEN_GRANT_TYPE = "authorization_code";
    public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";

    public static final Long ADMIN_TSID_COMPANY_ID = 10L;
    public static final Long CHEESE_COMPANY_ID = 37L;

    public static final String LOCATION_DEFAULT = "기타 지역";
    public static final String LOCATION_KOREA_SPACE = "대한민국 ";

    public static final String ALARM_TITLE_CERT = "인증";
    public static final String ALARM_TITLE_NOTICE = "공지";
    public static final String ALARM_TITLE_GROUP_INVITE = "초대";
    public static final String ALARM_TITLE_GROUP_WITHDRAW = "해제";

    public static final String TSID_DEEPLINK_URL = "tsid://tsidtech.auth/weboauth?token=";
}
