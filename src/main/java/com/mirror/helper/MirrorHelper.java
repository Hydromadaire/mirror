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

    public Class<?> getMirrorCreatorType(Class<?> type) {
        if (!isMirrorCreator(type)) {
            throw new IllegalArgumentException("type not a object factory: " + type.getName());
        }

        MirrorCreator mirrorCreator = type.getAnnotation(MirrorCreator.class);
        return mirrorCreator.value();
    }
}
