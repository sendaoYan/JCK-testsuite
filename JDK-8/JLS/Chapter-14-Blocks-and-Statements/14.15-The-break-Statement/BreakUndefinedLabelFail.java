/*
 * @test
 * @summary JLS 14.15: a break statement with an identifier must name an enclosing
 *          labeled statement, else it is a compile-time error.
 * @compile/fail BreakUndefinedLabelFail.java
 */

class BreakUndefinedLabelFail {
    void m() {
        while (true) {
            break missing;   // error: undefined label: missing
        }
    }
}
