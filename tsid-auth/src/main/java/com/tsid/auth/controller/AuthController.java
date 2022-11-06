package com.tsid.auth.controller;

import com.tsid.auth.ResValue;
import com.tsid.auth.service.AuthService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class AuthController {

    private final AuthService authService;
    private final LocaleResolver localeResolver;

    @GetMapping("/authorize")
    @ApiOperation(value = "인가 요청", hidden = true)
    public String authorize (...)  {

        AuthorizeDto dto = authService.authorizeKey(...);

        localeResolver.setLocale(request, response, new Locale(dto.getLang()));
        return "tsid";
    }

    @PostMapping("/token/check")
    @ApiOperation(value = "state 코드 체크", hidden = true)
    public ResponseEntity<ResValue<AuthResponse.CheckStateCode>> checkToken (...) {

        ResValue<AuthResponse.CheckStateCode> resValue = authService.checkStateCode(...);
        return new ResponseEntity<>(resValue, HttpStatus.OK);
    }

    @PostMapping("/authorize")
    @ApiOperation(value = "인가 확인", hidden = true)
    public ResponseEntity<ResValue<AuthResponse.AuthResult>> authorize (...) {

        ResValue<AuthResponse.AuthResult> resValue = authService.authorize(...);
        return new ResponseEntity<>(resValue, HttpStatus.OK);
    }

    @PostMapping("/authorize/check")
    @ApiOperation(value = "인가 체크", hidden = true)
    public ResponseEntity<ResValue<AuthResponse.AuthCheck>> authorizeCheck (...) {

        ResValue<AuthResponse.AuthCheck> resValue = authService.authorizeCheck(...);
        return new ResponseEntity<>(resValue, HttpStatus.OK);
    }

    @PostMapping("/authorize/agree")
    @ApiOperation(value = "이용 동의 받기", hidden = true)
    public ResponseEntity<ResValue<AuthResponse.AuthResult>> authorizeAgree (...) {

        ResValue<AuthResponse.AuthResult> resValue = authService.agreeCheck(...);
        return new ResponseEntity<>(resValue, HttpStatus.OK);
    }

    @GetMapping("/authorize/success")
    @ApiOperation(value = "인가 완료", hidden = true)
    public ResponseEntity<ResValue<AuthResponse.SuccessUrl>> redirectCallback(...) {

        ResValue<AuthResponse.SuccessUrl> resValue = authService.redirectCallback(...);
        return new ResponseEntity<>(resValue, HttpStatus.OK);
    }

    @PostMapping(value = "/token")
    @ApiOperation(value = "토큰 발급 요청")
    public ResponseEntity<TokenDto.Token> authToken(...){

        TokenDto.Token token = authService.getToken(...);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping(value = "/token", consumes = {"application/x-www-form-urlencoded"})
    @ApiOperation(value = "토큰 발급 요청")
    public ResponseEntity<TokenDto.Token> authToken(...){


        TokenDto.Token token = authService.getToken(...);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

}
