package com.mirror.wrapping;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class Unwrapper {

    public Object unwrap(Object object) {
        if (object == null) {
            return null;
        }

        if (object.getClass().isArray()) {
            return unwrapArray(object);
        }

        return unwrapObject(object);
    }

    public Object unwrapArray(Object array) {
        Class<?> componentType = array.getClass().getComponentType();
        if (componentType.isPrimitive()) {
            return array;
        }

        Object[] arrayObjects = (Object[]) array;

        Class<?> unwrappedComponent = unwrapObjectType(componentType);
        Object[] unwrappedArrayObjects = (Object[]) Array.newInstance(unwrappedComponent, arrayObjects.length);

        for (int i = 0; i < arrayObjects.length; i++) {
            Object unwrappedObject = unwrap(arrayObjects[i]);
            unwrappedArrayObjects[i] = unwrappedObject;
        }

        return unwrappedArrayObjects;
    }

    public Object unwrapObject(Object object) {
        if (Proxy.isProxyClass(object.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);
            // TODO: IF OUR INVOCATION HANDLER, RETURN INSTANCE FROM WITHIN IT
        }

        return object;
    }

    public Class<?> unwrapType(Class<?> type) {
        if (type.isPrimitive()) {
            return type;
        }

        if (type.isArray()) {
            return unwrapArrayType(type);
        }

        return unwrapObjectType(type);
    }

    public Class<?> unwrapArrayType(Class<?> type) {
        Class<?> componentType = type.getComponentType();
        return Array.newInstance(componentType, 0).getClass();
    }

    public Class<?> unwrapObjectType(Class<?> type) {
        // TODO: IF THE CLASS IS A MIRROR, FIND THE MIRRORED CLASS
        return type;
    }
}
