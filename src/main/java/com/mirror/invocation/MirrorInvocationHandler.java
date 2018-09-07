package com.mirror.invocation;

import com.mirror.GetField;
import com.mirror.SetField;
import com.mirror.helper.ReflectionHelper;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.WrappingException;

import java.lang.reflect.*;
import java.util.Optional;

public class MirrorInvocationHandler implements InvocationHandler {

    private final ReflectionHelper mReflectionHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final Class<?> mTargetClass;
    private final Object mTargetInstance;
    private final ClassLoader mClassLoader;

    public MirrorInvocationHandler(ReflectionHelper reflectionHelper, ThrowableWrapper throwableWrapper, Class<?> targetClass, Object targetInstance, ClassLoader classLoader) {
        mReflectionHelper = reflectionHelper;
        mThrowableWrapper = throwableWrapper;
        mTargetClass = targetClass;
        mTargetInstance = targetInstance;
        mClassLoader = classLoader;
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

        if (method.isAnnotationPresent(SetField.class)) {
            if (args.length != 1) {
                throw new MirrorFieldAccessException("Cannot set field with no argument");
            }

            SetField setField = method.getAnnotation(SetField.class);
            setFieldValue(setField.value(), args[0]);
            return null;
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

    private void setFieldValue(String fieldName, Object value) {
        try {
            Field field = mReflectionHelper.findMirrorField(fieldName, mTargetClass);

            Object instance = Modifier.isStatic(field.getModifiers()) ? null : mTargetInstance;
            mReflectionHelper.setFieldValue(field, instance, value);
        } catch (ReflectiveOperationException | UnwrappingException e) {
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
            Optional<Throwable> optionalThrowable = mThrowableWrapper.tryWrapThrowable(e.getCause(), method.getExceptionTypes(), mClassLoader);
            if (optionalThrowable.isPresent()) {
                throw optionalThrowable.get();
            }

            throw e.getCause();
        }
    }
}
