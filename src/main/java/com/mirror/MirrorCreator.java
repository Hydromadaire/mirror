package com.mirror;

import com.mirror.helper.ReflectionHelper;
import com.mirror.helper.MirrorHelper;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.Wrapper;

public class MirrorCreator {

    private final ClassLoader mClassLoader;
    private final MirrorHelper mMirrorHelper;
    private final ReflectionHelper mReflectionHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final MirrorValidator mMirrorValidator;

    public MirrorCreator(ClassLoader classLoader, MirrorHelper mirrorHelper, ReflectionHelper reflectionHelper, ThrowableWrapper throwableWrapper, MirrorValidator mirrorValidator) {
        mClassLoader = classLoader;
        mMirrorHelper = mirrorHelper;
        mReflectionHelper = reflectionHelper;
        mThrowableWrapper = throwableWrapper;
        mMirrorValidator = mirrorValidator;
    }

    private MirrorCreator(ClassLoader classLoader) {
        mClassLoader = classLoader;
        mMirrorHelper = new MirrorHelper();
        mReflectionHelper = new ReflectionHelper(new Wrapper(mMirrorHelper, this), new Unwrapper(mMirrorHelper, mClassLoader));
        mThrowableWrapper = new ThrowableWrapper();
        mMirrorValidator = new MirrorValidator(mMirrorHelper);
    }

    public <T> Mirror<T> createMirror(Class<T> mirrorClass) throws MirrorCreationException {
        try {
            mMirrorValidator.validateMirrorClass(mirrorClass);
            Class<?> targetClass = getTargetType(mirrorClass);
            return new Mirror<T>(mirrorClass, targetClass, mReflectionHelper, mThrowableWrapper);
        } catch (ClassNotFoundException | ClassNotMirrorException | MirrorValidationException e) {
            throw new MirrorCreationException(e);
        }
    }

    private Class<?> getTargetType(Class<?> mirrorClass) throws ClassNotFoundException {
        String targetTypeName = mMirrorHelper.getMirroredTypeName(mirrorClass);
        return Class.forName(targetTypeName, true, mClassLoader);
    }

    public static MirrorCreator createForClassLoader(ClassLoader classLoader) {
        return new MirrorCreator(classLoader);
    }
}
