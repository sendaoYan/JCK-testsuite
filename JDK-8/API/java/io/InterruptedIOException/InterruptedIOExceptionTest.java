/*
 * @test
 * @summary java.io.InterruptedIOException API conformance: bytesTransferred and inheritance.
 * @run main InterruptedIOExceptionTest
 */

import java.io.IOException;
import java.io.InterruptedIOException;

public class InterruptedIOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInterruptedIOException() {
        InterruptedIOException e = new InterruptedIOException("m");
        check(e instanceof IOException, "extends IOException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(e.bytesTransferred == 0, "bytesTransferred defaults to 0");
        e.bytesTransferred = 5;
        check(e.bytesTransferred == 5, "bytesTransferred public field is writable");
    }

    public static void main(String[] args) {
        testInterruptedIOException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
