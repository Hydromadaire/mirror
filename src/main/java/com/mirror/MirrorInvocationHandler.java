package com.mirror;

import com.mirror.helper.InvocationHelper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.WrappingException;

import java.lang.reflect.*;
import java.util.Optional;

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

    private void tryMirrorThrowable(Throwable throwable, Method mirrorMethod) throws Throwable {
        if (mirrorMethod.isAnnotationPresent(WrapExceptions.class)) {
            WrapExceptions wrapExceptions = mirrorMethod.getAnnotation(WrapExceptions.class);
            for (WrapException wrapException : wrapExceptions.value()) {
                tryThrowWrappedException(throwable, wrapException.sourceType(), wrapException.destType());
            }
        }

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
                Optional<Throwable> optionalThrowable = tryWrapExceptionWithThrowableConstructor(destType, throwable);
                if (!optionalThrowable.isPresent()) {
                    optionalThrowable = tryWrapExceptionWithDefaultConstructor(destType, throwable);
                }
                if (!optionalThrowable.isPresent()) {
                    MirrorInvocationException invocationException = new MirrorInvocationException("cannot wrap exception to type: " + destType.getName());
                    invocationException.addSuppressed(throwable);
                    throw invocationException;
                }

                throwable = optionalThrowable.get();
            } catch (ReflectiveOperationException e) {
                MirrorInvocationException invocationException = new MirrorInvocationException(e);
                invocationException.addSuppressed(throwable);
                throw invocationException;
            }

            throw throwable;
        }
    }

    private Optional<Throwable> tryWrapExceptionWithThrowableConstructor(Class<? extends Throwable> destType, Throwable throwable) throws ReflectiveOperationException {
        try {
            Constructor<? extends Throwable> constructor = destType.getConstructor(Throwable.class);

            if (!Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }

            return Optional.of(constructor.newInstance(throwable));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private Optional<Throwable> tryWrapExceptionWithDefaultConstructor(Class<? extends Throwable> destType, Throwable throwable) throws ReflectiveOperationException {
        try {
            Constructor<? extends Throwable> constructor = destType.getConstructor();

            if (!Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }

            Throwable wrapper = constructor.newInstance();
            wrapper.initCause(throwable);

            return Optional.of(wrapper);
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}
