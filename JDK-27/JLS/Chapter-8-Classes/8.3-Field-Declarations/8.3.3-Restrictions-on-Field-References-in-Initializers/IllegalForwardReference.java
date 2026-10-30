/*
 * @test
 * @summary JLS 8.3.3: a reference by simple name to an instance variable from
 *          its own initializer or a point to the left of its declarator (not on
 *          the left hand side of an assignment) is a compile-time error.
 * @compile/fail IllegalForwardReference.java
 */

class IllegalForwardReference {
    int i = j;   // error: illegal forward reference
    int j = 1;
}
