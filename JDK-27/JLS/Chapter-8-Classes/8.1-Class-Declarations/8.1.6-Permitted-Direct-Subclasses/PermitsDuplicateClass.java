/*
 * @test
 * @summary JLS 8.1.6: it is a compile-time error if the same class is specified
 *          more than once in a permits clause, even if named in different ways.
 * @compile/fail PermitsDuplicateClass.java
 */

package p;

sealed class PermitsDuplicateClass permits B, C, p.B {}   // error: p.B duplicates B
non-sealed class B extends PermitsDuplicateClass {}
non-sealed class C extends PermitsDuplicateClass {}
