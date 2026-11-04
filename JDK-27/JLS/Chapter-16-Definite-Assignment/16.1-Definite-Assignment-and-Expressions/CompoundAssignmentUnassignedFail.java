/*
 * @test
 * @summary JLS 16.1.7: the left-hand variable of a compound assignment must be
 *          definitely assigned before the expression (its old value is read), so
 *          "int x; x += 1;" is a compile-time error.
 * @compile/fail CompoundAssignmentUnassignedFail.java
 */

class CompoundAssignmentUnassignedFail {
    void m() {
        int x;
        x += 1;   // error: variable x might not have been initialized
    }
}
