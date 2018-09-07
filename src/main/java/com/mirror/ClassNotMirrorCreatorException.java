package com.mirror;

public class ClassNotMirrorCreatorException extends RuntimeException {

    public ClassNotMirrorCreatorException(Class<?> cls) {
        super(cls.getName());
    }
}
