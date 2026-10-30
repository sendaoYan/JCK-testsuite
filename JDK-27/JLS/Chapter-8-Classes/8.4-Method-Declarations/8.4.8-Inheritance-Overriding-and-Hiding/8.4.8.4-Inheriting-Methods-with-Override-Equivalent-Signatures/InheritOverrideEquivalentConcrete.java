/*
 * @test
 * @summary JLS 8.4.8.4 (Example 8.4.8.4-1): a class cannot inherit a concrete
 *          method whose signature is override-equivalent with another inherited
 *          method; C extends A<String> inherits two m(String) methods.
 * @compile/fail InheritOverrideEquivalentConcrete.java
 */

class A<T> {
    void m(String s) {}   // (1)
    void m(T t) {}        // (2)
}

class InheritOverrideEquivalentConcrete extends A<String> {
    // inherits m(String) from (1) and, via the parameterization T=String,
    // m(String) from (2): two override-equivalent concrete methods
}
