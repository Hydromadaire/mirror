package com.mirror;

public class MirrorFieldAccessException extends RuntimeException {

    public MirrorFieldAccessException(Throwable cause) {
        super(cause);
    }

    public MirrorFieldAccessException(String message) {
        super(message);
    }
}
