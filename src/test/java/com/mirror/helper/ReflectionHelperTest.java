package com.mirror.helper;

import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.Wrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ReflectionHelperTest {

    private ReflectionHelper mReflectionHelper;
    private Wrapper mWrapper;
    private Unwrapper mUnwrapper;

    @Before
    public void setUp() throws Exception {
        mWrapper = mock(Wrapper.class);
        mUnwrapper = mock(Unwrapper.class);

        mReflectionHelper = new ReflectionHelper(mWrapper, mUnwrapper);
    }

    @Test
    public void unwrapParameters_objects_returnsUnwrappedParameters() throws Exception {
        Object[] PARAMETERS = {new Object(), new Object()};
        Object UNWRAPPED = new Object();

        when(mUnwrapper.unwrap(any())).thenReturn(UNWRAPPED);

        Object[] result = mReflectionHelper.unwrapParameters(PARAMETERS);

        assertArrayEquals(new Object[] {UNWRAPPED, UNWRAPPED}, result);
    }

    @Test
    public void unwrapParameterTypes_types_returnsUnwrappedParameterTypes() throws Exception {
        Class<?>[] PARAMETER_TYPES = new Class[] {this.getClass(), this.getClass()};
        Class<?> UNWRAPPED_TYPE = Object.class;

        Method mockMethod = mock(Method.class);
        when(mockMethod.getParameterTypes()).thenReturn(PARAMETER_TYPES);

        doReturn(UNWRAPPED_TYPE).when(mUnwrapper).unwrapType(any(Class.class));

        Class[] result = mReflectionHelper.unwrapParameterTypes(mockMethod);

        assertArrayEquals(new Class[] {UNWRAPPED_TYPE, UNWRAPPED_TYPE}, result);
    }

    @Test
    public void findMirrorMethod_privateMethod_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = SomeClass.class.getDeclaredMethod("privateNoParamMethod");

        mReflectionHelper = spy(mReflectionHelper);
        doReturn(RESULT_METHOD.getParameterTypes()).when(mReflectionHelper).unwrapParameterTypes(any(Method.class));

        Method method = mock(Method.class);

        Method result = mReflectionHelper.findMirrorMethod(method, RESULT_METHOD.getName(), SomeClass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void findMirrorMethod_publicMethod_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = SomeClass.class.getDeclaredMethod("publicNoParamMethod");

        mReflectionHelper = spy(mReflectionHelper);
        doReturn(RESULT_METHOD.getParameterTypes()).when(mReflectionHelper).unwrapParameterTypes(any(Method.class));

        Method method = mock(Method.class);

        Method result = mReflectionHelper.findMirrorMethod(method, RESULT_METHOD.getName(), SomeClass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void findMirrorMethod_methodWithOverloads_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = SomeClass.class.getDeclaredMethod("withOverload", int.class);

        mReflectionHelper = spy(mReflectionHelper);
        doReturn(RESULT_METHOD.getParameterTypes()).when(mReflectionHelper).unwrapParameterTypes(any(Method.class));

        Method method = mock(Method.class);

        Method result = mReflectionHelper.findMirrorMethod(method, RESULT_METHOD.getName(), SomeClass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void findMirrorMethod_sameParamsDifferentName_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = SomeClass.class.getDeclaredMethod("differentName", int.class);
        Method DIFFERENT_NAME = SomeClass.class.getDeclaredMethod("withOverload", int.class);

        doAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        }).when(mUnwrapper).unwrapType(any(Class.class));

        Method result = mReflectionHelper.findMirrorMethod(DIFFERENT_NAME, RESULT_METHOD.getName(), SomeClass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void findMirrorMethod_methodFromSuper_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = Subclass.class.getMethod("publicNoParamMethod");

        mReflectionHelper = spy(mReflectionHelper);
        doReturn(RESULT_METHOD.getParameterTypes()).when(mReflectionHelper).unwrapParameterTypes(any(Method.class));

        Method method = mock(Method.class);

        Method result = mReflectionHelper.findMirrorMethod(method, RESULT_METHOD.getName(), Subclass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void invokeMirrorMethod_correctParameters_correctInvocationDone() throws Exception {
        Object[] PARAMETERS = new Object[0];
        Object INSTANCE = new Object();
        Class<?> RETURN_TYPE = this.getClass();
        Object RETURN = new Object();

        mReflectionHelper = spy(mReflectionHelper);
        doReturn(PARAMETERS).when(mReflectionHelper).unwrapParameters(any());

        when(mWrapper.wrap(any(), any())).thenReturn(RETURN);

        Method mockMethod = mock(Method.class);
        when(mockMethod.invoke(any(), any())).thenReturn(RETURN);

        mReflectionHelper.invokeMirrorMethod(mockMethod, INSTANCE, RETURN_TYPE, PARAMETERS);

        verify(mockMethod, times(1)).setAccessible(true);
        verify(mockMethod, times(1)).invoke(INSTANCE, PARAMETERS);
        verify(mWrapper, times(1)).wrap(RETURN, RETURN_TYPE);
    }

    @Test
    public void findMirrorField_privateField_returnsField() throws Exception {
        Field RESULT_FIELD = SomeClass.class.getDeclaredField("field");

        Field result = mReflectionHelper.findMirrorField(RESULT_FIELD.getName(), SomeClass.class);

        assertEquals(RESULT_FIELD, result);
    }

    @Test(expected = NoSuchFieldException.class)
    public void findMirrorField_protectedFieldFromSuper_throwsNoSuchFieldException() throws Exception {
         mReflectionHelper.findMirrorField("superField", Subclass.class);
    }

    @Test
    public void findMirrorField_publicFieldFromSuper_returnsField() throws Exception {
        Field RESULT_FIELD = Subclass.class.getField("publicField");

        Field result = mReflectionHelper.findMirrorField(RESULT_FIELD.getName(), Subclass.class);

        assertEquals(RESULT_FIELD, result);
    }

    @Test
    public void getFieldValue_correctParameters_correctFieldAccessDone() throws Exception {
        Object INSTANCE = new Object();
        Class<?> RETURN_TYPE = this.getClass();
        Object RETURN = new Object();

        when(mWrapper.wrap(any(), any())).thenReturn(RETURN);

        Field mockField = mock(Field.class);
        when(mockField.get(any())).thenReturn(RETURN);

        mReflectionHelper.getFieldValue(mockField, INSTANCE, RETURN_TYPE);

        verify(mockField, times(1)).setAccessible(true);
        verify(mockField, times(1)).get(INSTANCE);
        verify(mWrapper, times(1)).wrap(RETURN, RETURN_TYPE);
    }

    @Test
    public void setFieldValue_correctParameters_correctFieldAccessDone() throws Exception {
        Object INSTANCE = new Object();
        Object VALUE = new Object();

        when(mUnwrapper.unwrap(any())).thenReturn(VALUE);

        Field mockField = mock(Field.class);

        mReflectionHelper.setFieldValue(mockField, INSTANCE, VALUE);

        verify(mockField, times(1)).setAccessible(true);
        verify(mockField, times(1)).set(INSTANCE, VALUE);
        verify(mUnwrapper, times(1)).unwrap(VALUE);
    }

    private static class SomeClass {

        private Object field;
        protected Object superField;
        public Object publicField;

        private void privateNoParamMethod() {

        }

        public void publicNoParamMethod() {

        }

        public void withOverload(int a) {

        }

        public void withOverload(int a, double d) {

        }

        public void differentName(int a) {

        }
    }

    private static class Subclass extends SomeClass {

    }
}