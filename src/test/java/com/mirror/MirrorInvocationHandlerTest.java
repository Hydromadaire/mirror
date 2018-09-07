package com.mirror;

import com.mirror.helper.InvocationHelper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MirrorInvocationHandlerTest {

    private MirrorInvocationHandler mMirrorInvocationHandler;
    private InvocationHelper mInvocationHelper;
    private Object mTargetInstance;
    private Class<?> mTargetClass;

    @Before
    public void setUp() throws Exception {
        mInvocationHelper = mock(InvocationHelper.class);
        mTargetClass = TargetClass.class;
        mTargetInstance = mock(TargetClass.class);

        mMirrorInvocationHandler = new MirrorInvocationHandler(mInvocationHelper, mTargetClass, mTargetInstance);
    }

    @Test
    public void invoke_parameterlessVoidMethod_callsMethodReturnsNull() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicNoParam");

        when(mInvocationHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mInvocationHelper.invokeMirrorMethod(any(), any(), any(), any())).thenReturn(null);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, null);

        assertTrue(result == null);

        verify(mInvocationHelper, times(1)).findMirrorMethod(METHOD, METHOD.getName(), mTargetClass);
        verify(mInvocationHelper, times(1)).invokeMirrorMethod(METHOD, mTargetInstance, void.class);
    }

    @Test
    public void invoke_parameterVoidMethod_callsMethodReturnsNull() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicParam", Object.class);
        Object[] PARAMS = {new Object()};

        when(mInvocationHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mInvocationHelper.invokeMirrorMethod(any(), any(), any(), any())).thenReturn(null);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, PARAMS);

        assertTrue(result == null);

        verify(mInvocationHelper, times(1)).findMirrorMethod(METHOD, METHOD.getName(), mTargetClass);
        verify(mInvocationHelper, times(1)).invokeMirrorMethod(METHOD, mTargetInstance, void.class, PARAMS);
    }

    @Test
    public void invoke_parameterlessObjectMethod_callsMethodReturnsResult() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicReturnNoParam");
        Object RESULT = new Object();

        when(mInvocationHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mInvocationHelper.invokeMirrorMethod(any(), any(), any(), any())).thenReturn(RESULT);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, null);

        assertEquals(RESULT, result);

        verify(mInvocationHelper, times(1)).findMirrorMethod(METHOD, METHOD.getName(), mTargetClass);
        verify(mInvocationHelper, times(1)).invokeMirrorMethod(METHOD, mTargetInstance, Object.class);
    }

    @Test
    public void invoke_parameterObjectMethod_callsMethodReturnsResult() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicReturnParam", Object.class);
        Object RESULT = new Object();
        Object[] PARAMS = {new Object()};

        when(mInvocationHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mInvocationHelper.invokeMirrorMethod(any(), any(), any(), any())).thenReturn(RESULT);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, PARAMS);

        assertEquals(RESULT, result);

        verify(mInvocationHelper, times(1)).findMirrorMethod(METHOD, METHOD.getName(), mTargetClass);
        verify(mInvocationHelper, times(1)).invokeMirrorMethod(METHOD, mTargetInstance, Object.class, PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invoke_runtimeThrownFromMethod_propagatesExceptionCorrectly() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicReturnNoParam");

        when(mInvocationHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mInvocationHelper.invokeMirrorMethod(any(), any(), any(), any())).thenThrow(new InvocationTargetException(new IllegalArgumentException()));

        mMirrorInvocationHandler.invoke(null, METHOD, null);
    }

    @Test(expected = InterruptedException.class)
    public void invoke_declaredExceptionThrownFromMethod_propagatesExceptionCorrectly() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicExceptionDeclared");

        when(mInvocationHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mInvocationHelper.invokeMirrorMethod(any(), any(), any(), any())).thenThrow(new InvocationTargetException(new InterruptedException()));

        mMirrorInvocationHandler.invoke(null, METHOD, null);
    }

    @Test(expected = InterruptedException.class)
    public void invoke_wrappableExceptionThrownFromMethod_wrapExceptionCorrectly() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("wrapException");

        when(mInvocationHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mInvocationHelper.invokeMirrorMethod(any(), any(), any(), any())).thenThrow(new InvocationTargetException(new IllegalArgumentException()));

        mMirrorInvocationHandler.invoke(null, METHOD, null);
    }

    @Test(expected = InvocationTargetException.class)
    public void invoke_wrappableExceptionThrownFromMethodWithMultiWrappables_wrapExceptionCorrectly() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("wrapExceptions");

        when(mInvocationHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mInvocationHelper.invokeMirrorMethod(any(), any(), any(), any())).thenThrow(new InvocationTargetException(new IllegalAccessException()));

        mMirrorInvocationHandler.invoke(null, METHOD, null);
    }

    private static class TargetClass {

        public void publicNoParam() {

        }

        public void publicParam(Object param) {

        }

        public Object publicReturnNoParam() {
            return null;
        }

        public Object publicReturnParam(Object param) {
            return null;
        }

        public Object publicExceptionDeclared() throws InterruptedException {
            return null;
        }

        @WrapException(sourceType = IllegalArgumentException.class, destType = InterruptedException.class)
        public void wrapException() {

        }

        @WrapExceptions({
                @WrapException(sourceType = IllegalArgumentException.class, destType = InterruptedException.class),
                @WrapException(sourceType = IllegalAccessException.class, destType = InvocationTargetException.class)
        })
        public void wrapExceptions() {

        }
    }
}