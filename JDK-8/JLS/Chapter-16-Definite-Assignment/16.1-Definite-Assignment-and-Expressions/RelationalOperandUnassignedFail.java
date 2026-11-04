/*
 * @test
 * @summary JLS 16.1.6 (Java SE 8): each operand of a relational or equality
 *          expression must be definitely assigned before it is evaluated.
 * @compile/fail RelationalOperandUnassignedFail.java
 */

class RelationalOperandUnassignedFail {
    void m() {
        int x;
        boolean r = x == 1;   // error: variable x might not have been initialized
    }
}
