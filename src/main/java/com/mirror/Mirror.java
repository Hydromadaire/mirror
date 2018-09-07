package com.mirror;

import com.mirror.helper.ReflectionHelper;
import com.mirror.helper.MirrorHelper;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.Wrapper;

import java.lang.reflect.Proxy;

public class Mirror {

    private final ClassLoader mClassLoader;
    private final MirrorHelper mMirrorHelper;
    private final ReflectionHelper mReflectionHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final MirrorValidator mMirrorValidator;

    public Mirror(ClassLoader classLoader, MirrorHelper mirrorHelper, ReflectionHelper reflectionHelper, ThrowableWrapper throwableWrapper, MirrorValidator mirrorValidator) {
        mClassLoader = classLoader;
        mMirrorHelper = mirrorHelper;
        mReflectionHelper = reflectionHelper;
        mThrowableWrapper = throwableWrapper;
        mMirrorValidator = mirrorValidator;
    }

    private Mirror(ClassLoader classLoader) {
        mClassLoader = classLoader;
        mMirrorHelper = new MirrorHelper();
        mReflectionHelper = new ReflectionHelper(new Wrapper(mMirrorHelper, this), new Unwrapper(mMirrorHelper, mClassLoader));
        mThrowableWrapper = new ThrowableWrapper();
        mMirrorValidator = new MirrorValidator(mMirrorHelper);
    }

    public <T> T mirror(Class<T> mirrorClass, Object instance) throws MirrorCreationException {
        try {
            mMirrorValidator.validateMirrorClass(mirrorClass);
            Class<?> targetClass = getTargetType(mirrorClass);

            if (!targetClass.isInstance(instance)) {
                throw new IllegalArgumentException("instance is not of targetClass type: " + targetClass.getName());
            }

            return createMirrorProxy(mirrorClass, targetClass, instance);
        } catch (ClassNotFoundException | ClassNotMirrorException | MirrorValidationException e) {
            throw new MirrorCreationException(e);
        }
    }

    public <T> T createMirrorCreator(Class<T> mirrorCreatorClass) throws MirrorCreatorCreationException {
        try {
            mMirrorValidator.validateMirrorCreatorClass(mirrorCreatorClass);

            Class<?> mirrorClass = mMirrorHelper.getMirrorCreatorType(mirrorCreatorClass);
            mMirrorValidator.validateMirrorClass(mirrorClass);

            Class<?> targetClass = getTargetType(mirrorClass);
            return createMirrorCreatorProxy(mirrorCreatorClass, mirrorClass, targetClass);
        } catch (ClassNotFoundException | ClassNotMirrorException | MirrorValidationException | ClassNotMirrorCreatorException e) {
            throw new MirrorCreatorCreationException(e);
        }
    }

    private Class<?> getTargetType(Class<?> mirrorClass) throws ClassNotFoundException {
        String targetTypeName = mMirrorHelper.getMirroredTypeName(mirrorClass);
        return Class.forName(targetTypeName, true, mClassLoader);
    }

    private <T> T createMirrorProxy(Class<T> mirrorClass, Class<?> targetClass, Object instance) {
        return mirrorClass.cast(Proxy.newProxyInstance(
                mirrorClass.getClassLoader(),
                new Class[] {mirrorClass},
                new MirrorInvocationHandler(mReflectionHelper, mThrowableWrapper, targetClass, instance)));
    }

    private <T> T createMirrorCreatorProxy(Class<T> mirrorCreatorClass, Class<?> mirrorClass, Class<?> targetClass) {
        return mirrorCreatorClass.cast(Proxy.newProxyInstance(
                mirrorCreatorClass.getClassLoader(),
                new Class[] {mirrorCreatorClass},
                new MirrorCreatorInvocationHandler(mReflectionHelper, mThrowableWrapper, targetClass, mirrorClass)));
    }

    public static Mirror createForClassLoader(ClassLoader classLoader) {
        return new Mirror(classLoader);
    }
}
