# mirror

Mirror as a reflection tool for Java. It allows usage of classes and objects via reflection without having to actually use the Java
reflection API.

# Examples

Instead of calling a class via reflection, all that is needed is to define an interface which represents the class.

So, for a given hidden class:
```Java
package com.package;

private class SomeClass {

    public void hello() {
    
    }
}
```
the interface would be:

```Java
@MirroredClass("com.package.SomeClass")
public interface MirroredSomeClass {
    void hello();
}
```

Now, we have to create the mirror object:

```Java
ClassLoader classLoaderForHiddenClass = // some classloader;
MirrorCreator mirrorCreator = MirrorCreator.createForClassLoader(classLoaderForHiddenClass);
Mirror<MirroredSomeClass> mirror = mirrorCreator.create(MirroredSomeClass.class);

Object someClassInstance = // instance;
MirroredSomeClass someClass = mirror.create(someClassInstance);
```
