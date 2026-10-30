/*
 * @test
 * @summary JLS 11.2.1: a method invocation expression can throw an exception class
 *          that some expression in its argument list can throw.
 * @compile/fail ArgumentExpressionThrowsChecked.java
 */

import java.io.IOException;

class ArgumentExpressionThrowsChecked {
    void m() {
        n(f());   // error: unreported IOException from argument expression
    }

    void n(int x) {}
    int f() throws IOException { return 1; }
}
