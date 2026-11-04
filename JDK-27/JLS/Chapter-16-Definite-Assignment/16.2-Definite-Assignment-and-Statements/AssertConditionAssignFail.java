/*
 * @test
 * @summary JLS 16.2.8: a variable assigned only in the condition expression e1 of
 *          an assert statement is not definitely assigned after the assert,
 *          because e1 may not be evaluated (assertions can be disabled).
 * @compile/fail AssertConditionAssignFail.java
 */

class AssertConditionAssignFail {
    void m() {
        int x;
        assert (x = 1) == 1;
        int y = x;   // error: variable x might not have been initialized
    }
}
