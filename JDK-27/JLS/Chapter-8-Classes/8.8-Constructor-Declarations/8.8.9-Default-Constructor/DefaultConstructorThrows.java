/*
 * @test
 * @summary JLS 8.8.9: the default constructor has no throws clause, so it is a
 *          compile-time error if the superclass no-arg constructor it implicitly
 *          invokes declares a checked exception.
 * @compile/fail DefaultConstructorThrows.java
 */

class DefaultConstructorThrows {
    static class Base {
        Base() throws java.io.IOException {}
    }
    static class Sub extends Base {   // error: unreported exception IOException in default constructor
    }
}
