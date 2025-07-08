package com.example.developmenttasks.common.security.aop;

import com.example.developmenttasks.auth.entity.UserRole;
import com.example.developmenttasks.common.exception.CustomException;
import lombok.RequiredArgsConstructor;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;



@Aspect
@Component
@RequiredArgsConstructor
public class RoleAccessAspect {

    @Before("@annotation(com.example.developmenttasks.common.security.annotation.Admin)")
    public void checkAdminAccess() {
        checkRole(UserRole.ADMIN);
    }

    @Before("@annotation(com.example.developmenttasks.common.security.annotation.User)")
    public void checkUserAccess() {
        checkRole(UserRole.USER);
    }

    private void checkRole(UserRole requiredRole) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof  AuthMember authMember)){
            throw new CustomException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "유효하지 않은 인증 토큰입니다.");
        }

        if (!authMember.getUserRoles().contains(requiredRole)){
            throw new CustomException(
                    HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    requiredRole == UserRole.ADMIN ?
                            "관리자 권한이 필요한 요청입니다. 접근 권한이 없습니다." :
                            "접근 권한이 없습니다."
            );
        }
    }
}
