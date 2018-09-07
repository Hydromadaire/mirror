package com.mirror.wrapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class ThrowableWrapper {

    public Optional<Throwable> tryWrapThrowable(Throwable throwable, Class<? extends Throwable> sourceType, Class<? extends Throwable> destType) throws Throwable {
        if (sourceType.isInstance(throwable)) {
            try {
                Optional<Throwable> optionalThrowable = tryWrapThrowableWithCauseConstructor(destType, throwable);
                if (optionalThrowable.isPresent()) {
                    return optionalThrowable;
                }

                optionalThrowable = tryWrapThrowableWithDefaultConstructor(destType, throwable);
                if (optionalThrowable.isPresent()) {
                    return optionalThrowable;
                }

                throw initWrappingException(new ThrowableWrappingException("cannot wrap exception to type: " + destType.getName()), throwable);
            } catch (ReflectiveOperationException e) {
                throw initWrappingException(new ThrowableWrappingException(e), throwable);
            }
        }

        return Optional.empty();
    }

    private Optional<Throwable> tryWrapThrowableWithCauseConstructor(Class<? extends Throwable> destType, Throwable throwable) throws ReflectiveOperationException {
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

    private Optional<Throwable> tryWrapThrowableWithDefaultConstructor(Class<? extends Throwable> destType, Throwable throwable) throws ReflectiveOperationException {
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

    private ThrowableWrappingException initWrappingException(ThrowableWrappingException wrappingException, Throwable originalThrowable) {
        wrappingException.addSuppressed(originalThrowable);
        return wrappingException;
    }
}
