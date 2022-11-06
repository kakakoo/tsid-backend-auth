package com.tsid.auth.controller;

import com.tsid.auth.ResValue;
import com.tsid.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RefreshScope
@RequiredArgsConstructor
public class QnaController {

    private final UserService userService;

    @PostMapping("/qna")
    public ResValue makeQna(...){
        userService.insertQna(...);
        return new ResValue("OK");
    }
}
