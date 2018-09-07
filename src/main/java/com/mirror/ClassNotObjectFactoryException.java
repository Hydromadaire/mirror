package com.mirror;

public class ClassNotObjectFactoryException extends RuntimeException {

    public ClassNotObjectFactoryException(Class<?> cls) {
        super(cls.getName());
    }
}
