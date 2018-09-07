package com.mirror;

import com.mirror.helper.ReflectionHelper;
import com.mirror.helper.MirrorHelper;
import com.mirror.invocation.MirrorCreatorInvocationHandler;
import com.mirror.invocation.MirrorInvocationHandler;
import com.mirror.validation.ClassNotMirrorCreatorException;
import com.mirror.validation.ClassNotMirrorException;
import com.mirror.validation.MirrorValidationException;
import com.mirror.validation.MirrorValidator;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.Wrapper;

import java.lang.reflect.Proxy;

/**
 * <p>
 *     <code>Mirror</code> is responsible from creation of proxies which mirror objects via reflection.
 * </p>
 * <p>
 *     There are 2 types of proxies which can be created:
 *     <ul>
 *         <li>Instance Proxies: reflect instance accesses to a class using reflections. Can be created using a <code>MirrorCreator</code>, or using a call to {@link #mirror(Class, Object)}.</li>
 *         <li>Creation Proxies: reflect constructor calls and allow for the creation of <em>Instance Proxies</em>. Can be created using {@link #createMirrorCreator(Class)}</li>
 *     </ul>
 *
 *     Each proxy is an interface, which provides signature for method calls.
 * </p>
 * <p>
 *     The <em>Instance Proxy</em> provides access to <em>static</em> <b>or</b> <em>instance</em> methods and fields. The representing interface, much define the method signature, or
 *     the field type. Such interface must be annotated with {@link MirroredClass}.
 *
 *     <p>
 *          To define methods, precisely define the signature of the mirrored method, without consideration to access modifiers (<strong>public, protected, private, default</strong>), or <strong>static modifier</strong>.
 *          For example:
 *          <pre>
 *              // hidden class method
 *              protected static void callMe(String a) {
 *                  ......
 *              }
 *
 *              // mirror signature
 *              void callMe(String a);
 *          </pre>
 *     </p>
 * </p>
 * <p>
 *     The <em>Creation Proxy</em> provides access to constructors.
 *     <p>
 *         To define constructors, define a method which returns the object created by the constructor (it's <em>Instance Proxy</em> interface), followed by any name, and the exact parameters it receives. The method
 *         must be annotated with {@link MirrorCreator}.
 *         For example:
 *         <pre>{@code
 *             // constructor in class
 *             public SomeClass(int count, String name) {
 *                 ....
 *             }
 *
 *             // constructor signature
 *             @MirrorCreator
 *             SomeClassMirror create(int count, String name);
 *         }</pre>
 *     </p>
 * </p>
 * <p>
 *     Mirror proxies provide field access which is split into 2 parts: getters and setters.
 *     <p>
 *         To define getters, define a parameterless method, which returns the same type as the field, and use the {@link GetField} annotation. The name of the method doesn't affect the mirror.
 *         For example:
 *         <pre>{@code
 *             // the field
 *             private int mCount;
 *
 *             // the mirror signature
 *             @GetField("mCount")
 *             int getCount();
 *         }</pre>
 *     </p>
 *     <p>
 *         To define setters, define a <strong>void</strong> method with a single parameter with the type of the field, and use the {@link SetField} annotation. The name of the method doesn't affect the mirror.
 *         For example:
 *         <pre>{@code
 *             // the field
 *             private int mCount;
 *
 *             // the mirror signature
 *             @SetField("mCount")
 *             void setCount(int count);
 *         }</pre>
 *     </p>
 * </p>
 * <p>
 *     Mirror proxies provide the ability to wrap exceptions thrown from mirrored methods. If a checked exception is thrown from a method (or constructor) which is mirrored, the mirroring signature
 *     must either define the exception itself, or wrap the exception into a different one.
 *     For example:
 *     <pre>{@code
 *          // method with exception
 *          public void callMe() throws SomeException {
 *              ....
 *          }
 *
 *          // the mirror signature
 *          void callMe() throws SomeException;
 *
 *          // or with wrapping
 *          void callMe() throws MyException;
 *
 *          // where MyException is define with similarly to this:
 *          @MirroredException("com.package.SomeException")
 *          public class MyException extends Exception {
 *          }
 *     }</pre>
 *
 *     An exception wrapper must be defined with either a default constructor or a <em>cause constructor</em> (constructor which receives a <code>Throwable</code>).
 *     Either of this is necessary, for it to be used.
 * </p>
 * <p>
 *     Value wrapping and unwrapping is an automatic process done to values passed and returned from and to mirror methods. These processes
 *     automatically convert values between mirror proxies and their mirrored targets. This allows to define signatures in proxies using other mirror types
 *     which mirror the original value which was defined in the proxy.
 *     For example:
 *     <pre>
 *         // original signature
 *         SomeClass callMe();
 *
 *         // mirror signature without wrapping
 *         SomeClass callMe();
 *
 *         // mirror signature with wrapping, were MirroredSomeClass is a mirror proxy of SomeClass
 *         MirroredSomeClass callMe()
 *     </pre>
 *
 *     This is useful for when parameters or return types of methods are hidden which require reflection access.
 * </p>
 */
public class Mirror {

    private final ClassLoader mClassLoader;
    private final MirrorHelper mMirrorHelper;
    private final ReflectionHelper mReflectionHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final MirrorValidator mMirrorValidator;

    public Mirror(ClassLoader classLoader, MirrorHelper mirrorHelper, ReflectionHelper reflectionHelper, ThrowableWrapper throwableWrapper, MirrorValidator mirrorValidator) {
        mClassLoader = classLoader;
        mMirrorHelper = mirrorHelper;
        mReflectionHelper = reflectionHelper;
        mThrowableWrapper = throwableWrapper;
        mMirrorValidator = mirrorValidator;
    }

    private Mirror(ClassLoader classLoader) {
        mClassLoader = classLoader;
        mMirrorHelper = new MirrorHelper();
        mReflectionHelper = new ReflectionHelper(new Wrapper(mMirrorHelper, this), new Unwrapper(mMirrorHelper, mClassLoader));
        mThrowableWrapper = new ThrowableWrapper();
        mMirrorValidator = new MirrorValidator(mMirrorHelper);
    }

    public <T> T mirror(Class<T> mirrorClass, Object instance) throws MirrorCreationException {
        try {
            mMirrorValidator.validateMirrorClass(mirrorClass);
            Class<?> targetClass = mMirrorHelper.getMirrorTargetType(mirrorClass, mClassLoader);

            if (!targetClass.isInstance(instance)) {
                throw new IllegalArgumentException("instance is not of targetClass type: " + targetClass.getName());
            }

            return createMirrorProxy(mirrorClass, targetClass, instance);
        } catch (ClassNotFoundException | ClassNotMirrorException | MirrorValidationException e) {
            throw new MirrorCreationException(e);
        }
    }

    public <T> T createMirrorCreator(Class<T> mirrorCreatorClass) throws MirrorCreatorCreationException {
        try {
            mMirrorValidator.validateMirrorCreatorClass(mirrorCreatorClass);
            return createMirrorCreatorProxy(mirrorCreatorClass);
        } catch (MirrorValidationException | ClassNotMirrorCreatorException e) {
            throw new MirrorCreatorCreationException(e);
        }
    }

    private <T> T createMirrorProxy(Class<T> mirrorClass, Class<?> targetClass, Object instance) {
        return mirrorClass.cast(Proxy.newProxyInstance(
                mirrorClass.getClassLoader(),
                new Class[] {mirrorClass},
                new MirrorInvocationHandler(mReflectionHelper, mThrowableWrapper, targetClass, instance, mClassLoader)));
    }

    private <T> T createMirrorCreatorProxy(Class<T> mirrorCreatorClass) {
        return mirrorCreatorClass.cast(Proxy.newProxyInstance(
                mirrorCreatorClass.getClassLoader(),
                new Class[] {mirrorCreatorClass},
                new MirrorCreatorInvocationHandler(mReflectionHelper, mThrowableWrapper, mMirrorHelper, mMirrorValidator, mClassLoader)));
    }

    public static Mirror createForClassLoader(ClassLoader classLoader) {
        return new Mirror(classLoader);
    }
}
