package com.mirror;

import com.mirror.helper.ReflectionHelper;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.WrappingException;

import java.lang.reflect.*;

public class MirrorInvocationHandler implements InvocationHandler {

    private final ReflectionHelper mReflectionHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final Class<?> mTargetClass;
    private final Object mTargetInstance;

    public MirrorInvocationHandler(ReflectionHelper reflectionHelper, ThrowableWrapper throwableWrapper, Class<?> targetClass, Object targetInstance) {
        mReflectionHelper = reflectionHelper;
        mThrowableWrapper = throwableWrapper;
        mTargetClass = targetClass;
        mTargetInstance = targetInstance;
    }

    public Object getTargetInstance() {
        return mTargetInstance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null) {
            args = new Object[0];
        }

        if (method.isAnnotationPresent(GetField.class)) {
            GetField getField = method.getAnnotation(GetField.class);
            return getFieldValue(getField.value(), method.getReturnType());
        }

        return invokeMethod(method, args);
    }

    private Object getFieldValue(String fieldName, Class<?> fieldReturnType) {
        try {
            Field field = mReflectionHelper.findMirrorField(fieldName, mTargetClass);

            Object instance = Modifier.isStatic(field.getModifiers()) ? null : mTargetInstance;
            return mReflectionHelper.getFieldValue(field, instance, fieldReturnType);
        } catch (ReflectiveOperationException | WrappingException e) {
            throw new MirrorFieldAccessException(e);
        }
    }

    public Object invokeMethod(Method method, Object[] args) throws Throwable {
        try {
            String mirroredMethodName = method.getName();
            Method mirroredMethod = mReflectionHelper.findMirrorMethod(method, mirroredMethodName, mTargetClass);

            Object instance = Modifier.isStatic(mirroredMethod.getModifiers()) ? null : mTargetInstance;
            return mReflectionHelper.invokeMirrorMethod(mirroredMethod, instance, method.getReturnType(), args);
        } catch (NoSuchMethodException | IllegalAccessException | UnwrappingException | WrappingException e) {
            throw new MirrorInvocationException(e);
        } catch (InvocationTargetException e) {
            tryMirrorThrowable(e.getCause(), method);
            throw e.getCause();
        }
    }

    private void tryMirrorThrowable(Throwable throwable, Method method) throws Throwable {
        if (method.isAnnotationPresent(WrapExceptions.class)) {
            WrapExceptions wrapExceptions = method.getAnnotation(WrapExceptions.class);
            for (WrapException wrapException : wrapExceptions.value()) {
                tryThrowWrappedException(wrapException, throwable);
            }
        }

        if (method.isAnnotationPresent(WrapException.class)) {
            WrapException wrapException = method.getAnnotation(WrapException.class);
            tryThrowWrappedException(wrapException, throwable);
        }

        if (throwable instanceof RuntimeException || throwable instanceof Error) {
            throw throwable;
        }

        for (Class<?> declaredException : method.getExceptionTypes()) {
            if (declaredException.isInstance(throwable)) {
                throw throwable;
            }
        }
    }

    private void tryThrowWrappedException(WrapException wrapException, Throwable throwable) throws Throwable {
        if (!wrapException.sourceType().isInstance(throwable)) {
            return;
        }

        throw  mThrowableWrapper.wrapThrowable(throwable, wrapException.destType());
    }
}
