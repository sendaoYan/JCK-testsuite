/*
 * @test
 * @summary java.io.WriteAbortedException API conformance: detail cause and inheritance.
 * @run main WriteAbortedExceptionTest
 */

import java.io.ObjectStreamException;
import java.io.WriteAbortedException;

public class WriteAbortedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWriteAbortedException() {
        Exception cause = new Exception();
        WriteAbortedException e = new WriteAbortedException("s", cause);
        check(e instanceof ObjectStreamException, "extends ObjectStreamException");
        check(e.detail == cause, "detail field holds the cause");
        check(e.getCause() == cause, "getCause returns the detail exception");
        check(e.getMessage().startsWith("s"), "message starts with the supplied string");
    }

    public static void main(String[] args) {
        testWriteAbortedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
