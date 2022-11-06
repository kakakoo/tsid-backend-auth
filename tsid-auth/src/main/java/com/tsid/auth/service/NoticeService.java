package com.tsid.auth.service;

import com.tsid.auth.repo.AppRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeService {

    private final AppRepo appRepo;

    @Transactional(readOnly = true)
    public NoticeResponse.NoticeBody getNoticeList(Pageable pageable) {

        List<NoticeResponse.NoticeDetail> noticeList = appRepo.getNoticeListByPaging(pageable);
        return new NoticeResponse.NoticeBody(noticeList);
    }

    @Transactional(readOnly = true)
    public NoticeResponse.NoticeDetail getNoticeDetail(Long noticeId) {

        return appRepo.getNoticeDetail(noticeId);
    }
}
