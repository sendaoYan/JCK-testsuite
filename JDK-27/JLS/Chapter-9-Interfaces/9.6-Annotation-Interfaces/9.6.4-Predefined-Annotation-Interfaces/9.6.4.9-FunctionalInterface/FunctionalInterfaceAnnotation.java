/*
 * @test
 * @summary JLS 9.6.4.9: FunctionalInterface marks an interface with exactly one
 *          abstract method; a conforming interface compiles.
 * @compile FunctionalInterfaceAnnotation.java
 */

@FunctionalInterface
interface FunctionalInterfaceAnnotation {
    void m();
}
