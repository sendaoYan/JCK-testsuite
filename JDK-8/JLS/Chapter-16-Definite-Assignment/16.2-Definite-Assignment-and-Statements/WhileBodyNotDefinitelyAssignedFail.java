/*
 * @test
 * @summary JLS 16.2.10 (Java SE 8): a variable assigned only in the body of a while
 *          loop is not definitely assigned after the loop.
 * @compile/fail WhileBodyNotDefinitelyAssignedFail.java
 */

class WhileBodyNotDefinitelyAssignedFail {
    void m(boolean b) {
        int x;
        while (b) { x = 1; }
        int y = x;   // error: variable x might not have been initialized
    }
}
