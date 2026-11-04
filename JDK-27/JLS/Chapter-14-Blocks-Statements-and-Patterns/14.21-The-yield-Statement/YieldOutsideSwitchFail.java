/*
 * @test
 * @summary JLS 14.21: a yield statement may appear only within a switch
 *          expression; using it elsewhere is a compile-time error.
 * @compile/fail YieldOutsideSwitchFail.java
 */

class YieldOutsideSwitchFail {
    void m() {
        yield 1;   // error: yield outside of switch expression
    }
}
