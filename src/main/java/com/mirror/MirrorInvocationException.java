package com.mirror;

public class MirrorInvocationException extends RuntimeException {

    public MirrorInvocationException(Throwable cause) {
        super(cause);
    }

    public MirrorInvocationException(String message) {
        super(message);
    }
}
