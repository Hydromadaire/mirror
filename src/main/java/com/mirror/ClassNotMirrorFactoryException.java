package com.mirror;

public class ClassNotMirrorFactoryException extends RuntimeException {

    public ClassNotMirrorFactoryException(Class<?> cls) {
        super(cls.getName());
    }
}
