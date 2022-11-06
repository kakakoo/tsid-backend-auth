package com.tsid.auth.repo;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tsid.auth.util.EncryptUtil;
import com.tsid.domain.entity.oauthAuthorizeLog.OauthAuthorizeLog;
import com.tsid.domain.entity.oauthTokenLog.OauthTokenLog;
import com.tsid.domain.entity.user.User;
import com.tsid.domain.entity.userAccessToken.UserAccessToken;
import com.tsid.domain.entity.userActionLog.UserActionLog;
import com.tsid.domain.entity.userActionLog.UserActionLogRepository;
import com.tsid.domain.entity.userJoinCompany.UserJoinCompany;
import com.tsid.domain.enums.EActionFlag;
import com.tsid.domain.enums.EStatusFlag;
import com.tsid.domain.enums.group.EGroupPositionFlag;
import com.tsid.domain.enums.group.EGroupStatusFlag;
import com.tsid.domain.enums.token.ETokenStatusFlag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.querydsl.core.types.ExpressionUtils.count;
import static com.tsid.domain.entity.group.QGroup.group;
import static com.tsid.domain.entity.groupCert.QGroupCert.groupCert;
import static com.tsid.domain.entity.oauthAuthorizeLog.QOauthAuthorizeLog.oauthAuthorizeLog;
import static com.tsid.domain.entity.oauthTokenLog.QOauthTokenLog.oauthTokenLog;
import static com.tsid.domain.entity.user.QUser.user;
import static com.tsid.domain.entity.userAccessToken.QUserAccessToken.userAccessToken;
import static com.tsid.domain.entity.userActionLog.QUserActionLog.userActionLog;
import static com.tsid.domain.entity.userHasCompany.QUserHasCompany.userHasCompany;
import static com.tsid.domain.entity.userHasGroup.QUserHasGroup.userHasGroup;
import static com.tsid.domain.entity.userJoinCompany.QUserJoinCompany.userJoinCompany;
import static com.tsid.domain.entity.userPrivacy.QUserPrivacy.userPrivacy;

@Component
@RequiredArgsConstructor
public class UserRepo {

    private final JPAQueryFactory jpaQueryFactory;
    private final UserActionLogRepository userActionLogRepository;

    public User getUserByStateAndCompany(String state, Long companyId) {
        return jpaQueryFactory
                .select(user)
                .from(groupCert)
                .join(groupCert.group, group)
                .join(userHasGroup).on(userHasGroup.group.id.eq(group.id),
                        userHasGroup.position.eq(EGroupPositionFlag.MAKER))
                .join(userHasGroup.user, user)
                .where(groupCert.stateCode.eq(state),
                        groupCert.company.id.eq(companyId),
                        user.status.eq(EStatusFlag.ACTIVE))
                .fetchOne();
    }

    public Long getUserHasCompanyCount(Long userId) {
        return jpaQueryFactory
                .select(count(userHasCompany))
                .from(userHasCompany)
                .where(userHasCompany.user.id.eq(userId),
                        userHasCompany.status.eq(EGroupStatusFlag.ACTIVE))
                .fetchOne();
    }

    public UserTokenDto getUserOauthByCompanyAndCode(Long companyId, String code) {
        return jpaQueryFactory
                .select(Projections.fields(
                        UserTokenDto.class,
                        user.id.as("userId"),
                        user.uuid.as("uuid")))
                .from(oauthAuthorizeLog)
                .join(user).on(oauthAuthorizeLog.userId.eq(user.id), user.status.eq(EStatusFlag.ACTIVE))
                .where(oauthAuthorizeLog.companyId.eq(companyId),
                        oauthAuthorizeLog.tokenCode.eq(code))
                .fetchOne();
    }

    public void insertUserActionLog(long userId, EActionFlag action, long target) {
        UserActionLog lastLog = jpaQueryFactory
                .selectFrom(userActionLog)
                .where(userActionLog.userId.eq(userId))
                .orderBy(userActionLog.id.desc())
                .fetchFirst();

        String hash = "";
        if (lastLog != null) {
            long time = lastLog.getCreateDate().toInstant().toEpochMilli() / 1000;
            hash = "hash generate";
        }
        ZonedDateTime now = ZonedDateTime.now();
        long time = now.toInstant().toEpochMilli() / 1000;
        hash = hash + "hash generate";

        ZonedDateTime longZone = ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.of("Asia/Seoul"));

        UserActionLog actionLog = UserActionLog.builder()
                .build();
        userActionLogRepository.save(actionLog);
    }

    public String getUserCi(String uuid) {
        return jpaQueryFactory
                .select(userPrivacy.ci)
                .from(user)
                .join(user.userPrivacy, userPrivacy)
                .where(user.uuid.eq(uuid))
                .fetchFirst();
    }

    public UserAccessToken getUserTokenInfo(String token) {
        return jpaQueryFactory
                .selectFrom(userAccessToken)
                .where(userAccessToken.token.eq(token),
                        userAccessToken.status.eq(ETokenStatusFlag.ACTIVE))
                .fetchOne();
    }
}
