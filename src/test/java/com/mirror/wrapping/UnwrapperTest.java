package com.mirror.wrapping;

import com.mirror.invocation.MirrorInvocationHandler;
import com.mirror.helper.MirrorHelper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UnwrapperTest {

    private Unwrapper mUnwrapper;
    private ClassLoader mClassLoader;
    private MirrorHelper mMirrorHelper;

    @Before
    public void setUp() throws Exception {
        mClassLoader = this.getClass().getClassLoader();
        mMirrorHelper = mock(MirrorHelper.class);

        mUnwrapper = new Unwrapper(mMirrorHelper, mClassLoader);
    }

    @Test
    public void unwrapObject_proxyMirror_returnsInnerInstance() throws Exception {
        Object INNER_INSTANCE = new Object();
        MirrorInvocationHandler invocationHandler = mock(MirrorInvocationHandler.class);
        when(invocationHandler.getTargetInstance()).thenReturn(INNER_INSTANCE);

        Object proxy = createProxy(invocationHandler);

        Object result = mUnwrapper.unwrapObject(proxy);

        assertEquals(INNER_INSTANCE, result);
    }

    @Test
    public void unwrapObject_proxyNonMirror_returnsOriginalObject() throws Exception {
        InvocationHandler invocationHandler = new SomeInvocationHandler();

        Object proxy = createProxy(invocationHandler);

        Object result = mUnwrapper.unwrapObject(proxy);

        assertTrue(proxy == result);
    }

    @Test
    public void unwrapObject_nonProxy_returnsOriginalObject() throws Exception {
        Object OBJECT = new Object();

        Object result = mUnwrapper.unwrapObject(OBJECT);

        assertEquals(OBJECT, result);
    }

    @Test
    public void unwrapArray_arrayOfPrimitives_returnsSameArray() throws Exception {
        int[] ARRAY = {4, 6, 7};

        Object result = mUnwrapper.unwrapArray(ARRAY);

        assertEquals(ARRAY, result);
    }

    @Test
    public void unwrapArray_arrayOfOrdinaryObjects_sameObjectsReturned() throws Exception {
        Object[] ARRAY = {new Object(), new Object()};

        Object result = mUnwrapper.unwrapArray(ARRAY);
        Object[] arrayResult = (Object[]) result;

        assertArrayEquals(ARRAY, arrayResult);
    }

    @Test
    public void unwrapArray_arrayOfMirrors_targetObjectsReturned() throws Exception {
        Object TARGET_OBJECT = new Object();

        MirrorInvocationHandler mirrorInvocationHandler = mock(MirrorInvocationHandler.class);
        when(mirrorInvocationHandler.getTargetInstance()).thenReturn(TARGET_OBJECT);

        Object[] ARRAY = {createProxy(mirrorInvocationHandler), createProxy(mirrorInvocationHandler)};

        Object result = mUnwrapper.unwrapArray(ARRAY);
        Object[] arrayResult = (Object[]) result;

        assertArrayEquals(arrayResult, new Object[] {TARGET_OBJECT, TARGET_OBJECT});
    }

    @Test
    public void unwrap_object_callsUnwrapObject() throws Exception {
        Object OBJECT = new Object();

        mUnwrapper = spy(mUnwrapper);
        doReturn(new Object()).when(mUnwrapper).unwrapObject(any());
        doReturn(new Object()).when(mUnwrapper).unwrapArray(any());

        mUnwrapper.unwrap(OBJECT);

        verify(mUnwrapper, times(1)).unwrapObject(OBJECT);
    }

    @Test
    public void unwrap_array_callsUnwrapArray() throws Exception {
        Object OBJECT = new Object[1];

        mUnwrapper = spy(mUnwrapper);
        doReturn(new Object()).when(mUnwrapper).unwrapObject(any());
        doReturn(new Object()).when(mUnwrapper).unwrapArray(any());

        mUnwrapper.unwrap(OBJECT);

        verify(mUnwrapper, times(1)).unwrapArray(OBJECT);
    }

    @Test
    public void unwrap_null_returnsNull() throws Exception {
        Object result = mUnwrapper.unwrap(null);

        assertTrue(result == null);
    }

    @Test
    public void unwrapObjectType_mirrorType_returnsTypeOfMirror() throws Exception {
        when(mMirrorHelper.isMirror(any(Class.class))).thenReturn(true);
        when(mMirrorHelper.getMirroredTypeName(any(Class.class))).thenReturn(ClassToBeMirrored.class.getName());

        Class<?> result = mUnwrapper.unwrapObjectType(this.getClass());

        assertEquals(result, ClassToBeMirrored.class);
    }

    @Test
    public void unwrapObjectType_nonMirrorType_returnsSameObject() throws Exception {
        Class<?> TYPE = this.getClass();

        when(mMirrorHelper.isMirror(any(Class.class))).thenReturn(false);

        Object result = mUnwrapper.unwrapObjectType(TYPE);

        assertEquals(TYPE, result);
    }

    @Test
    public void unwrapArrayType_mirrorArrayType_returnsUnwrappedArrayType() throws Exception {
        when(mMirrorHelper.isMirror(any(Class.class))).thenReturn(true);
        when(mMirrorHelper.getMirroredTypeName(any(Class.class))).thenReturn(ClassToBeMirrored.class.getName());

        Class<?> ARRAY_TYPE = Object[].class;

        Class<?> result = mUnwrapper.unwrapArrayType(ARRAY_TYPE);

        assertEquals(ClassToBeMirrored[].class, result);
    }

    @Test
    public void unwrapArrayType_nonMirrorArrayType_returnsSameType() throws Exception {
        when(mMirrorHelper.isMirror(any(Class.class))).thenReturn(false);

        Class<?> ARRAY_TYPE = Object[].class;

        Class<?> result = mUnwrapper.unwrapArrayType(ARRAY_TYPE);

        assertEquals(ARRAY_TYPE, result);
    }

    @Test
    public void unwrapType_object_callsUnwrapObjectType() throws Exception {
        Class<?> TYPE = Object.class;

        mUnwrapper = spy(mUnwrapper);
        doReturn(this.getClass()).when(mUnwrapper).unwrapObjectType(any(Class.class));
        doReturn(this.getClass()).when(mUnwrapper).unwrapArrayType(any(Class.class));

        mUnwrapper.unwrapType(TYPE);

        verify(mUnwrapper, times(1)).unwrapObjectType(TYPE);
    }

    @Test
    public void unwrapType_array_callsUnwrapArrayType() throws Exception {
        Class<?> TYPE = Object[].class;

        mUnwrapper = spy(mUnwrapper);
        doReturn(this.getClass()).when(mUnwrapper).unwrapObjectType(any(Class.class));
        doReturn(this.getClass()).when(mUnwrapper).unwrapArrayType(any(Class.class));

        mUnwrapper.unwrapType(TYPE);

        verify(mUnwrapper, times(1)).unwrapArrayType(TYPE);
    }

    private Object createProxy(InvocationHandler invocationHandler) {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {SomeInterface.class}, invocationHandler);
    }

    private static interface SomeInterface {

    }

    private static class SomeInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    private static class ClassToBeMirrored {

    }
}