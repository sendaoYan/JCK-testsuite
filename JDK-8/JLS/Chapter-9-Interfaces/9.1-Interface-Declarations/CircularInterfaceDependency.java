/*
 * @test
 * @summary JLS 9.1.3 (Java SE 8): it is a compile-time error if an interface is
 *          directly or indirectly its own superinterface.
 * @compile/fail CircularInterfaceDependency.java
 */

interface A extends B {}
interface B extends A {}   // error: cyclic inheritance involving A
