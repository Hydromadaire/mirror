package com.mirror.helper;

import com.mirror.MirroredClass;
import com.mirror.MirrorFactory;

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
        return type.isAnnotationPresent(MirrorFactory.class);
    }

    public Class<?> getObjectFactoryType(Class<?> type) {
        if (!isObjectFactory(type)) {
            throw new IllegalArgumentException("type not a object factory: " + type.getName());
        }

        MirrorFactory mirrorFactory = type.getAnnotation(MirrorFactory.class);
        return mirrorFactory.value();
    }
}
