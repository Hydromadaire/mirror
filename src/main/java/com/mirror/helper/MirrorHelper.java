package com.mirror.helper;

import com.mirror.MirroredClass;
import com.mirror.ObjectFactory;

public class MirrorHelper {

    public boolean isMirror(Class<?> type) {
        return type.isAnnotationPresent(MirroredClass.class);
    }

    public String getMirroredTypeName(Class<?> type) {
        if (!isMirror(type)) {
            throw new IllegalArgumentException("type not a mirror: " + type.getName());
        }

        MirroredClass mirroredClass = type.getAnnotation(MirroredClass.class);
        return mirroredClass.value();
    }

    public boolean isObjectFactory(Class<?> type) {
        return type.isAnnotationPresent(ObjectFactory.class);
    }

    public Class<?> getObjectFactoryType(Class<?> type) {
        if (!isObjectFactory(type)) {
            throw new IllegalArgumentException("type not a object factory: " + type.getName());
        }

        ObjectFactory objectFactory = type.getAnnotation(ObjectFactory.class);
        return objectFactory.value();
    }
}
