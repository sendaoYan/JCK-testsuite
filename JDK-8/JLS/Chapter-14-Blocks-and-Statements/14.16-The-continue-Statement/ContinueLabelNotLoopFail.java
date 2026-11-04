/*
 * @test
 * @summary JLS 14.16: the target of a labeled continue statement must be a
 *          while, do, or for statement; a label naming an enclosing block is a
 *          compile-time error.
 * @compile/fail ContinueLabelNotLoopFail.java
 */

class ContinueLabelNotLoopFail {
    void m() {
        block: {
            for (int i = 0; i < 1; i++) {
                continue block;   // error: not a loop label: block
            }
        }
    }
}
