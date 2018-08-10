package com.mirror.helper;

import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.Wrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class InvocationHelperTest {

    private InvocationHelper mInvocationHelper;
    private Wrapper mWrapper;
    private Unwrapper mUnwrapper;

    @Before
    public void setUp() throws Exception {
        mWrapper = mock(Wrapper.class);
        mUnwrapper = mock(Unwrapper.class);

        mInvocationHelper = new InvocationHelper(mWrapper, mUnwrapper);
    }

    @Test
    public void unwrapParameters_objects_returnsUnwrappedParameters() throws Exception {
        Object[] PARAMETERS = {new Object(), new Object()};
        Object UNWRAPPED = new Object();

        when(mUnwrapper.unwrap(any())).thenReturn(UNWRAPPED);

        Object[] result = mInvocationHelper.unwrapParameters(PARAMETERS);

        assertArrayEquals(new Object[] {UNWRAPPED, UNWRAPPED}, result);
    }

    @Test
    public void unwrapParameterTypes_types_returnsUnwrappedParameterTypes() throws Exception {
        Class<?>[] PARAMETER_TYPES = new Class[] {this.getClass(), this.getClass()};
        Class<?> UNWRAPPED_TYPE = Object.class;

        Method mockMethod = mock(Method.class);
        when(mockMethod.getParameterTypes()).thenReturn(PARAMETER_TYPES);

        doReturn(UNWRAPPED_TYPE).when(mUnwrapper).unwrapType(any(Class.class));

        Class[] result = mInvocationHelper.unwrapParameterTypes(mockMethod);

        assertArrayEquals(new Class[] {UNWRAPPED_TYPE, UNWRAPPED_TYPE}, result);
    }

    @Test
    public void findMirrorMethod_privateMethod_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = SomeClass.class.getDeclaredMethod("privateNoParamMethod");

        mInvocationHelper = spy(mInvocationHelper);
        doReturn(RESULT_METHOD.getParameterTypes()).when(mInvocationHelper).unwrapParameterTypes(any(Method.class));

        Method method = mock(Method.class);

        Method result = mInvocationHelper.findMirrorMethod(method, RESULT_METHOD.getName(), SomeClass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void findMirrorMethod_publicMethod_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = SomeClass.class.getDeclaredMethod("publicNoParamMethod");

        mInvocationHelper = spy(mInvocationHelper);
        doReturn(RESULT_METHOD.getParameterTypes()).when(mInvocationHelper).unwrapParameterTypes(any(Method.class));

        Method method = mock(Method.class);

        Method result = mInvocationHelper.findMirrorMethod(method, RESULT_METHOD.getName(), SomeClass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void findMirrorMethod_methodWithOverloads_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = SomeClass.class.getDeclaredMethod("withOverload", int.class);

        mInvocationHelper = spy(mInvocationHelper);
        doReturn(RESULT_METHOD.getParameterTypes()).when(mInvocationHelper).unwrapParameterTypes(any(Method.class));

        Method method = mock(Method.class);

        Method result = mInvocationHelper.findMirrorMethod(method, RESULT_METHOD.getName(), SomeClass.class);

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

        Method result = mInvocationHelper.findMirrorMethod(DIFFERENT_NAME, RESULT_METHOD.getName(), SomeClass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void findMirrorMethod_methodFromSuper_returnsMirroredMethod() throws Exception {
        Method RESULT_METHOD = Subclass.class.getMethod("publicNoParamMethod");

        mInvocationHelper = spy(mInvocationHelper);
        doReturn(RESULT_METHOD.getParameterTypes()).when(mInvocationHelper).unwrapParameterTypes(any(Method.class));

        Method method = mock(Method.class);

        Method result = mInvocationHelper.findMirrorMethod(method, RESULT_METHOD.getName(), Subclass.class);

        assertEquals(RESULT_METHOD, result);
    }

    @Test
    public void invokeMirrorMethod_correctParameters_correctInvocationDone() throws Exception {
        Object[] PARAMETERS = new Object[0];
        Object INSTANCE = new Object();
        Class<?> RETURN_TYPE = this.getClass();
        Object RETURN = new Object();

        mInvocationHelper = spy(mInvocationHelper);
        doReturn(PARAMETERS).when(mInvocationHelper).unwrapParameters(any());

        when(mWrapper.wrap(any(), any())).thenReturn(RETURN);

        Method mockMethod = mock(Method.class);
        when(mockMethod.invoke(any(), any())).thenReturn(RETURN);

        mInvocationHelper.invokeMirrorMethod(mockMethod, INSTANCE, RETURN_TYPE, PARAMETERS);

        verify(mockMethod, times(1)).setAccessible(true);
        verify(mockMethod, times(1)).invoke(INSTANCE, PARAMETERS);
        verify(mWrapper, times(1)).wrap(RETURN, RETURN_TYPE);
    }

    private static class SomeClass {

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