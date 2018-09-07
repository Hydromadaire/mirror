package com.mirror.wrapping;

public class ThrowableWrappingException extends RuntimeException {

    public ThrowableWrappingException(Throwable cause) {
        super(cause);
    }

    public ThrowableWrappingException(String message) {
        super(message);
    }
}
