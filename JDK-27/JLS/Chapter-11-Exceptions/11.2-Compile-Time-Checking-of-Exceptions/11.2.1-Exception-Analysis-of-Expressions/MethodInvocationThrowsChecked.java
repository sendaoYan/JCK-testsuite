/*
 * @test
 * @summary JLS 11.2.1: a method invocation expression can throw the checked
 *          exception classes of the invoked method's throws clause.
 * @compile/fail MethodInvocationThrowsChecked.java
 */

import java.io.IOException;

class MethodInvocationThrowsChecked {
    void m() {
        n();   // error: unreported IOException
    }

    void n() throws IOException {}
}
