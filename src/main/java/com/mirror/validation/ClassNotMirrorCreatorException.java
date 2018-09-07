package com.mirror.validation;

public class ClassNotMirrorCreatorException extends RuntimeException {

    public ClassNotMirrorCreatorException(Class<?> cls) {
        super(cls.getName());
    }
}
