/*
 * @test
 * @summary JLS 16.1.6: each operand of a relational or equality expression
 *          (==, !=, <, >, <=, >=) must be definitely assigned before it is
 *          evaluated; reading an unassigned local is a compile-time error.
 * @compile/fail RelationalOperandUnassignedFail.java
 */

class RelationalOperandUnassignedFail {
    void m() {
        int x;
        boolean r = x == 1;   // error: variable x might not have been initialized
    }
}
