package com.hqc.hophuddles.security;

import com.hqc.hophuddles.enums.Permission;
import com.hqc.hophuddles.exception.UnauthorizedException;
import com.hqc.hophuddles.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

    private final PermissionService permissionService;

    @Around("@annotation(requirePermission)")
    public Object enforcePermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Permission permission = requirePermission.value();
        String resourceIdParam = requirePermission.resourceIdParam();
        String resourceType = requirePermission.resourceType();
        String message = requirePermission.message();

        // Get resource ID from method parameters
        Long resourceId = extractResourceId(joinPoint, resourceIdParam);

        if (!permissionService.hasPermission(permission, resourceId, resourceType)) {
            log.warn("Access denied: User lacks permission {} for resource {} of type {}",
                    permission, resourceId, resourceType);
            throw new UnauthorizedException(message);
        }

        return joinPoint.proceed();
    }

    private Long extractResourceId(ProceedingJoinPoint joinPoint, String paramName) {
        if (paramName.isEmpty()) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName)) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof Number) {
                    return ((Number) arg).longValue();
                }
            }
        }

        return null;
    }
}