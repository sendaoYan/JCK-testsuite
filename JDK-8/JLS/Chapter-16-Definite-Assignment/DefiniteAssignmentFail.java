/*
 * @test
 * @summary JLS 16 (Java SE 8): a local variable that is assigned on only one branch
 *          of a conditional statement is not definitely assigned afterward.
 * @compile/fail DefiniteAssignmentFail.java
 */

class DefiniteAssignmentFail {
    void m(boolean b) {
        int x;
        if (b) { x = 1; }
        int y = x;   // error: variable x might not have been initialized
    }
}
