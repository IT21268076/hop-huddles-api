// UnauthorizedException.java
package com.hqc.hophuddles.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}