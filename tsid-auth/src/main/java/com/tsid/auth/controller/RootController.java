package com.tsid.auth.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@ApiIgnore
@RestController
@Slf4j
@RefreshScope
@RequiredArgsConstructor
public class RootController {

    @Value("${server.name}")
    private String SERVER_NAME;

    @GetMapping("/")
    @ApiOperation(value = "root", hidden = true)
    public Map<String, Object> root() {
        return new HashMap<>();
    }

    @GetMapping("/health")
    @ApiOperation(value = "health check", hidden = true)
    public Map<String, Object> rootCont() throws UnknownHostException {

        Map<String, Object> reqMap = new HashMap<>();
        InetAddress localHost = InetAddress.getLocalHost();
        String hostAddress = localHost.getHostAddress();

        reqMap.put("server", SERVER_NAME);
        reqMap.put("address", hostAddress);
        reqMap.put("status", true);

        return reqMap;
    }

}
