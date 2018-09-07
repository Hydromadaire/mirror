package com.mirror;

import com.mirror.helper.InvocationHelper;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.WrappingException;

import java.lang.reflect.*;
import java.util.Optional;

public class MirrorInvocationHandler implements InvocationHandler {

    private final InvocationHelper mInvocationHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final Class<?> mTargetClass;
    private final Object mTargetInstance;

    public MirrorInvocationHandler(InvocationHelper invocationHelper, ThrowableWrapper throwableWrapper, Class<?> targetClass, Object targetInstance) {
        mInvocationHelper = invocationHelper;
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

        return invokeMethod(method, args);
    }

    public Object invokeMethod(Method method, Object[] args) throws Throwable {
        try {
            String mirroredMethodName = method.getName();
            Method mirroredMethod = mInvocationHelper.findMirrorMethod(method, mirroredMethodName, mTargetClass);

            Object instance = Modifier.isStatic(mirroredMethod.getModifiers()) ? null : mTargetInstance;
            return mInvocationHelper.invokeMirrorMethod(mirroredMethod, instance, method.getReturnType(), args);
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
        Optional<Throwable> optionalThrowable = mThrowableWrapper.tryWrapThrowable(throwable, wrapException.sourceType(), wrapException.destType());
        if (optionalThrowable.isPresent()) {
            throw optionalThrowable.get();
        }
    }
}
