package com.mirror;

import com.mirror.helper.MirrorHelper;

public class MirrorValidator {

    private MirrorHelper mMirrorHelper;

    public MirrorValidator(MirrorHelper mirrorHelper) {
        mMirrorHelper = mirrorHelper;
    }

    public void validateMirrorClass(Class<?> mirrorClass) {
        if (!mMirrorHelper.isMirror(mirrorClass)) {
            throw new ClassNotMirrorException(mirrorClass);
        }

        if (!mirrorClass.isInterface()) {
            throw new MirrorValidationException("mirror should be an interface");
        }
    }

    public void validateMirrorFactoryClass(Class<?> mirrorFactoryClass) {
        if (!mMirrorHelper.isMirrorFactory(mirrorFactoryClass)) {
            throw new ClassNotMirrorFactoryException(mirrorFactoryClass);
        }

        if (!mirrorFactoryClass.isInterface()) {
            throw new MirrorValidationException("mirror factory should be an interface");
        }
    }
}
