package com.tsid.auth.repo;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tsid.domain.entity.geoIpBlock.GeoIpBlock;
import com.tsid.domain.entity.serverInfo.ServerInfo;
import com.tsid.domain.enums.notice.ENoticeStatusFlag;
import com.tsid.domain.enums.notice.ENoticeTypeFlag;
import com.tsid.domain.enums.term.ETermGroupFlag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static com.tsid.domain.entity.geoCity.QGeoCity.geoCity;
import static com.tsid.domain.entity.geoIpBlock.QGeoIpBlock.geoIpBlock;
import static com.tsid.domain.entity.notice.QNotice.notice;
import static com.tsid.domain.entity.serverInfo.QServerInfo.serverInfo;
import static com.tsid.domain.entity.term.QTerm.term;
import static com.tsid.domain.entity.termGroup.QTermGroup.termGroup;

@Component
@RequiredArgsConstructor
public class AppRepo {

    private final JPAQueryFactory jpaQueryFactory;

    public String getAddress(Long geonameId) {
        return jpaQueryFactory
                .select(geoCity.name)
                .from(geoCity)
                .where(geoCity.geoname_id.eq(geonameId))
                .fetchOne();
    }

    public List<NoticeResponse.NoticeDetail> getNoticeListByPaging(Pageable pageable) {
        ZonedDateTime nowTime = ZonedDateTime.now();
        return jpaQueryFactory
                .select(Projections.bean(
                        NoticeResponse.NoticeDetail.class,
                        notice.id,
                        notice.title,
                        notice.noticeType,
                        notice.createDate))
                .from(notice)
                .where(notice.startDate.before(nowTime),
                        notice.endDate.after(nowTime),
                        notice.status.eq(ENoticeStatusFlag.ACTIVE))
                .orderBy(new CaseBuilder().when(notice.noticeType.eq(ENoticeTypeFlag.EMERGENCY)).then(1).otherwise(2).asc(),
                        notice.id.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
    }

    public NoticeResponse.NoticeDetail getNoticeDetail(Long noticeId) {
        return jpaQueryFactory
                .select(Projections.bean(
                        NoticeResponse.NoticeDetail.class,
                        notice.id,
                        notice.title,
                        notice.description,
                        notice.noticeType,
                        notice.createDate))
                .from(notice)
                .where(notice.id.eq(noticeId))
                .fetchOne();
    }
}
