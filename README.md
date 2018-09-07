# mirror

Mirror is a reflection tool for Java. It allows usage of classes and objects via reflection without having to actually use the Java reflection API.

# How to Use

Instead of calling a class via reflection, all that is needed is to define an interface which represents the class.

So, for a given hidden class:
```Java
package com.package;

private class SomeClass {

    private String mName;

    public SomeClass(String name) {
        mName = name;
    }

    public void hello() {
        System.out.println("hello " + mName);
    }
}
```
We can create an interface which defines it's method signatures:

```Java
@MirroredClass("com.package.SomeClass")
public interface MirroredSomeClass {
    void hello(); // the signature of the method in SomeClass
}
```
Next, we need to create a `Mirror` object which will allow us to mirror the hidden class:

```Java
ClassLoader classLoaderForHiddenClass = classloader;// classloader which loads hidden class
Mirror mirror = Mirror.createForClassLoader(classLoaderForHiddenClass);
```

Assuming we have an instance of `SomeClass`, we can create the mirror object with it:

```Java
Object someClassInstance = instance;// instance of hidden class
MirroredSomeClass someClass = mirror.mirror(MirroredSomeClass.class, someClassInstance);
```

If we don't have an instance, we need a `MirrorCreator` interface, which will mirror constructor calls:
```Java
public interface SomeClassCreator {
    @MirrorCreator
    MirroredSomeClass create(String name); // This will invoke the parametered constructor
}
```
Let's initialize the `MirrorCreator` and create the mirror:
```Java
SomeClassCreator creator = mirror.createMirrorCreator(SomeClassCreator.class);
MirroredSomeClass someClass = creator.create("jack");
```

Now we can access the hidden class via the mirror, using the methods we have defined:

```Java
someClass.hello();
```
