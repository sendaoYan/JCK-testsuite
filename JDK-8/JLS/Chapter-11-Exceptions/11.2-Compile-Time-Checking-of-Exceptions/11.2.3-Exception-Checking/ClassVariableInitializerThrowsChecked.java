/*
 * @test
 * @summary JLS 11.2.3 (Java SE 8): a class variable initializer may not throw a
 *          checked exception class.
 * @compile/fail ClassVariableInitializerThrowsChecked.java
 */

import java.io.IOException;

class ClassVariableInitializerThrowsChecked {
    static int x = f();   // error: unreported IOException in static field initializer

    static int f() throws IOException { return 1; }
}
