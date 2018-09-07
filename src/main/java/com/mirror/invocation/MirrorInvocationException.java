package com.mirror.invocation;

public class MirrorInvocationException extends RuntimeException {

    public MirrorInvocationException(Throwable cause) {
        super(cause);
    }

    public MirrorInvocationException(String message) {
        super(message);
    }
}
