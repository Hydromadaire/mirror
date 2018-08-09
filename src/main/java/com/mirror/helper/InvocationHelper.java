package com.mirror.helper;

import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.Wrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InvocationHelper {

    private final Wrapper mWrapper;
    private final Unwrapper mUnwrapper;

    public InvocationHelper(Wrapper wrapper, Unwrapper unwrapper) {
        mWrapper = wrapper;
        mUnwrapper = unwrapper;
    }

    public Object[] unwrapParameters(Object... parameters) throws UnwrappingException {
        List<Object> unwrappedParameters = new ArrayList<Object>();

        for (Object parameter : parameters) {
            Object unwrapped = mUnwrapper.unwrap(parameter);
            unwrappedParameters.add(unwrapped);
        }

        return unwrappedParameters.toArray();
    }

    public Class<?>[] unwrapParameterTypes(Method method) throws UnwrappingException {
        List<Class<?>> parameterTypes = new ArrayList<Class<?>>();

        for (Class<?> type : method.getParameterTypes()) {
            Class<?> unwrapped = mUnwrapper.unwrapType(type);
            parameterTypes.add(unwrapped);
        }

        Class<?>[] parameterTypesArr = new Class[parameterTypes.size()];
        return parameterTypes.toArray(parameterTypesArr);
    }

    public Method findMirrorMethod(Method method, String methodName, Class<?> targetClass) throws NoSuchMethodException, UnwrappingException {
        Class<?>[] parameterTypes = unwrapParameterTypes(method);
        return targetClass.getMethod(methodName, parameterTypes);
    }

    public Object invokeMirrorMethod(Method method, Object instance, Object... parameters) throws InvocationTargetException, IllegalAccessException, UnwrappingException {
        Object[] unwrappedParameters = unwrapParameters(parameters);

        method.setAccessible(true);
        Object result = method.invoke(instance, unwrappedParameters);

        return mWrapper.wrap(result);
    }
}
