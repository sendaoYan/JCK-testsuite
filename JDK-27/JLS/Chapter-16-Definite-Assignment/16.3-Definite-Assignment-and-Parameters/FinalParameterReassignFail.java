/*
 * @test
 * @summary JLS 16.3 / 8.4.1: a formal parameter is definitely assigned before the
 *          method body; a final parameter may not be assigned a second time.
 * @compile/fail FinalParameterReassignFail.java
 */

class FinalParameterReassignFail {
    void m(final int p) {
        p = 2;   // error: final parameter p may not be assigned
    }
}
