package com.mirror.helper;

import com.mirror.MirroredClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MirrorHelperTest {

    @Test
    public void isMirror_onMirroredClass_returnsTrue() throws Exception {
        MirrorHelper mirrorHelper = new MirrorHelper();

        assertTrue(mirrorHelper.isMirror(MirrorClassEx.class));
    }

    @Test
    public void isMirror_onNonMirroredClass_returnsFalse() throws Exception {
        MirrorHelper mirrorHelper = new MirrorHelper();

        assertFalse(mirrorHelper.isMirror(NonMirrorClassEx.class));
    }

    @Test
    public void getMirroredTypeName_onMirroredClass_returnsExpectedName() throws Exception {
        MirrorHelper mirrorHelper = new MirrorHelper();

        String name = mirrorHelper.getMirroredTypeName(MirrorClassEx.class);
        assertEquals("com.mirror.Mirror", name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMirroredTypeName_onNonMirroredClass_throwsIllegalArgumentException() throws Exception {
        MirrorHelper mirrorHelper = new MirrorHelper();

        mirrorHelper.getMirroredTypeName(NonMirrorClassEx.class);
    }

    @MirroredClass("com.mirror.Mirror")
    private static interface MirrorClassEx {
    }

    private static interface NonMirrorClassEx {

    }
}