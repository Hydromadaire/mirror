package com.mirror;

import com.mirror.helper.ReflectionHelper;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.WrappingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MirrorFactoryInvocationHandler implements InvocationHandler {

    private final ReflectionHelper mReflectionHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final Class<?> mTargetClass;
    private final Class<?> mMirrorClass;

    public MirrorFactoryInvocationHandler(ReflectionHelper reflectionHelper, ThrowableWrapper throwableWrapper, Class<?> targetClass, Class<?> mirrorClass) {
        mReflectionHelper = reflectionHelper;
        mThrowableWrapper = throwableWrapper;
        mTargetClass = targetClass;
        mMirrorClass = mirrorClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null) {
            args = new Object[0];
        }

        return invokeConstructor(method, args);
    }

    private Object invokeConstructor(Method method, Object[] args) throws Throwable {
        try {
            Constructor<?> constructor = mReflectionHelper.findMirrorConstructor(method, mTargetClass);
            return mReflectionHelper.invokeMirrorConstructor(constructor, mMirrorClass, args);
        } catch (UnwrappingException | WrappingException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new MirrorInvocationException(e);
        } catch (InvocationTargetException e) {
            mThrowableWrapper.tryMirrorThrowable(e.getCause(), method);
            throw e.getCause();
        }
    }
}
