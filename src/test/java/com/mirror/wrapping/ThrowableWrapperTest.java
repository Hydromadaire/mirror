package com.mirror.wrapping;

import org.junit.Test;

import static org.junit.Assert.*;

public class ThrowableWrapperTest {

    @Test
    public void wrapException_destTypeHasPublicCauseConstructor_returnsWrappedException() throws Throwable {
        ThrowableWrapper throwableWrapper = new ThrowableWrapper();
        testWrap(throwableWrapper, new RuntimeException(), PublicCauseConstructor.class);
    }

    @Test
    public void wrapException_destTypeHasProtectedCauseConstructor_returnsWrappedException() throws Throwable {
        ThrowableWrapper throwableWrapper = new ThrowableWrapper();
        testWrap(throwableWrapper, new RuntimeException(), ProtectedCauseConstructor.class);
    }

    @Test
    public void wrapException_destTypeHasPrivateCauseConstructor_returnsWrappedException() throws Throwable {
        ThrowableWrapper throwableWrapper = new ThrowableWrapper();
        testWrap(throwableWrapper, new RuntimeException(), PrivateCauseConstructor.class);
    }

    @Test
    public void wrapException_destTypeHasPublicDefaultConstructor_returnsWrappedException() throws Throwable {
        ThrowableWrapper throwableWrapper = new ThrowableWrapper();
        testWrap(throwableWrapper, new RuntimeException(), PublicDefaultConstructor.class);
    }

    @Test
    public void wrapException_destTypeHasProtectedDefaultConstructor_returnsWrappedException() throws Throwable {
        ThrowableWrapper throwableWrapper = new ThrowableWrapper();
        testWrap(throwableWrapper, new RuntimeException(), ProtectedDefaultConstructor.class);
    }

    @Test
    public void wrapException_destTypeHasPrivateDefaultConstructor_returnsWrappedException() throws Throwable {
        ThrowableWrapper throwableWrapper = new ThrowableWrapper();
        testWrap(throwableWrapper, new RuntimeException(), PrivateDefaultConstructor.class);
    }

    @Test(expected = ThrowableWrappingException.class)
    public void wrapException_destTypeNoUsableConstructor_throwsThrowableWrappingException() throws Throwable {
        ThrowableWrapper throwableWrapper = new ThrowableWrapper();

        throwableWrapper.wrapThrowable(new RuntimeException(), UnusableThrowableType.class);
    }

    private void testWrap(ThrowableWrapper throwableWrapper, Throwable throwable, Class<? extends Throwable> destType) {
        Throwable result = throwableWrapper.wrapThrowable(throwable, destType);
        assertTrue(destType.isInstance(result));
    }

    private static class PublicCauseConstructor extends Throwable {
        public PublicCauseConstructor(Throwable throwable) {
            super(throwable);
        }
    }

    private static class ProtectedCauseConstructor extends Throwable {
        protected ProtectedCauseConstructor(Throwable throwable) {
            super(throwable);
        }
    }

    private static class PrivateCauseConstructor extends Throwable {
        private PrivateCauseConstructor(Throwable throwable) {
            super(throwable);
        }
    }

    private static class PublicDefaultConstructor extends Throwable {
        public PublicDefaultConstructor() {

        }
    }

    private static class ProtectedDefaultConstructor extends Throwable {
        protected ProtectedDefaultConstructor() {

        }
    }

    private static class PrivateDefaultConstructor extends Throwable {
        private PrivateDefaultConstructor() {

        }
    }

    private static class UnusableThrowableType extends Throwable {
        public UnusableThrowableType(String message) {

        }
    }
}