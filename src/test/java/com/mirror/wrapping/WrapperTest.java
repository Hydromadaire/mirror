package com.mirror.wrapping;

import com.mirror.Mirror;
import com.mirror.MirrorCreator;
import com.mirror.helper.MirrorHelper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WrapperTest {

    private Wrapper mWrapper;
    private MirrorHelper mMirrorHelper;
    private MirrorCreator mMirrorCreator;

    @Before
    public void setUp() throws Exception {
        mMirrorCreator = mock(MirrorCreator.class);
        mMirrorHelper = mock(MirrorHelper.class);

        mWrapper = new Wrapper(mMirrorHelper, mMirrorCreator);
    }

    @Test
    public void wrapObject_mirrorOfObject_createsAppropriateMirror() throws Exception {
        Object SOURCE_OBJECT = new Object();
        Object CREATED_OBJECT = new Object();
        Class<?> TARGET_MIRROR_CLASS = this.getClass();

        when(mMirrorHelper.isMirror(any(Class.class))).thenReturn(true);

        Mirror mockMirror = mock(Mirror.class);
        when(mMirrorCreator.createMirror(any(Class.class))).thenReturn(mockMirror);

        when(mockMirror.create(any())).thenReturn(CREATED_OBJECT);

        Object result = mWrapper.wrapObject(SOURCE_OBJECT, TARGET_MIRROR_CLASS);
        assertEquals(CREATED_OBJECT, result);

        verify(mMirrorHelper, times(1)).isMirror(TARGET_MIRROR_CLASS);
        verify(mMirrorCreator, times(1)).createMirror(TARGET_MIRROR_CLASS);
        verify(mockMirror, times(1)).create(SOURCE_OBJECT);
    }

    @Test
    public void wrapObject_notMirrorOfObject_returnOriginalObject() throws Exception {
        Object SOURCE_OBJECT = new Object();
        Class<?> TARGET_MIRROR_CLASS = this.getClass();

        when(mMirrorHelper.isMirror(any(Class.class))).thenReturn(false);

        Object result = mWrapper.wrapObject(SOURCE_OBJECT, TARGET_MIRROR_CLASS);
        assertEquals(SOURCE_OBJECT, result);

        verify(mMirrorHelper, times(1)).isMirror(TARGET_MIRROR_CLASS);
    }

    @Test
    public void wrapArray_mirrorOfArrayElements_wrapsEachElement() throws Exception {
        Object[] ARRAY = {new Object(), new Object()};
        Object CREATED_OBJECT = new Object();
        Class<?> TARGET_MIRROR_CLASS = this.getClass();

        when(mMirrorHelper.isMirror(any(Class.class))).thenReturn(true);

        Mirror mockMirror = mock(Mirror.class);
        when(mMirrorCreator.createMirror(any(Class.class))).thenReturn(mockMirror);

        when(mockMirror.create(any())).thenReturn(CREATED_OBJECT);

        Object result = mWrapper.wrapArray(ARRAY, TARGET_MIRROR_CLASS);
        Object[] arrResult = (Object[]) result;

        assertThat(arrResult, arrayWithSize(ARRAY.length));
        assertThat(arrResult, hasItemInArray(CREATED_OBJECT));
    }

    @Test
    public void wrapArray_arrayOfPrimitives_returnsSameArray() throws Exception {
        int[] array = {5, 6, 7};

        Object result = mWrapper.wrapArray(array, this.getClass());

        assertEquals(array, result);
    }

    @Test
    public void wrap_isObjectNull_returnNull() throws Exception {
        Object result = mWrapper.wrap(null, this.getClass());
        assertThat(result, is(nullValue()));
    }

    @Test
    public void wrap_objectParameter_callsWrapObject() throws Exception {
        Object SOURCE_OBJECT = new Object();
        Class<?> TARGET_TYPE = this.getClass();

        mWrapper = spy(mWrapper);
        doReturn(new Object()).when(mWrapper).wrapObject(any(), any(Class.class));
        doReturn(new Object()).when(mWrapper).wrapArray(any(), any(Class.class));

        mWrapper.wrap(SOURCE_OBJECT, TARGET_TYPE);

        verify(mWrapper, times(1)).wrapObject(SOURCE_OBJECT, TARGET_TYPE);
    }

    @Test
    public void wrap_arrayParameter_callsWrapArray() throws Exception {
        Object[] SOURCE_OBJECT = {new Object()};
        Class<?> TARGET_TYPE = int[].class;

        mWrapper = spy(mWrapper);
        doReturn(new Object()).when(mWrapper).wrapArray(any(), any(Class.class));

        mWrapper.wrap(SOURCE_OBJECT, TARGET_TYPE);

        verify(mWrapper, times(1)).wrapArray(SOURCE_OBJECT, TARGET_TYPE.getComponentType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrap_arrayParameterNonArrayType_callsWrapArray() throws Exception {
        Object[] SOURCE_OBJECT = {new Object()};
        Class<?> TARGET_TYPE = int.class;

        mWrapper = spy(mWrapper);
        doReturn(new Object()).when(mWrapper).wrapObject(any(), any(Class.class));
        doReturn(new Object()).when(mWrapper).wrapArray(any(), any(Class.class));

        mWrapper.wrap(SOURCE_OBJECT, TARGET_TYPE);
    }
}