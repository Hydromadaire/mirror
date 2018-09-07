package com.mirror.invocation;

public class MirrorCreationByProxyException extends RuntimeException {

    public MirrorCreationByProxyException(Throwable cause) {
        super(cause);
    }

    public MirrorCreationByProxyException(String message) {
        super(message);
    }
}
