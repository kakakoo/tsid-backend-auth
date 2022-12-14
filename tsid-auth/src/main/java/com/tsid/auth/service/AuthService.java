package com.tsid.auth.service;

import com.tsid.auth.ResValue;
import com.tsid.auth.common.Constants;
import com.tsid.auth.exception.AuthServerException;
import com.tsid.auth.exception.ErrCode;
import com.tsid.auth.exception.ErrMsg;
import com.tsid.auth.repo.AppRepo;
import com.tsid.auth.repo.CertRepo;
import com.tsid.auth.repo.CompanyRepo;
import com.tsid.auth.repo.UserRepo;
import com.tsid.auth.util.EncryptUtil;
import com.tsid.auth.util.TokenUtil;
import com.tsid.domain.entity.company.Company;
import com.tsid.domain.entity.groupCert.GroupCert;
import com.tsid.domain.entity.groupCertHistory.GroupCertHistory;
import com.tsid.domain.entity.oauthAuthorizeLog.OauthAuthorizeLog;
import com.tsid.domain.entity.oauthTokenLog.OauthTokenLog;
import com.tsid.domain.entity.oauthTokenLog.OauthTokenLogRepository;
import com.tsid.domain.entity.serverInfo.ServerInfo;
import com.tsid.domain.entity.user.User;
import com.tsid.domain.entity.user.UserRepository;
import com.tsid.domain.entity.userJoinCompany.UserJoinCompany;
import com.tsid.domain.entity.userJoinCompany.UserJoinCompanyRepository;
import com.tsid.domain.enums.EStatusFlag;
import com.tsid.domain.enums.term.ETermGroupFlag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static com.tsid.auth.util.TokenUtil.ACCESS_TOKEN_VALID_TIME;
import static com.tsid.auth.util.TokenUtil.REFRESH_TOKEN_VALID_TIME;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final TokenUtil tokenUtil;
    private final CertService certService;
    private final SnsUtil snsUtil;

    private final AppRepo appRepo;
    private final UserRepo userRepo;
    private final CertRepo certRepo;
    private final CompanyRepo companyRepo;

    private final UserRepository userRepository;
    private final UserJoinCompanyRepository userJoinCompanyRepository;
    private final OauthTokenLogRepository oauthTokenLogRepository;

    @Value("${auth.di.key}")
    private String AUTH_DI_KEY;
    @Value("${auth.temp.key}")
    private String AUTH_TEMP_KEY;

    private boolean companyStatus(Company companyInfo){
        return !(companyInfo.getStatus().equals(EStatusFlag.ACTIVE) || companyInfo.getStatus().equals(EStatusFlag.HIDE));
    }

    @Transactional(readOnly = true)
    public AuthorizeDto authorizeKey(...)  {
        /**
         * ?????? ???????????? ????????? ??????
         * ?????? ????????? ??? ????????? ???????????? ?????? ???????????????
         */
        if (!Constants.AUTHORIZE_RESPONSE_TYPE.equals(responseType)) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER_RESPONSE_TYPE);
        }

        Company companyInfo = companyRepo.getCompanyByClientId(clientId, redirectUri, Constants.AUTHORIZE_GRANT_TYPE);

        if (companyInfo == null || companyStatus(companyInfo)) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_COMPANY);
        }

        Long stateCnt = certRepo.getStateCheck(state);
        if (stateCnt != null && stateCnt > 0) {
            throw new AuthServerException(ErrCode.STATE_CODE_CONFLICT, ErrMsg.STATE_CODE_CONFLICT);
        }

        String privacy = "";
        String useTerm = "";
        List<TermDto> terms = appRepo.getTerm();
        for (TermDto term : terms) {
            if (term.getType().equals(ETermGroupFlag.PRIVACY)) {
                privacy = term.getTermUrl();
            } else {
                useTerm = term.getTermUrl();
            }
        }

        String token = "token generate";

        AuthorizeDto result = AuthorizeDto.builder()
                .build();

        return result;
    }

    @Transactional
    public ResValue<AuthResponse.AuthResult> authorize(...) {
        /**
         * ????????? ????????? ??????
         * ?????? ???????????? tsid ????????? ???????????? ????????? ???????????? tsid ?????? ?????? ??????
         * ?????? ???????????? tsid ??????????????? ?????? ???????????? ?????? ?????? ??????
         *  ?????? ???????????? ?????? ????????? ????????? ?????? ????????? ?????? ??????
         *  ?????? ???????????? ?????? ????????? ?????????, ????????? ?????? ????????????????????? ?????? ??????
         */
        if (request.getUserTel() == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }

        AuthTokenDto token = makeToken(request.getToken());
        Long stateCnt = certRepo.getStateCheck(token.getState());
        if (stateCnt != null && stateCnt > 0) {
            throw new AuthServerException(ErrCode.STATE_CODE_CONFLICT, ErrMsg.STATE_CODE_CONFLICT);
        }

        Company companyInfo = companyRepo.getCompanyByClientId(token.getClientId(), token.getRedirectUri(), Constants.AUTHORIZE_GRANT_TYPE);

        if (companyInfo == null || companyStatus(companyInfo)) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }

        String tel = "????????? ?????????";

        if (tel == null || tel.length() != 11) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }

        User userInfo = userRepository.getExistUserByTel(tel);
        if (userInfo == null) {
            /**
             * ??? ?????? ?????? ?????????
             */
            ServerInfo installInfo = appRepo.getServerInfoByCode("INSTALL");
            SnsRequest.SmsCustom smsRequest = SnsRequest.SmsCustom.builder()
                    .build();
            snsUtil.sendSmsCustom(smsRequest);

            AuthResponse.AuthResult response = AuthResponse.AuthResult.builder()
                    .build();
            return new ResValue<>(response);
        }

        if (companyInfo.getId().equals(Constants.ADMIN_TSID_COMPANY_ID)) {
            /**
             * TSID ????????? ????????? ????????? ??????
             */
            Long hasCompany = userRepo.getUserHasCompanyCount(userInfo.getId());
            if (hasCompany == null || hasCompany == 0) {
                throw new AuthServerException(ErrCode.FORBIDDEN_TSID, ErrMsg.FORBIDDEN_TSID);
            }
        }

        Long callbackId = companyRepo.getCompanyCallbackId(companyInfo.getId(), token.getRedirectUri());

        MakeCertInfo makeCertInfo = certService.doCertAction(...);
        if (makeCertInfo == null || !makeCertInfo.getIsMake()) {
            throw new AuthServerException(ErrCode.INTERNAL_ERROR, "auth server error");
        }

        AuthResponse.AuthResult response = AuthResponse.AuthResult.builder()
                .build();
        return new ResValue<>(response);
    }

    @Transactional(readOnly = true)
    public ResValue<AuthResponse.AuthCheck> authorizeCheck(...) {
        /**
         * ?????? ?????????????????? ??????
         * ????????? ?????????????????? ?????? ???????????? ???????????? ??????????????? ?????? ????????? ???????????? ??????
         */
        if (request.getPlatform().equals(EPlayformType.WEB) && request.getUserTel() == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }

        AuthTokenDto token = makeToken(request.getToken());
        Company companyInfo = companyRepo.getCompanyByClientId(token.getClientId(), token.getRedirectUri(), null);

        User userInfo;
        if (request.getPlatform().equals(EPlayformType.WEB)) {
            String tel = "???????????? ?????????";
            userInfo = userRepository.getExistUserByTel(tel);
        } else {
            userInfo = userRepo.getUserByStateAndCompany(token.getState(), companyInfo.getId());
        }

        if (userInfo == null || companyInfo == null || companyStatus(companyInfo)) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }

        GroupCert cert = certRepo.getGroupCertByCompanyAndState(companyInfo.getId(), token.getState());
        if (cert == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.CERT_NOT_EXIST);
        }

        boolean isCert = cert.getIsCert();
        boolean isFirst = false;
        if (isCert) {
            UserJoinCompany joinCompany = userRepo.getUserJoinCompanyByUserAndCompany(userInfo.getId(), companyInfo.getId());
            if (joinCompany == null) {
                isFirst = true;
            }
        }

        List<GroupCertHistory> certHistoryList = certRepo.getGroupCertHistoryList(cert.getId());

        int certCount = 0;
        if (certHistoryList != null) {
            certCount = certHistoryList.size();
        }

        AuthResponse.AuthCheck response = AuthResponse.AuthCheck.builder()
                .build();

        return new ResValue<>(response);
    }

    @Transactional
    public ResValue<AuthResponse.AuthResult> agreeCheck(...) {

        /**
         * tsid?????? ????????? ???????????? ?????? ?????? ?????? ??????
         */
        if (request.getPlatform().equals(EPlayformType.WEB) && request.getUserTel() == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }
        AuthTokenDto token = makeToken(request.getToken());

        Company companyInfo = companyRepo.getCompanyByClientId(token.getClientId());

        User userInfo;
        if (request.getPlatform().equals(EPlayformType.WEB)) {
            String tel = "???????????? ?????????";
            userInfo = userRepository.getExistUserByTel(tel);
        } else {
            userInfo = userRepo.getUserByStateAndCompany(token.getState(), companyInfo.getId());
        }

        if (userInfo == null || companyInfo == null || companyStatus(companyInfo)) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }

        UserJoinCompany joinCompany = userRepo.getUserJoinCompanyByUserAndCompany(userInfo.getId(), companyInfo.getId());

        if (joinCompany == null) {
            UserJoinCompany userCompany = UserJoinCompany.builder()
                    .build();

            userJoinCompanyRepository.save(userCompany);
        }

        AuthResponse.AuthResult response = AuthResponse.AuthResult.builder()
                .build();
        return new ResValue<>(response);
    }

    @Transactional
    public ResValue<AuthResponse.SuccessUrl> redirectCallback(...) {
        /**
         * ?????? ????????? ?????? ?????? ??? callback ??? ?????? ??????
         */
        AuthTokenDto token = makeToken(requestToken);

        Company companyInfo = companyRepo.getCompanyByClientId(token.getClientId());

        User userInfo;
        if (platform.equals(EPlayformType.WEB)) {
            userTel = "???????????? ?????????";
            userInfo = userRepository.getExistUserByTel(userTel);
        } else {
            userInfo = userRepo.getUserByStateAndCompany(token.getState(), companyInfo.getId());
        }

        if (userInfo == null || companyInfo == null || companyStatus(companyInfo)) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }

        GroupCert cert = certRepo.getGroupCertByCompanyAndState(companyInfo.getId(), token.getState());
        if (!cert.getIsCert()) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_PARAMETER);
        }

        String code = certRepo.getTokenCodeByUserAndCompany(userInfo.getId(), companyInfo.getId());

        String targetUrl = "url ?????????";

        cert.updateCallback();

        AuthResponse.SuccessUrl url = AuthResponse.SuccessUrl.builder()
                .build();

        return new ResValue<>(url);
    }

    private AuthTokenDto makeToken(String requestToken) {

        /**
         * ???????????? ?????? ???????????? ????????? ??????
         */
        return AuthTokenDto.builder()
                .build();
    }

    @Transactional
    public TokenDto.Token getToken(TokenRequest request) {

        if (!request.getGrant_type().equals(Constants.TOKEN_GRANT_TYPE) &&
                !request.getGrant_type().equals(Constants.REFRESH_TOKEN_GRANT_TYPE)) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_GRANT_TYPE);
        }

        Company companyInfo;
        UserTokenDto userInfo;

        if (request.getGrant_type().equals(Constants.TOKEN_GRANT_TYPE)) {
            companyInfo = companyRepo.getCompanyByClientAndUrl(request.getClient_id(), request.getClient_secret(), request.getRedirect_uri());
            if (companyInfo == null) {
                throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_COMPANY_INFO);
            }

            userInfo = userRepo.getUserOauthByCompanyAndCode(companyInfo.getId(), request.getCode());
            if (userInfo == null) {
                throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_USER);
            }
        } else {
            companyInfo = companyRepo.getCompanyByClient(request.getClient_id(), request.getClient_secret());

            userInfo = userRepo.getUserOauthWithExpireByCompanyAndToken(companyInfo.getId(), request.getRefresh_token());
            if (userInfo == null) {
                throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_USER);
            }

            ZonedDateTime now = ZonedDateTime.now();
            if (userInfo.getExpiresIn().isBefore(now)) {
                throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_REFRESH_TOKEN);
            }
        }

        UserJoinCompany joinCompany = userRepo.getUserJoinCompanyByUserAndCompany(userInfo.getUserId(), companyInfo.getId());
        if (joinCompany == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_AGREEMENT);
        }

        String uuid = "uuid generate";
        TokenDto.Token token = tokenUtil.generateToken(request.getClient_id(), uuid);

        OauthTokenLog tokenLog = userRepo.getAuthTokenLogByUserAndCompany(userInfo.getUserId(), companyInfo.getId());

        ZonedDateTime expiresZone = ZonedDateTime.now().plusSeconds(ACCESS_TOKEN_VALID_TIME / 1000);
        ZonedDateTime refreshExpiresZone = ZonedDateTime.now().plusSeconds(REFRESH_TOKEN_VALID_TIME / 1000);

        if (tokenLog == null) {
            OauthTokenLog log = OauthTokenLog.builder()
                    .build();
            oauthTokenLogRepository.save(log);
        } else {
            tokenLog.updateToken(token.getAccess_token(), expiresZone,
                    token.getRefresh_token(), refreshExpiresZone);
        }

        if (request.getGrant_type().equals(Constants.TOKEN_GRANT_TYPE)) {
            OauthAuthorizeLog authorizeLog = userRepo.getAuthorizeLogByCompanyUserCode(companyInfo.getId(), userInfo.getUserId(), request.getCode());
            authorizeLog.updateTokenCode();
        }

        return token;
    }

    @Transactional(readOnly = true)
    public ResValue<AuthResponse.CheckStateCode> checkStateCode(AuthRequest.Authorize request) {
        if (request == null || request.getToken() == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_STATE_CODE);
        }
        AuthTokenDto token = makeToken(request.getToken());

        Long stateCnt = certRepo.getStateCheck(token.getState());

        boolean isValid = true;
        if (stateCnt != null && stateCnt > 0) {
            throw new AuthServerException(ErrCode.STATE_CODE_CONFLICT, ErrMsg.STATE_CODE_CONFLICT);
        }

        AuthResponse.CheckStateCode result = AuthResponse.CheckStateCode.builder()
                .build();

        return new ResValue<>(result);
    }


}
