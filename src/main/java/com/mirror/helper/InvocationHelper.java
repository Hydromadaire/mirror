package com.mirror.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InvocationHelper {

    public Object[] unwrapParameters(Object... parameters) {
        List<Object> unwrappedParameters = new ArrayList<Object>();

        for (Object parameter : parameters) {
            // TODO: UNWRAP
        }

        return unwrappedParameters.toArray();
    }

    public Class<?>[] unwrapParameterTypes(Method method) {
        List<Class<?>> parameterTypes = new ArrayList<Class<?>>();

        for (Class<?> type : method.getParameterTypes()) {
            // TODO: UNWRAP
        }

        Class<?>[] parameterTypesArr = new Class[parameterTypes.size()];
        return parameterTypes.toArray(parameterTypesArr);
    }

    public Method findMirrorMethod(Method method, String methodName, Class<?> targetClass) throws NoSuchMethodException {
        Class<?>[] parameterTypes = unwrapParameterTypes(method);
        return targetClass.getMethod(methodName, parameterTypes);
    }

    public Object invokeMirrorMethod(Method method, Object instance, Object... parameters) throws InvocationTargetException, IllegalAccessException {
        Object[] unwrappedParameters = unwrapParameters(parameters);

        method.setAccessible(true);
        Object result = method.invoke(instance, unwrappedParameters);

        // TODO: WRAP
        return result;
    }
}
