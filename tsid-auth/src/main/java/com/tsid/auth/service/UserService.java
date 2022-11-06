package com.tsid.auth.service;

import com.tsid.auth.common.Constants;
import com.tsid.auth.exception.AuthServerException;
import com.tsid.auth.exception.ErrCode;
import com.tsid.auth.exception.ErrMsg;
import com.tsid.auth.repo.CompanyRepo;
import com.tsid.auth.repo.UserRepo;
import com.tsid.auth.util.EncryptUtil;
import com.tsid.auth.util.SecurityUtil;
import com.tsid.auth.util.TokenUtil;
import com.tsid.domain.entity.company.Company;
import com.tsid.domain.entity.qna.Qna;
import com.tsid.domain.entity.qna.QnaRepository;
import com.tsid.domain.entity.user.User;
import com.tsid.domain.entity.user.UserRepository;
import com.tsid.domain.entity.userAccessToken.UserAccessToken;
import com.tsid.domain.enums.EStatusFlag;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final TokenUtil tokenUtil;
    private final SnsUtil snsUtil;
    private final UserRepo userRepo;
    private final CompanyRepo companyRepo;
    private final UserRepository userRepository;
    private final QnaRepository qnaRepository;

    @Value("${auth.di.key}")
    private String AUTH_DI_KEY;

    @Transactional(readOnly = true)
    public UserResponse getUser() {

        String uuid = "uuid generate";
        User userInfo = userRepository.getUserByUuid(uuid);

        if (userInfo == null || !userInfo.getStatus().equals(EStatusFlag.ACTIVE)) {
            throw new AuthServerException(ErrCode.FORBIDDEN_USER, ErrMsg.INVALID_USER);
        }

        return UserResponse.builder()
                .build();
    }

    @Transactional(readOnly = true)
    public CheckResponse checkCiValue(String Authorization, CheckRequest request) {

        Claims claims = tokenUtil.getClaims(Authorization);
        String clientId = claims.getAudience();

        Company company = companyRepo.getCompanyByClientId(clientId);

        boolean isEqual = false;
        if (Constants.CHEESE_COMPANY_ID.equals(company.getId())) {
            String uuid = EncryptUtil.aes256Decrypt(AUTH_DI_KEY, claims.getSubject());

            String ci = userRepo.getUserCi(uuid);
            if (ci.equals(request.getCi())) {
                isEqual = true;
            }
        }

        return CheckResponse.builder()
                .build();
    }

    @Transactional
    public void insertQna(String accessToken, QnaRequest request) {

        String title = "1:1 문의가 접수되었습니다.";
        String mesage = "";
        mesage += "제목 : " + request.getTitle();
        mesage += "\n\n문의 유형 : " + request.getQnaType().getDescription();
        mesage += "\n\n내용 : " + request.getDescription();
        mesage += "\n\n회신받을 이메일 : " + request.getEmail();

        SnsRequest.QnaEmail email = SnsRequest.QnaEmail.builder()
                        .title(title)
                        .text(mesage)
                        .build();
        snsUtil.sendQnaEmail(email);

        User userInfo = null;
        if (accessToken != null && accessToken.startsWith("TSID")) {
            UserAccessToken tokenInfo = userRepo.getUserTokenInfo(accessToken.substring(5));
            if (tokenInfo != null) {
                userInfo = userRepository.getUserById(tokenInfo.getUserId());
            }
        }

        Qna qna = Qna.builder()
                .email(request.getEmail())
                .user(userInfo)
                .title(request.getTitle())
                .question(request.getDescription())
                .type(request.getQnaType())
                .build();
        qnaRepository.save(qna);
    }

}
