package com.mirror.wrapping;

import com.mirror.WrapException;
import com.mirror.WrapExceptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class ThrowableWrapper {

    public void tryMirrorThrowable(Throwable throwable, Method method) throws Throwable {
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

        throw wrapThrowable(throwable, wrapException.destType());
    }

    public Throwable wrapThrowable(Throwable throwable, Class<? extends Throwable> destType) {
        try {
            Optional<Throwable> optionalThrowable = tryWrapThrowableWithCauseConstructor(destType, throwable);
            if (optionalThrowable.isPresent()) {
                return optionalThrowable.get();
            }

            optionalThrowable = tryWrapThrowableWithDefaultConstructor(destType, throwable);
            if (optionalThrowable.isPresent()) {
                return optionalThrowable.get();
            }

            throw initWrappingException(new ThrowableWrappingException("cannot wrap exception to type: " + destType.getName()), throwable);
        } catch (ReflectiveOperationException e) {
            throw initWrappingException(new ThrowableWrappingException(e), throwable);
        }
    }

    private Optional<Throwable> tryWrapThrowableWithCauseConstructor(Class<? extends Throwable> destType, Throwable throwable) throws ReflectiveOperationException {
        try {
            Constructor<? extends Throwable> constructor = destType.getDeclaredConstructor(Throwable.class);

            if (!Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
            }

            return Optional.of(constructor.newInstance(throwable));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private Optional<Throwable> tryWrapThrowableWithDefaultConstructor(Class<? extends Throwable> destType, Throwable throwable) throws ReflectiveOperationException {
        try {
            Constructor<? extends Throwable> constructor = destType.getDeclaredConstructor();

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

    private ThrowableWrappingException initWrappingException(ThrowableWrappingException wrappingException, Throwable originalThrowable) {
        wrappingException.addSuppressed(originalThrowable);
        return wrappingException;
    }
}
