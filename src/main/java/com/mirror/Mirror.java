package com.mirror;

import com.mirror.helper.InvocationHelper;

import java.lang.reflect.Proxy;

public class Mirror<T> {

    private Class<?> mTargetClass;
    private Class<T> mMirrorClass;
    private InvocationHelper mInvocationHelper;

    public Mirror(Class<T> mirrorClass, Class<?> targetClass, InvocationHelper invocationHelper) {
        mMirrorClass = mirrorClass;
        mTargetClass = targetClass;
        mInvocationHelper = invocationHelper;
    }

    public T create(Object instance) {
        if (!mTargetClass.isInstance(instance)) {
            throw new IllegalArgumentException("instance is not of targetClass type: " + mTargetClass.getName());
        }

        return createProxy(instance);
    }

    private T createProxy(Object instance) {
        return (T) Proxy.newProxyInstance(
                mMirrorClass.getClassLoader(),
                new Class[] {mMirrorClass},
                new MirrorInvocationHandler(mInvocationHelper, mTargetClass, instance));
    }
}
