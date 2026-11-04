/*
 * @test
 * @summary JLS 16.1.7 (Java SE 8): the left-hand variable of a compound assignment
 *          must be definitely assigned before the expression, because its old
 *          value is read.
 * @compile/fail CompoundAssignmentUnassignedFail.java
 */

class CompoundAssignmentUnassignedFail {
    void m() {
        int x;
        x += 1;   // error: variable x might not have been initialized
    }
}
