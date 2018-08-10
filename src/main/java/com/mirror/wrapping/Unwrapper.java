package com.mirror.wrapping;

import com.mirror.MirrorInvocationHandler;
import com.mirror.helper.MirrorHelper;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class Unwrapper {

    private MirrorHelper mMirrorHelper;
    private ClassLoader mClassLoader;

    public Unwrapper(MirrorHelper mirrorHelper, ClassLoader classLoader) {
        mMirrorHelper = mirrorHelper;
        mClassLoader = classLoader;
    }

    public Object unwrap(Object object) throws UnwrappingException {
        if (object == null) {
            return null;
        }

        if (object.getClass().isArray()) {
            return unwrapArray(object);
        }

        return unwrapObject(object);
    }

    public Object unwrapArray(Object array) throws UnwrappingException {
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

            if (invocationHandler instanceof MirrorInvocationHandler) {
                return ((MirrorInvocationHandler) invocationHandler).getTargetInstance();
            }
        }

        return object;
    }

    public Class<?> unwrapType(Class<?> type) throws UnwrappingException {
        if (type.isPrimitive()) {
            return type;
        }

        if (type.isArray()) {
            return unwrapArrayType(type);
        }

        return unwrapObjectType(type);
    }

    public Class<?> unwrapArrayType(Class<?> type) throws UnwrappingException {
        Class<?> componentType = type.getComponentType();
        Class<?> unwrappedComponentType = unwrapType(componentType);

        return Array.newInstance(unwrappedComponentType, 0).getClass();
    }

    public Class<?> unwrapObjectType(Class<?> type) throws UnwrappingException {
        try {
            if (mMirrorHelper.isMirror(type)) {
                return getMirrorType(type);
            }

            return type;
        } catch (ClassNotFoundException e) {
            throw new UnwrappingException(e);
        }
    }

    private Class<?> getMirrorType(Class<?> type) throws ClassNotFoundException {
        String typeName = mMirrorHelper.getMirroredTypeName(type);
        return Class.forName(typeName, true, mClassLoader);
    }
}
