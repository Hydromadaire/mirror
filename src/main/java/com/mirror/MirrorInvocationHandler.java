package com.mirror;

import com.mirror.helper.InvocationHelper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.WrappingException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MirrorInvocationHandler implements InvocationHandler {

    private final InvocationHelper mInvocationHelper;
    private final Class<?> mTargetClass;
    private final Object mTargetInstance;

    public MirrorInvocationHandler(InvocationHelper invocationHelper, Class<?> targetClass, Object targetInstance) {
        mInvocationHelper = invocationHelper;
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
        Method mirroredMethod = null;

        try {
            String mirroredMethodName = method.getName();
            mirroredMethod = mInvocationHelper.findMirrorMethod(method, mirroredMethodName, mTargetClass);

            Object instance = Modifier.isStatic(mirroredMethod.getModifiers()) ? null : mTargetInstance;
            return mInvocationHelper.invokeMirrorMethod(mirroredMethod, instance, method.getReturnType(), args);
        } catch (NoSuchMethodException | IllegalAccessException | UnwrappingException | WrappingException e) {
            throw new MirrorInvocationException(e);
        } catch (InvocationTargetException e) {
            tryMirrorThrowable(e.getCause(), mirroredMethod);
            throw e.getCause();
        }
    }

    private void tryMirrorThrowable(Throwable throwable, Method mirrorMethod) throws Throwable {
        if (mirrorMethod.isAnnotationPresent(WrapException.class)) {
            WrapException wrapException = mirrorMethod.getAnnotation(WrapException.class);
            tryThrowWrappedException(throwable, wrapException.sourceType(), wrapException.destType());
        }

        if (throwable instanceof RuntimeException || throwable instanceof Error) {
            throw throwable;
        }

        for (Class<?> declaredException : mirrorMethod.getExceptionTypes()) {
            if (declaredException.isInstance(throwable)) {
                throw throwable;
            }
        }
    }

    private void tryThrowWrappedException(Throwable throwable, Class<? extends Throwable> sourceType, Class<? extends Throwable> destType) throws Throwable {
        if (sourceType.isInstance(throwable)) {
            try {
                Throwable wrapThrowable = destType.newInstance();
                wrapThrowable.initCause(throwable);
                throwable = wrapThrowable;
            } catch (ReflectiveOperationException e) {
                MirrorInvocationException invocationException = new MirrorInvocationException(e);
                invocationException.addSuppressed(throwable);
                throw invocationException;
            }

            throw throwable;
        }
    }
}
