package com.tsid.auth.controller;

import com.tsid.auth.service.CertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/cert")
public class CertController {

    private final CertService certService;

    /**
     * 인증요청 - 딥링크
     */
    @PostMapping("/{version}/link")
    public ResponseEntity<CertResponse.CertTokenResponse> makeToken(...){

        CertResponse.CertTokenResponse response = certService.makeToken(...);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/{version}/link", consumes = {"application/x-www-form-urlencoded"})
    public ResponseEntity<CertResponse.CertTokenResponse> makeToken(...){

        CertResponse.CertTokenResponse response = certService.makeToken(...);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 인증요청 - 푸시타입
     */
    @PostMapping("/{version}/push")
    public ResponseEntity<CertResponse.CertMakeResponse> cert(...){

        return new ResponseEntity<>(certService.cert(...), HttpStatus.OK);
    }

    @PostMapping(value = "/{version}/push", consumes = {"application/x-www-form-urlencoded"})
    public ResponseEntity<CertResponse.CertMakeResponse> cert(...){

        return new ResponseEntity<>(certService.cert(...), HttpStatus.OK);
    }

    /**
     * 인증에 대한 결과 확인
     * @return
     */
    @GetMapping("/{version}/check")
    public ResponseEntity<CertResponse.CertCheckResponse> certCheck(...){

        CertResponse.CertCheckResponse response = certService.certCheck(...);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
