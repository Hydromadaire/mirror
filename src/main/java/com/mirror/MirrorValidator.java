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

    public void validateMirrorCreatorClass(Class<?> mirrorCreatorClass) {
        if (!mirrorCreatorClass.isInterface()) {
            throw new MirrorValidationException("mirror creator should be an interface");
        }
    }
}
