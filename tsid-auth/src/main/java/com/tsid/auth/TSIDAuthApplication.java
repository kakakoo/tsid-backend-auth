package com.tsid.auth;

import co.elastic.apm.attach.ElasticApmAttacher;
import com.tsid.domain.TsidDomainRoot;
import com.tsid.external.TsidExternalRoot;
import com.tsid.internal.TsidInternalRoot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@EnableFeignClients
@Slf4j
@SpringBootApplication(scanBasePackageClasses = {
        TSIDAuthApplication.class,
        TsidDomainRoot.class,
        TsidExternalRoot.class,
        TsidInternalRoot.class
})
public class TSIDAuthApplication {

    @Value("${elastic.apm.server-url}")
    private String SERVER_URL;

    @Value("${elastic.apm.service-name}")
    private String SERVICE_NAME;

    @Value("${elastic.apm.environment}")
    private String ENVIRONMENT;

    @Value("${elastic.apm.application-package}")
    private String APPLICATION_PACKAGE;

    @Value("${elastic.apm.log-level}")
    private String LOG_LEVEL;

    @Value("${elastic.apm.capture-body}")
    private String CAPTURE_BODY;

    @PostConstruct
    public void init() {
        log.info("지금 시간 ::: " + ZonedDateTime.now());
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        log.info("지금 시간 ::: " + ZonedDateTime.now());

        Map<String, String> apmProps = new HashMap<>();
        apmProps.put("server_url", SERVER_URL);
        apmProps.put("service_name", SERVICE_NAME);
        apmProps.put("environment", ENVIRONMENT);
        apmProps.put("application_packages", APPLICATION_PACKAGE);
        apmProps.put("log_level", LOG_LEVEL);
        apmProps.put("capture_body", CAPTURE_BODY);
        apmProps.put("sanitize_field_names", "*token*");
        ElasticApmAttacher.attach(apmProps);
    }

    public static void main(String[] args) {
        SpringApplication.run(TSIDAuthApplication.class, args);
    }
}
