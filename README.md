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
    void hello(); // the signature of the method in SomeClass
}
```

Now, we have to create the mirror object:

```Java
ClassLoader classLoaderForHiddenClass = classloader;// classloader which loads hidden class
MirrorCreator mirrorCreator = MirrorCreator.createForClassLoader(classLoaderForHiddenClass);
Mirror<MirroredSomeClass> mirror = mirrorCreator.create(MirroredSomeClass.class);

Object someClassInstance = instance;// instance of hidden class
MirroredSomeClass someClass = mirror.create(someClassInstance);
```
Now you can access the hidden class via the mirror, using the methods you have defined:

```Java
someClass.hello();
```
