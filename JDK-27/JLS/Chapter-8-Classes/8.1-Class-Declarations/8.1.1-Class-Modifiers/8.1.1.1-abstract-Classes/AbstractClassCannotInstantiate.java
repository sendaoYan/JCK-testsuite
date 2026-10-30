/*
 * @test
 * @summary JLS 8.1.1.1: it is a compile-time error to create an instance of an
 *          abstract class using a class instance creation expression.
 * @compile/fail AbstractClassCannotInstantiate.java
 */

abstract class A {}

class AbstractClassCannotInstantiate {
    void m() {
        A a = new A();   // error: A is abstract; cannot be instantiated
    }
}
