package com.tsid.auth.controller;

import com.tsid.auth.ResValue;
import com.tsid.auth.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RefreshScope
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping()
    public ResValue<NoticeResponse.NoticeBody> getNoticeList(Pageable pageable) {

        return new ResValue(noticeService.getNoticeList(pageable));
    }

    @GetMapping("/{noticeId}")
    public ResValue<NoticeResponse.NoticeDetail> getNoticeDetail(@PathVariable("noticeId") Long noticeId) {

        return new ResValue(noticeService.getNoticeDetail(noticeId));
    }
}
