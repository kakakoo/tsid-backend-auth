package com.tsid.auth.repo;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tsid.domain.entity.group.Group;
import com.tsid.domain.entity.groupCert.GroupCert;
import com.tsid.domain.entity.groupCert.GroupCertRepository;
import com.tsid.domain.entity.groupCertHistory.GroupCertHistory;
import com.tsid.domain.entity.groupCertHistory.GroupCertHistoryRepository;
import com.tsid.domain.entity.groupUpdateHistory.GroupUpdateHistory;
import com.tsid.domain.entity.permission.Permission;
import com.tsid.domain.entity.userHasGroup.UserHasGroup;
import com.tsid.domain.enums.cert.ECertFlag;
import com.tsid.domain.enums.group.EGroupPositionFlag;
import com.tsid.domain.enums.group.EGroupStatusFlag;
import com.tsid.domain.enums.group.EGroupUpdateFlag;
import com.tsid.domain.enums.group.EUpdateFlag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.count;
import static com.tsid.domain.entity.group.QGroup.group;
import static com.tsid.domain.entity.groupHasCompany.QGroupHasCompany.groupHasCompany;
import static com.tsid.domain.entity.groupUpdateHistory.QGroupUpdateHistory.groupUpdateHistory;
import static com.tsid.domain.entity.permission.QPermission.permission;
import static com.tsid.domain.entity.user.QUser.user;
import static com.tsid.domain.entity.userHasGroup.QUserHasGroup.userHasGroup;

@RequiredArgsConstructor
@Component
public class GroupRepo {

    private final JPAQueryFactory jpaQueryFactory;
    private final GroupCertRepository groupCertRepository;
    private final GroupCertHistoryRepository groupCertHistoryRepository;

    public Group getGroupByUserAndCompany(Long userId, Long companyId) {
        return jpaQueryFactory
                .select(group)
                .from(user)
                .join(user.groups, userHasGroup).on(userHasGroup.position.eq(EGroupPositionFlag.MAKER),
                        userHasGroup.status.in(EGroupStatusFlag.ACTIVE, EGroupStatusFlag.RELEASE))
                .join(userHasGroup.group, group).on(group.isActive.isTrue())
                .join(groupHasCompany).on(group.id.eq(groupHasCompany.group.id),
                        groupHasCompany.company.id.eq(companyId))
                .where(user.id.eq(userId))
                .fetchOne();
    }

    public Long getGroupUserCount(Long groupId) {
        return jpaQueryFactory
                .select(count(userHasGroup))
                .from(userHasGroup)
                .where(userHasGroup.group.id.eq(groupId),
                        userHasGroup.status.in(EGroupStatusFlag.RELEASE, EGroupStatusFlag.ACTIVE))
                .fetchOne();
    }

    public List<UserHasGroup> getUserHasGroupList(Long groupId) {
        return jpaQueryFactory
                .select(userHasGroup)
                .from(userHasGroup)
                .where(userHasGroup.group.id.eq(groupId),
                        userHasGroup.status.in(EGroupStatusFlag.ACTIVE, EGroupStatusFlag.RELEASE))
                .fetch();
    }

    public void checkDelegate(Long userId, Long companyId) {
        /**
         * 생성한 그룹의 사용자가 위임받을 사용처의 그룹인지 확인
         * 맞으면 진행중인 인증은 거절 처리
         */
        GroupUpdateHistory updateHistory = jpaQueryFactory
                .selectFrom(groupUpdateHistory)
                .join(group).on(groupUpdateHistory.groupId.eq(group.id))
                .join(groupHasCompany).on(group.id.eq(groupHasCompany.group.id),
                        groupHasCompany.company.id.eq(companyId))
                .where(groupUpdateHistory.targetId.eq(userId),
                        groupUpdateHistory.flag.eq(EUpdateFlag.DELEGATE),
                        groupUpdateHistory.status.eq(EGroupUpdateFlag.PROGRESS))
                .fetchOne();

        if (updateHistory != null) {
            /**
             * 위임받을 사용처가 있고, 진행중이면 해당 인증건에 대해서 거절 처리
             */
            GroupCert cert = groupCertRepository.getById(updateHistory.getGroupCertId());
            GroupCertHistory userCertHistory = groupCertHistoryRepository.getGroupCertHistoryByUserIdAndCertId(userId, updateHistory.getGroupCertId());

            userCertHistory.updateCancel();
            cert.updateCertAuth(ECertFlag.REJECT);
            updateHistory.updateStatus(EGroupUpdateFlag.CANCEL);
        }
    }

    public Permission getCertPermission() {
        return jpaQueryFactory
                .selectFrom(permission)
                .where(permission.id.eq(1L))
                .fetchOne();
    }
}
