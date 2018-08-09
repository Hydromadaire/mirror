package com.mirror.wrapping;

import java.lang.reflect.Array;

public class Wrapper {

    public Object wrap(Object object) {
        if (object == null) {
            return null;
        }

        if (object.getClass().isArray()) {
            return wrapArray(object);
        }

        return wrapObject(object);
    }

    public Object wrapArray(Object array) {
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

    public Object wrapObject(Object object) {
        // TODO: IF A WRAPPABLE CLASS: WRAP
        return object;
    }
}
