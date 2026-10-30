/*
 * @test
 * @summary JLS 9.6.4.9: it is a compile-time error to annotate an interface
 *          with more than one abstract method with FunctionalInterface.
 * @compile/fail FunctionalInterfaceNotFunctional.java
 */

@FunctionalInterface
interface FunctionalInterfaceNotFunctional {
    void m();
    void n();   // error: two abstract methods
}
