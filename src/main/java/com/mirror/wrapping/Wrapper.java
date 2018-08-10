package com.mirror.wrapping;

import com.mirror.Mirror;
import com.mirror.MirrorCreationException;
import com.mirror.MirrorCreator;
import com.mirror.helper.MirrorHelper;

import java.lang.reflect.Array;

public class Wrapper {

    private MirrorHelper mMirrorHelper;
    private MirrorCreator mMirrorCreator;

    public Wrapper(MirrorHelper mirrorHelper, MirrorCreator mirrorCreator) {
        mMirrorHelper = mirrorHelper;
        mMirrorCreator = mirrorCreator;
    }

    public Object wrap(Object object, Class<?> wrappingTarget) throws WrappingException {
        if (object == null) {
            return null;
        }

        if (object.getClass().isArray()) {
            if (!wrappingTarget.isArray()) {
                throw new IllegalArgumentException("cannot wrap array to non-array type");
            }

            return wrapArray(object, wrappingTarget.getComponentType());
        }

        return wrapObject(object, wrappingTarget);
    }

    public Object wrapArray(Object array, Class<?> wrappingTarget) throws WrappingException {
        Class<?> componentType = array.getClass().getComponentType();
        if (componentType.isPrimitive()) {
            return array;
        }

        Object[] arrayObjects = (Object[]) array;
        Object[] wrappedArrayObjects = (Object[]) Array.newInstance(componentType, arrayObjects.length);

        for (int i = 0; i < arrayObjects.length; i++) {
            Object wrapped = wrap(arrayObjects[i], wrappingTarget);
            wrappedArrayObjects[i] = wrapped;
        }

        return wrappedArrayObjects;
    }

    public Object wrapObject(Object object, Class<?> wrappingTarget) throws WrappingException {
        try {
            if (mMirrorHelper.isMirror(wrappingTarget)) {
                return createMirror(object, wrappingTarget);
            }

            return object;
        } catch (MirrorCreationException e) {
            throw new WrappingException(e);
        }
    }

    private Object createMirror(Object object, Class<?> mirrorClass) throws MirrorCreationException {
        Mirror<?> mirror = mMirrorCreator.createMirror(mirrorClass);
        return mirror.create(object);
    }
}
