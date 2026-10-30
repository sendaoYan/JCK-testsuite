/*
 * @test
 * @summary JLS 16.1.2: after a && b, a variable assigned only in b is not
 *          definitely assigned (b is skipped when a is false), so reading it is a
 *          compile-time error.
 * @compile/fail ConditionalAndFail.java
 */

class ConditionalAndFail {
    void m(boolean b) {
        int x;
        boolean r = b && ((x = 1) == 1);
        int y = x;   // error: variable x might not have been initialized
    }
}
