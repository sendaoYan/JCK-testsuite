/*
 * @test
 * @summary JLS 16.2.8: in "assert e1 : e2", the detail expression e2 is evaluated
 *          only when e1 is false (and assertions are enabled), so a variable
 *          assigned only in e2 is not definitely assigned after the assert.
 * @compile/fail AssertDetailMessageFail.java
 */

class AssertDetailMessageFail {
    void m(boolean b) {
        int x;
        assert b : (x = 1);
        int y = x;   // error: variable x might not have been initialized
    }
}
