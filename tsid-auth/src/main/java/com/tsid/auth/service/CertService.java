package com.tsid.auth.service;

import com.tsid.auth.common.Constants;
import com.tsid.auth.exception.AuthServerException;
import com.tsid.auth.exception.ErrCode;
import com.tsid.auth.exception.ErrMsg;
import com.tsid.auth.repo.*;
import com.tsid.auth.util.*;
import com.tsid.domain.entity.certRole.CertRole;
import com.tsid.domain.entity.company.Company;
import com.tsid.domain.entity.geoIpBlock.GeoIpBlock;
import com.tsid.domain.entity.group.Group;
import com.tsid.domain.entity.group.GroupRepository;
import com.tsid.domain.entity.groupCert.GroupCert;
import com.tsid.domain.entity.groupCert.GroupCertRepository;
import com.tsid.domain.entity.groupCertHistory.GroupCertHistory;
import com.tsid.domain.entity.groupCertHistory.GroupCertHistoryRepository;
import com.tsid.domain.entity.groupHasCompany.GroupHasCompany;
import com.tsid.domain.entity.groupHasCompany.GroupHasCompanyRepository;
import com.tsid.domain.entity.groupHasPermission.GroupHasPermission;
import com.tsid.domain.entity.groupHasPermission.GroupHasPermissionRepository;
import com.tsid.domain.entity.permission.Permission;
import com.tsid.domain.entity.user.User;
import com.tsid.domain.entity.user.UserRepository;
import com.tsid.domain.entity.userHasGroup.UserHasGroup;
import com.tsid.domain.entity.userHasGroup.UserHasGroupRepository;
import com.tsid.domain.entity.userNotification.UserNotification;
import com.tsid.domain.entity.userNotification.UserNotificationRepository;
import com.tsid.domain.enums.EActionFlag;
import com.tsid.domain.enums.EStatusFlag;
import com.tsid.domain.enums.group.EGroupPositionFlag;
import com.tsid.domain.enums.group.EGroupStatusFlag;
import com.tsid.domain.enums.notification.EAlarmFlag;
import com.tsid.domain.enums.notification.ETargetFlag;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.tsid.auth.common.Constants.ALARM_TITLE_CERT;
import static com.tsid.auth.common.Constants.TSID_DEEPLINK_URL;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertService {

    private final SnsUtil snsUtil;
    private final TokenUtil tokenUtil;

    private final AppRepo appRepo;
    private final UserRepo userRepo;
    private final CertRepo certRepo;
    private final GroupRepo groupRepo;
    private final CompanyRepo companyRepo;

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupCertRepository groupCertRepository;
    private final UserHasGroupRepository userHasGroupRepository;
    private final GroupHasCompanyRepository groupHasCompanyRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final GroupHasPermissionRepository groupHasPermissionRepository;
    private final GroupCertHistoryRepository groupCertHistoryRepository;

    @Value("${auth.di.key}")
    private String AUTH_DI_KEY;
    @Value("${auth.temp.key}")
    private String AUTH_TEMP_KEY;

    @Transactional
    public CertResponse.CertTokenResponse makeToken(...) {

        getUserInfo();
        Claims claims = tokenUtil.getClaims(accessToken);
        String clientId = claims.getAudience();

        Company companyInfo = companyRepo.getCompanyByClientId(clientId, request.getCallback_url(), request.getType());
        if (companyInfo == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_COMPANY);
        }

        String token = "token generate";

        return CertResponse.CertTokenResponse.builder()
                .build();
    }

    private User getUserInfo(){
        String uuid = "generate uuid";
        User userInfo = userRepository.getUserByUuid(uuid);

        if (userInfo == null || !userInfo.getStatus().equals(EStatusFlag.ACTIVE)) {
            throw new AuthServerException(ErrCode.FORBIDDEN_USER, ErrMsg.INVALID_USER);
        }
        return userInfo;
    }

    @Transactional
    public CertResponse.CertMakeResponse cert(...) {

        User userInfo = getUserInfo();

        /**
         * TSID 로그인 한 사용자에 대한 인증요청 API
         */
        Claims claims = tokenUtil.getClaims(accessToken);
        String clientId = claims.getAudience();

        Company companyInfo = companyRepo.getCompanyByClientId(clientId, request.getCallback_url(), request.getType());
        if (companyInfo == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_COMPANY);
        }

        Long callbackId = companyRepo.getCompanyCallbackId(companyInfo.getId(), request.getCallback_url());

        MakeCertInfo isAction = doCertAction(...);

        return CertResponse.CertMakeResponse.builder()
                .build();
    }

    @Transactional
    public MakeCertInfo doCertAction(...) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IpUtil.getIpAddress(request);
        String location = getLocation(ipAddress);

        Group groupInfo = groupRepo.getGroupByUserAndCompany(userInfo.getId(), companyInfo.getId());

        boolean isGroup = false;
        /**
         * 해당 사용처에 대한 그룹이 없으면 관련 데이터 생성
         * 그룹, 사용자-그룹, 그룹-사용처, 그룹-권한
         */
        ZonedDateTime nowTime = ZonedDateTime.now();
        if (groupInfo == null) {
            String groupName = getGroupName(userInfo.getId(), companyInfo.getName());
            groupInfo = Group.builder()
                    .build();
            groupRepository.save(groupInfo);

            UserHasGroup userHasGroup = UserHasGroup.builder()
                    .build();
            userHasGroupRepository.save(userHasGroup);

            GroupHasCompany groupHasCompany = GroupHasCompany.builder()
                    .build();
            groupHasCompanyRepository.save(groupHasCompany);

            Permission permissionInfo = groupRepo.getCertPermission();

            GroupHasPermission groupHasPermission = GroupHasPermission.builder()
                    .build();
            groupHasPermissionRepository.save(groupHasPermission);

            /**
             * 위임건 있는지 확인
             */
            groupRepo.checkDelegate(userInfo.getId(), companyInfo.getId());
        } else {
            Long userCount = groupRepo.getGroupUserCount(groupInfo.getId());
            if (userCount > 1) {
                isGroup = true;
            }
        }

        CertRole certRoleInfo = certRepo.getCertRoleByCode(grantType);
        CertMemberDto certMemberDto = certRepo.getGroupMemberInfo(groupInfo.getId());
        Long certCnt = certRepo.getCertCountByGroup(groupInfo.getId());

        int certCount = 1;
        if (certCnt != null) {
            certCount = certCnt.intValue() + 1;
        }

        /**
         * 인증 생성
         * stateCode : callback 할때 보내줘야 할 값
         */
        GroupCert groupCert = GroupCert.builder()
                .build();
        groupCert.setCreateDate(nowTime);
        groupCertRepository.save(groupCert);

        List<UserHasGroup> activeGroupUser = groupRepo.getUserHasGroupList(groupInfo.getId());
        for (UserHasGroup userHasGroup : activeGroupUser) {
            GroupCertHistory groupCertHistory = GroupCertHistory.builder()
                    .build();
            groupCertHistoryRepository.save(groupCertHistory);
        }

        List<Long> userIds = activeGroupUser.stream()
                .map(p -> p.getUser().getId()).collect(Collectors.toList());

        /**
         * 알림 등록
         */
        ETargetFlag targetFlag = ETargetFlag.ofName(pushTarget);

        String content = "<font color='#3881ED'><b>" + groupInfo.getName() + "</b></font>" +
                " 에서 <b><u>인증요청</u></b>이 왔습니다.";
        for (Long userId : userIds) {
            UserNotification noti = UserNotification.builder()
                    .build();
            userNotificationRepository.save(noti);
        }

        userRepo.insertUserActionLog(userInfo.getId(), EActionFlag.CERT_MAKE, groupCert.getId());

        /**
         * 그룹 사용자에게 push 보내기
         */
        String pushTitle = "인증요청 : " + certRoleInfo.getName();
        String pushMessage = groupInfo.getName() + " 에서 인증요청이 왔습니다!";

        PushDto.PushData payload = PushDto.PushData.builder()
                .build();

        snsUtil.push(payload, userIds);

        return new MakeCertInfo(true, isGroup);
    }

    private String getGroupName(Long userId, String name) {

        String groupName = name;

        int index = 1;
        while (true) {
            Long groupCount = groupRepo.getSameGroupCount(userId, groupName);
            if (groupCount == null || groupCount == 0) {
                break;
            }
            groupName = name + index;
            index++;
        }

        return groupName;
    }

    private boolean ipMathes(String ip, String subnet) {
        IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(subnet);
        return ipAddressMatcher.matches(ip);
    }

    private String getLocation(String ip) {

        /**
         * ip를 통해 지역 유추
         */
        String [] ipArr = ip.split("\\.");

        String location = Constants.LOCATION_DEFAULT;
        if (ipArr.length < 4) {
            return location;
        }

        try {
            String network = ipArr[0] + "." + ipArr[1] + ".";
            GeoIpBlock findBlock = null;

            List<GeoIpBlock> ipBlocks = appRepo.getIpBlocks(network);
            for (GeoIpBlock ipBlock : ipBlocks) {
                if (ipMathes(ip, ipBlock.getNetwork())) {
                    findBlock = ipBlock;
                    break;
                }
            }

            if (findBlock != null) {
                String addr = appRepo.getAddress(findBlock.getGeoname_id());
                location = Constants.LOCATION_KOREA_SPACE + addr;
            }
        } catch (Exception e) {
            return location;
        }

        return location;
    }

    private String zonedDateTimeToString(ZonedDateTime time) {
        if(time == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return time.format(formatter);
    }

    @Transactional(readOnly = true)
    public CertResponse.CertCheckResponse certCheck(...) {
        /**
         * 해당 정보에 대한 인증이 완료된거 맞는지 체크
         */
        User userInfo = getUserInfo();
        Claims claims = tokenUtil.getClaims(accessToken);
        String clientId = claims.getAudience();

        Company companyInfo = companyRepo.getCompanyByClientAndCode(clientId, request.getType());
        if (companyInfo == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.INVALID_COMPANY);
        }

        GroupCert cert = certRepo.getCertByUserAndState(userInfo.getId(), request.getState());
        if (cert == null) {
            throw new AuthServerException(ErrCode.INVALID_PARAMETER, ErrMsg.STATE_CODE_CONFLICT);
        }

        String certDate = null;
        if (cert.getIsCert()) {
            certDate = zonedDateTimeToString(cert.getUpdateDate());
        }

        return CertResponse.CertCheckResponse.builder()
                .build();
    }
}
