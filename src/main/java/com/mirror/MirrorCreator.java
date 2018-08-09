package com.mirror;

import com.mirror.helper.InvocationHelper;
import com.mirror.helper.MirrorHelper;

public class MirrorCreator {

    private final ClassLoader mClassLoader;
    private final MirrorHelper mMirrorHelper;
    private final InvocationHelper mInvocationHelper;

    public MirrorCreator(ClassLoader classLoader, MirrorHelper mirrorHelper, InvocationHelper invocationHelper) {
        mClassLoader = classLoader;
        mMirrorHelper = mirrorHelper;
        mInvocationHelper = invocationHelper;
    }

    public <T> Mirror<T> createMirror(Class<T> mirrorClass) throws MirrorCreationException {
        try {
            Class<?> targetClass = getTargetType(mirrorClass);
            return new Mirror<T>(mirrorClass, targetClass, mInvocationHelper);
        } catch (ClassNotFoundException e) {
            throw new MirrorCreationException(e);
        }
    }

    private Class<?> getTargetType(Class<?> mirrorClass) throws ClassNotFoundException {
        String targetTypeName = mMirrorHelper.getMirroredTypeName(mirrorClass);
        return Class.forName(targetTypeName, true, mClassLoader);
    }
}
