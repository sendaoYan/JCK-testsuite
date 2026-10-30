/*
 * @test
 * @summary JLS 16.1.8 (definite unassignment): a blank final local variable may be
 *          assigned at most once.
 * @compile/fail BlankFinalLocalDoubleAssignFail.java
 */

class BlankFinalLocalDoubleAssignFail {
    void m() {
        final int x;
        x = 1;
        x = 2;   // error: variable x might already have been assigned
    }
}
