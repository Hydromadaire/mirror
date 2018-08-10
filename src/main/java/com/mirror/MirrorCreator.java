package com.mirror;

import com.mirror.helper.InvocationHelper;
import com.mirror.helper.MirrorHelper;
import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.Wrapper;

public class MirrorCreator {

    private final ClassLoader mClassLoader;
    private final MirrorHelper mMirrorHelper;
    private final InvocationHelper mInvocationHelper;

    public MirrorCreator(ClassLoader classLoader, MirrorHelper mirrorHelper, InvocationHelper invocationHelper) {
        mClassLoader = classLoader;
        mMirrorHelper = mirrorHelper;
        mInvocationHelper = invocationHelper;
    }

    private MirrorCreator(ClassLoader classLoader) {
        mClassLoader = classLoader;
        mMirrorHelper = new MirrorHelper();
        mInvocationHelper = new InvocationHelper(new Wrapper(mMirrorHelper, this), new Unwrapper(mMirrorHelper, mClassLoader));
    }

    public <T> Mirror<T> createMirror(Class<T> mirrorClass) throws MirrorCreationException {
        try {
            validateMirrorClass(mirrorClass);
            Class<?> targetClass = getTargetType(mirrorClass);
            return new Mirror<T>(mirrorClass, targetClass, mInvocationHelper);
        } catch (ClassNotFoundException | ClassNotMirrorException | MirrorValidationException e) {
            throw new MirrorCreationException(e);
        }
    }

    private Class<?> getTargetType(Class<?> mirrorClass) throws ClassNotFoundException {
        String targetTypeName = mMirrorHelper.getMirroredTypeName(mirrorClass);
        return Class.forName(targetTypeName, true, mClassLoader);
    }

    private void validateMirrorClass(Class<?> mirrorClass) {
        if (!mMirrorHelper.isMirror(mirrorClass)) {
            throw new ClassNotMirrorException(mirrorClass);
        }

        if (!mirrorClass.isInterface()) {
            throw new MirrorValidationException("mirror should be an interface");
        }
    }

    public static MirrorCreator createForClassLoader(ClassLoader classLoader) {
        return new MirrorCreator(classLoader);
    }
}
