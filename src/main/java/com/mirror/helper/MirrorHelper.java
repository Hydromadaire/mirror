package com.mirror.helper;

import com.mirror.MirroredClass;

public class MirrorHelper {

    public boolean isMirror(Class<?> type) {
        return type.isAnnotationPresent(MirroredClass.class);
    }

    public String getMirroredTypeName(Class<?> type) {
        MirroredClass mirroredClass = type.getAnnotation(MirroredClass.class);
        return mirroredClass.value();
    }
}
