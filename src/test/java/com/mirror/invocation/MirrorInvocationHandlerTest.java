package com.mirror.invocation;

import com.mirror.GetField;
import com.mirror.SetField;
import com.mirror.helper.ReflectionHelper;
import com.mirror.wrapping.ThrowableWrapper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
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
        ClassLoader classLoader = this.getClass().getClassLoader();

        mMirrorInvocationHandler = new MirrorInvocationHandler(mReflectionHelper, mThrowableWrapper, mTargetClass, mTargetInstance, classLoader);
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

    @Test
    public void invoke_methodIsGetField_returnsFieldValue() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("getField");
        Object FIELD_VALUE = new Object();

        when(mReflectionHelper.findMirrorField(anyString(), any())).thenReturn(mock(Field.class));
        when(mReflectionHelper.getFieldValue(any(), any(), any())).thenReturn(FIELD_VALUE);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, null);
        assertEquals(FIELD_VALUE, result);
    }

    @Test
    public void invoke_methodIsSetField_setFieldValue() throws Throwable {
        Method METHOD = TargetClass.class.getDeclaredMethod("setField", Object.class);
        Object FIELD_VALUE = new Object();

        Field mockField = mock(Field.class);

        when(mReflectionHelper.findMirrorField(anyString(), any())).thenReturn(mockField);
        when(mReflectionHelper.getFieldValue(any(), any(), any())).thenReturn(FIELD_VALUE);

        Object result = mMirrorInvocationHandler.invoke(null, METHOD, new Object[] {FIELD_VALUE});
        assertNull(result);

        verify(mReflectionHelper, times(1)).setFieldValue(eq(mockField), any(), eq(FIELD_VALUE));
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

        @GetField("field")
        public Object getField() {
            return null;
        }

        @SetField("field")
        public void setField(Object value) {

        }
    }
}