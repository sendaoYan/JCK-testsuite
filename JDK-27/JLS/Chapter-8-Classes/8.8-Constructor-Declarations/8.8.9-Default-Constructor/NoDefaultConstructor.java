/*
 * @test
 * @summary JLS 8.8.9: it is a compile-time error if a default constructor is
 *          implicitly declared but the superclass has no accessible no-arg
 *          constructor.
 * @compile/fail NoDefaultConstructor.java
 */

class Base {
    Base(int x) {}   // no no-arg constructor
}

class NoDefaultConstructor extends Base {
    // error: constructor Base in class Base cannot be applied to given types
}
