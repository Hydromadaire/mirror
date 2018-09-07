package com.mirror.helper;

import com.mirror.MirroredClass;
import com.mirror.MirrorCreator;

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

    public boolean isMirrorCreator(Class<?> type) {
        return type.isAnnotationPresent(MirrorCreator.class);
    }

    public Class<?> getMirrorTargetType(Class<?> mirrorClass, ClassLoader classLoader) throws ClassNotFoundException {
        String targetTypeName = getMirroredTypeName(mirrorClass);
        return Class.forName(targetTypeName, true, classLoader);
    }
}
