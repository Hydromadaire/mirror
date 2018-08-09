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

    public Object wrap(Object object) throws WrappingException {
        if (object == null) {
            return null;
        }

        if (object.getClass().isArray()) {
            return wrapArray(object);
        }

        return wrapObject(object);
    }

    public Object wrapArray(Object array) throws WrappingException {
        Class<?> componentType = array.getClass().getComponentType();
        if (componentType.isPrimitive()) {
            return array;
        }

        Object[] arrayObjects = (Object[]) array;
        Object[] wrappedArrayObjects = (Object[]) Array.newInstance(componentType, arrayObjects.length);

        for (int i = 0; i < arrayObjects.length; i++) {
            Object wrapped = wrap(arrayObjects[i]);
            wrappedArrayObjects[i] = wrapped;
        }

        return wrappedArrayObjects;
    }

    public Object wrapObject(Object object) throws WrappingException {
        try {
            if (mMirrorHelper.isMirror(object.getClass())) {
                return createMirror(object);
            }

            return object;
        } catch (MirrorCreationException e) {
            throw new WrappingException(e);
        }
    }

    private Object createMirror(Object object) throws MirrorCreationException {
        Mirror<?> mirror = mMirrorCreator.createMirror(object.getClass());
        return mirror.create(object);
    }
}
