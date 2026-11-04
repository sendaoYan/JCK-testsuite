/*
 * @test
 * @summary JLS 11.2.1: a method reference expression of the form Primary::
 *          Identifier can throw an exception class if the Primary subexpression
 *          can throw it.
 * @compile/fail MethodReferencePrimaryThrowsChecked.java
 */

import java.io.IOException;

class MethodReferencePrimaryThrowsChecked {
    void m() {
        n(f()::g);   // error: unreported IOException from the Primary expression f()
    }

    void n(Runnable r) {}
    Helper f() throws IOException { return new Helper(); }

    static class Helper {
        void g() {}
    }
}
