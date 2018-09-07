package com.mirror;

import com.mirror.helper.ReflectionHelper;
import com.mirror.wrapping.ThrowableWrapper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MirrorInvocationHandlerTest {

    private MirrorInvocationHandler mMirrorInvocationHandler;
    private ReflectionHelper mReflectionHelper;
    private ThrowableWrapper mThrowableWrapper;
    private Object mTargetInstance;
    private Class<?> mTargetClass;

    @Before
    public void setUp() throws Exception {
        mReflectionHelper = mock(ReflectionHelper.class);
        mThrowableWrapper = new ThrowableWrapper();
        mTargetClass = TargetClass.class;
        mTargetInstance = mock(TargetClass.class);

        mMirrorInvocationHandler = new MirrorInvocationHandler(mReflectionHelper, mThrowableWrapper, mTargetClass, mTargetInstance);
    }

    @Test
    public void invoke_parameterlessVoidMethod_callsMethodReturnsNull() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicNoParam");

        when(mReflectionHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mReflectionHelper.invokeMirrorMethod(any(), any(), any(), any())).thenReturn(null);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, null);

        assertTrue(result == null);

        verify(mReflectionHelper, times(1)).findMirrorMethod(METHOD, METHOD.getName(), mTargetClass);
        verify(mReflectionHelper, times(1)).invokeMirrorMethod(METHOD, mTargetInstance, void.class);
    }

    @Test
    public void invoke_parameterVoidMethod_callsMethodReturnsNull() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicParam", Object.class);
        Object[] PARAMS = {new Object()};

        when(mReflectionHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mReflectionHelper.invokeMirrorMethod(any(), any(), any(), any())).thenReturn(null);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, PARAMS);

        assertTrue(result == null);

        verify(mReflectionHelper, times(1)).findMirrorMethod(METHOD, METHOD.getName(), mTargetClass);
        verify(mReflectionHelper, times(1)).invokeMirrorMethod(METHOD, mTargetInstance, void.class, PARAMS);
    }

    @Test
    public void invoke_parameterlessObjectMethod_callsMethodReturnsResult() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicReturnNoParam");
        Object RESULT = new Object();

        when(mReflectionHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mReflectionHelper.invokeMirrorMethod(any(), any(), any(), any())).thenReturn(RESULT);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, null);

        assertEquals(RESULT, result);

        verify(mReflectionHelper, times(1)).findMirrorMethod(METHOD, METHOD.getName(), mTargetClass);
        verify(mReflectionHelper, times(1)).invokeMirrorMethod(METHOD, mTargetInstance, Object.class);
    }

    @Test
    public void invoke_parameterObjectMethod_callsMethodReturnsResult() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicReturnParam", Object.class);
        Object RESULT = new Object();
        Object[] PARAMS = {new Object()};

        when(mReflectionHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mReflectionHelper.invokeMirrorMethod(any(), any(), any(), any())).thenReturn(RESULT);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, PARAMS);

        assertEquals(RESULT, result);

        verify(mReflectionHelper, times(1)).findMirrorMethod(METHOD, METHOD.getName(), mTargetClass);
        verify(mReflectionHelper, times(1)).invokeMirrorMethod(METHOD, mTargetInstance, Object.class, PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invoke_runtimeThrownFromMethod_propagatesExceptionCorrectly() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicReturnNoParam");

        when(mReflectionHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mReflectionHelper.invokeMirrorMethod(any(), any(), any(), any())).thenThrow(new InvocationTargetException(new IllegalArgumentException()));

        mMirrorInvocationHandler.invoke(null, METHOD, null);
    }

    @Test(expected = InterruptedException.class)
    public void invoke_declaredExceptionThrownFromMethod_propagatesExceptionCorrectly() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("publicExceptionDeclared");

        when(mReflectionHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mReflectionHelper.invokeMirrorMethod(any(), any(), any(), any())).thenThrow(new InvocationTargetException(new InterruptedException()));

        mMirrorInvocationHandler.invoke(null, METHOD, null);
    }

    @Test(expected = InterruptedException.class)
    public void invoke_wrappableExceptionThrownFromMethod_wrapExceptionCorrectly() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("wrapException");

        when(mReflectionHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mReflectionHelper.invokeMirrorMethod(any(), any(), any(), any())).thenThrow(new InvocationTargetException(new IllegalArgumentException()));

        mMirrorInvocationHandler.invoke(null, METHOD, null);
    }

    @Test(expected = InvocationTargetException.class)
    public void invoke_wrappableExceptionThrownFromMethodWithMultiWrappables_wrapExceptionCorrectly() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("wrapExceptions");

        when(mReflectionHelper.findMirrorMethod(any(), anyString(), any())).thenReturn(METHOD);
        when(mReflectionHelper.invokeMirrorMethod(any(), any(), any(), any())).thenThrow(new InvocationTargetException(new IllegalAccessException()));

        mMirrorInvocationHandler.invoke(null, METHOD, null);
    }

    @Test
    public void invoke_methodIsGetField_returnsFieldValue() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("getField");
        Object FIELD_VALUE = new Object();

        when(mReflectionHelper.findMirrorField(anyString(), any())).thenReturn(mock(Field.class));
        when(mReflectionHelper.getFieldValue(any(), any(), any())).thenReturn(FIELD_VALUE);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, null);
        assertEquals(FIELD_VALUE, result);
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

        @GetField("field")
        public Object getField() {
            return null;
        }
    }
}