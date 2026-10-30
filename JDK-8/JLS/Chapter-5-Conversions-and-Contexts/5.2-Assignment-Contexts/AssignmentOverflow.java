/*
 * @test
 * @summary JLS 5.2 (Java SE 8): a constant expression must be representable in the
 *          target type for the implicit narrowing conversion to apply.
 * @compile/fail AssignmentOverflow.java
 */

class AssignmentOverflow {
    byte b = 128;   // error: incompatible types, possible lossy conversion
}
