/*
 * @test
 * @summary JLS 11.2.1: a class instance creation expression can throw the checked
 *          exception classes of the chosen constructor's throws clause.
 * @compile/fail ClassInstanceCreationThrowsChecked.java
 */

import java.io.IOException;

class ClassInstanceCreationThrowsChecked {
    void m() {
        new C();   // error: unreported exception IOException from constructor C()
    }

    static class C {
        C() throws IOException {}
    }
}
