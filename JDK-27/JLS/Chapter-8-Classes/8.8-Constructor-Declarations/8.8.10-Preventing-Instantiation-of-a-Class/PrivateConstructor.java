/*
 * @test
 * @summary JLS 8.8.10: a private constructor prevents instantiation of a class
 *          from outside the class declaration.
 * @compile/fail PrivateConstructor.java
 */

class ClassOnly {
    private ClassOnly() {}
}

class PrivateConstructor {
    void m() {
        new ClassOnly();   // error: ClassOnly() has private access
    }
}
