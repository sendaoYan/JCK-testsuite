/*
 * @test
 * @summary JLS 9.4 (Java SE 8): it is a compile-time error if an interface method is
 *          declared synchronized.
 * @compile/fail InterfaceMethodCannotBeSynchronized.java
 */

interface InterfaceMethodCannotBeSynchronized {
    synchronized void m();   // error: modifier synchronized not allowed here
}
