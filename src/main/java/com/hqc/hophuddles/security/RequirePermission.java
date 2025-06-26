package com.hqc.hophuddles.security;

import com.hqc.hophuddles.enums.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    Permission value();
    String resourceIdParam() default "";
    String resourceType() default "AGENCY";
    String message() default "Access denied";
}