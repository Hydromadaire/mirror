package com.mirror;

import com.mirror.helper.MirrorHelper;
import com.mirror.helper.ReflectionHelper;
import com.mirror.wrapping.ThrowableWrapper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MirrorCreatorInvocationHandlerTest {

    private MirrorCreatorInvocationHandler mMirrorCreatorInvocationHandler;
    private ReflectionHelper mReflectionHelper;
    private ThrowableWrapper mThrowableWrapper;
    private MirrorHelper mMirrorHelper;
    private MirrorValidator mMirrorValidator;
    private ClassLoader mClassLoader;

    @Before
    public void setUp() throws Exception {
        mReflectionHelper = mock(ReflectionHelper.class);
        mThrowableWrapper = new ThrowableWrapper();
        mMirrorHelper = mock(MirrorHelper.class);
        mMirrorValidator = mock(MirrorValidator.class);
        mClassLoader = this.getClass().getClassLoader();

        mMirrorCreatorInvocationHandler = new MirrorCreatorInvocationHandler(mReflectionHelper, mThrowableWrapper, mMirrorHelper, mMirrorValidator, mClassLoader);
    }

    @Test
    public void invoke_mirrorCreatingMethod_createAppropriateMirror() throws Throwable {
        Method METHOD = SomeClass.class.getDeclaredMethod("create");
        Class TARGET_CLASS = SomeClass.class;
        Object RESULT = new Object();

        when(mMirrorHelper.getMirrorTargetType(any(), any())).thenReturn(TARGET_CLASS);

        Constructor mockConstructor = mock(Constructor.class);

        when(mReflectionHelper.findMirrorConstructor(any(), any())).thenReturn(mockConstructor);
        when(mReflectionHelper.invokeMirrorConstructor(any(), any(), any())).thenReturn(RESULT);

        Object result = mMirrorCreatorInvocationHandler.invoke(null, METHOD, null);

        assertEquals(RESULT, result);
    }

    @Test(expected = MirrorCreationByProxyException.class)
    public void invoke_notMirrorCreatingMethod_throwMirrorCreationByProxyException() throws Throwable {
        Method METHOD = SomeClass.class.getDeclaredMethod("someMethod");

        mMirrorCreatorInvocationHandler.invoke(null, METHOD, null);
    }

    private static class SomeClass {

        @MirrorCreator
        public Object create() {
            return null;
        }

        public void someMethod() {

        }
    }
}