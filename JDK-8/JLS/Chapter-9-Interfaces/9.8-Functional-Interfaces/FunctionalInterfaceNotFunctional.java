/*
 * @test
 * @summary JLS 9.8 (Java SE 8): it is a compile-time error if the FunctionalInterface
 *          annotation is applied to an interface that is not a functional interface.
 * @compile/fail FunctionalInterfaceNotFunctional.java
 */

@FunctionalInterface
interface FunctionalInterfaceNotFunctional {   // error: Unexpected @FunctionalInterface annotation
    void a();
    void b();
}
