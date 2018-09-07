package com.mirror.validation;

import com.mirror.helper.MirrorHelper;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class MirrorValidatorTest {

    private MirrorValidator mMirrorValidator;
    private MirrorHelper mMirrorHelper;

    @Before
    public void setUp() throws Exception {
        mMirrorHelper = mock(MirrorHelper.class);

        mMirrorValidator = new MirrorValidator(mMirrorHelper);
    }

    @Test
    public void validateMirrorClass_validMirror_doesNotThrow() throws Exception {
        when(mMirrorHelper.isMirror(any())).thenReturn(true);

        mMirrorValidator.validateMirrorClass(InterfaceClass.class);
    }

    @Test(expected = ClassNotMirrorException.class)
    public void validateMirrorClass_nonMirror_throwsClassNotMirrorException() throws Exception {
        when(mMirrorHelper.isMirror(any())).thenReturn(false);

        mMirrorValidator.validateMirrorClass(InterfaceClass.class);
    }

    @Test(expected = MirrorValidationException.class)
    public void validateMirrorClass_notInterface_throwsMirrorValidationException() throws Exception {
        when(mMirrorHelper.isMirror(any())).thenReturn(true);

        mMirrorValidator.validateMirrorClass(this.getClass());
    }

    private static interface InterfaceClass {

    }
}