/*
 * @test
 * @summary JLS 9.1.4: it is a compile-time error if the same class or interface
 *          is specified more than once in a permits clause.
 * @compile/fail SealedInterfacePermitsDuplicate.java
 */

sealed interface I permits C, C {}   // error: duplicate in permits
final class C implements I {}
