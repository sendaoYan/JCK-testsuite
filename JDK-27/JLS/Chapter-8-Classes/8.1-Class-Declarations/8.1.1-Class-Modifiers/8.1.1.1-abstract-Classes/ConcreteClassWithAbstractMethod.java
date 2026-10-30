/*
 * @test
 * @summary JLS 8.1.1.1: a normal class may have abstract methods only if it is
 *          abstract; it is a compile-time error otherwise.
 * @compile/fail ConcreteClassWithAbstractMethod.java
 */

class ConcreteClassWithAbstractMethod {
    abstract void m();   // error: abstract method in a non-abstract class
}
