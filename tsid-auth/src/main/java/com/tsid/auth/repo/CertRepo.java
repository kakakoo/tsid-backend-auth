package com.tsid.auth.repo;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tsid.domain.entity.certRole.CertRole;
import com.tsid.domain.entity.groupCert.GroupCert;
import com.tsid.domain.entity.groupCertHistory.GroupCertHistory;
import com.tsid.domain.enums.cert.ECertHistoryFlag;
import com.tsid.domain.enums.group.EGroupPositionFlag;
import com.tsid.domain.enums.group.EGroupStatusFlag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.count;
import static com.tsid.domain.entity.certRole.QCertRole.certRole;
import static com.tsid.domain.entity.group.QGroup.group;
import static com.tsid.domain.entity.groupCert.QGroupCert.groupCert;
import static com.tsid.domain.entity.groupCertHistory.QGroupCertHistory.groupCertHistory;
import static com.tsid.domain.entity.oauthAuthorizeLog.QOauthAuthorizeLog.oauthAuthorizeLog;
import static com.tsid.domain.entity.userHasGroup.QUserHasGroup.userHasGroup;

@Component
@RequiredArgsConstructor
public class CertRepo {

    private final JPAQueryFactory jpaQueryFactory;

    public GroupCert getGroupCertByCompanyAndState(Long companyId, String state) {
        return jpaQueryFactory
                .selectFrom(groupCert)
                .where(groupCert.company.id.eq(companyId),
                        groupCert.stateCode.eq(state))
                .fetchOne();
    }

    public CertMemberDto getGroupMemberInfo(Long groupId) {
        return jpaQueryFactory
                .select(Projections.bean(
                        CertMemberDto.class,
                        userHasGroup.group.id,
                        userHasGroup.position.when(EGroupPositionFlag.CONSENTER).then(1).otherwise(0).sum().as("consenter"),
                        userHasGroup.position.when(EGroupPositionFlag.REFERRER).then(1).otherwise(0).sum().as("referrer")))
                .from(userHasGroup)
                .where(userHasGroup.group.id.eq(groupId),
                        userHasGroup.status.in(EGroupStatusFlag.ACTIVE, EGroupStatusFlag.RELEASE))
                .groupBy(userHasGroup.group.id)
                .fetchOne();
    }

    public Long getCertCountByGroup(Long groupId) {
        return jpaQueryFactory
                .select(count(groupCert))
                .from(groupCert)
                .where(groupCert.group.id.eq(groupId))
                .fetchOne();
    }
}
