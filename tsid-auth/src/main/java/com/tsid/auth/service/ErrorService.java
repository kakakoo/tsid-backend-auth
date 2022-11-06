package com.tsid.auth.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tsid.auth.common.Constants;
import com.tsid.auth.util.EncryptUtil;
import com.tsid.auth.util.SecurityUtil;
import com.tsid.domain.entity.errorLog.ErrorLog;
import com.tsid.domain.entity.errorLog.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tsid.domain.entity.user.QUser.user;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorService {

    private final ErrorLogRepository errorLogRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Value("${auth.di.key}")
    private String AUTH_DI_KEY;

    @Transactional
    public void insertErrorLog(String platform, String version, String url, String message){
        Long userId;
        String userUuid = SecurityUtil.getCurrentUserUuid();
        if (userUuid == null || userUuid.equals("anonymousUser")) {
            userId = null;
        } else {
            String uuid = EncryptUtil.aes256Decrypt(AUTH_DI_KEY, userUuid);
            userId = jpaQueryFactory
                    .select(user.id)
                    .from(user)
                    .where(user.uuid.eq(uuid))
                    .fetchFirst();
        }

        ErrorLog errorLog = ErrorLog.builder()
                .userId(userId)
                .os(platform)
                .version(version)
                .url(url)
                .message(message)
                .server(Constants.SERVER_INFO)
                .build();
        errorLogRepository.save(errorLog);
    }
}
