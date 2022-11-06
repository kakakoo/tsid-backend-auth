package com.tsid.auth.repo;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tsid.domain.entity.company.Company;
import com.tsid.domain.enums.EStatusFlag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.tsid.domain.entity.certRole.QCertRole.certRole;
import static com.tsid.domain.entity.company.QCompany.company;
import static com.tsid.domain.entity.companyCallback.QCompanyCallback.companyCallback;
import static com.tsid.domain.entity.companyDetail.QCompanyDetail.companyDetail;
import static com.tsid.domain.entity.companyHasCertRole.QCompanyHasCertRole.companyHasCertRole;

@Component
@RequiredArgsConstructor
public class CompanyRepo {

    private final JPAQueryFactory jpaQueryFactory;


    public Company getCompanyByClientId(String clientId) {
        return getCompanyByClientId(clientId, null, null);
    }

    public Company getCompanyByClientId(String clientId, String redirectUri, String grantType) {
        return jpaQueryFactory
                .select(company)
                .from(companyDetail)
                .join(companyDetail.company, company).on(company.status.in(EStatusFlag.ACTIVE, EStatusFlag.HIDE))
                .join(company.companyHasCertRoles, companyHasCertRole)
                .join(companyCallback).on(company.id.eq(companyCallback.company.id))
                .join(companyHasCertRole.certRole, certRole)
                .where(checkCondition(clientId, redirectUri, grantType))
                .fetchOne();
    }

    private BooleanExpression checkCondition(String clientId, String redirectUri, String grantType) {

        if (clientId != null && redirectUri != null && grantType != null) {
            return companyDetail.clientId.eq(clientId)
                    .and(companyCallback.callback.eq(redirectUri))
                    .and(certRole.code.eq(grantType));
        } else if (clientId != null && redirectUri != null){
            return companyDetail.clientId.eq(clientId)
                    .and(companyCallback.callback.eq(redirectUri));
        } else if (clientId != null && grantType != null){
            return companyDetail.clientId.eq(clientId)
                    .and(certRole.code.eq(grantType));
        } else {
            return companyDetail.clientId.eq(clientId);
        }
    }

    public Long getCompanyCallbackId(Long companyId, String redirectUri) {
        return jpaQueryFactory
                .select(companyCallback.id)
                .from(companyCallback)
                .where(companyCallback.company.id.eq(companyId),
                        companyCallback.callback.eq(redirectUri))
                .fetchOne();
    }

    public Company getCompanyByClientAndUrl(String clientId, String secret, String redirectUri) {
        return jpaQueryFactory
                .select(company)
                .from(companyDetail)
                .join(company).on(companyDetail.company.id.eq(company.id),
                        company.status.in(EStatusFlag.ACTIVE, EStatusFlag.HIDE))
                .join(companyCallback).on(company.id.eq(companyCallback.company.id),
                        companyCallback.callback.eq(redirectUri))
                .where(companyDetail.clientId.eq(clientId),
                        companyDetail.secret.eq(secret))
                .fetchOne();
    }

}
