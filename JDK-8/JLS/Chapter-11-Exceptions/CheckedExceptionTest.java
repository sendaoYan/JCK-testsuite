/*
 * @test
 * @summary JLS 11.2 (Java SE 8): a checked exception must be caught or declared;
 *          calling a method that throws it without handling is a compile error.
 * @compile/fail CheckedExceptionTest.java
 */

class CheckedExceptionTest {
    static void risky() throws java.io.IOException {}
    void m() {
        risky();   // error: unreported exception java.io.IOException
    }
}
