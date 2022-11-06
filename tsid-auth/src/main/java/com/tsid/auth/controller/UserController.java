package com.tsid.auth.controller;

import com.tsid.auth.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getUser(){

        return userService.getUser();
    }

    @PostMapping("/check")
    @ApiOperation(value = "체크", hidden = true)
    public CheckResponse checkCi(...) {

        return userService.checkCiValue(...);
    }
}
