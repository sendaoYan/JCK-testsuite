/*
 * @test
 * @summary JLS 9.4.1.2: it is a compile-time error if a default method is
 *          override-equivalent with a non-private method of Object.
 * @compile/fail DefaultMethodOverrideEquivalentObjectMethod.java
 */

interface DefaultMethodOverrideEquivalentObjectMethod {
    default String toString() { return "x"; }   // error: override-equivalent to Object.toString
}
