/*
 * @test
 * @summary java.io.StreamCorruptedException API conformance: constructors and inheritance.
 * @run main StreamCorruptedExceptionTest
 */

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;

public class StreamCorruptedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStreamCorruptedException() {
        StreamCorruptedException e = new StreamCorruptedException("msg");
        check(e instanceof ObjectStreamException, "extends ObjectStreamException");
        check(e instanceof IOException, "extends IOException");
        check("msg".equals(e.getMessage()), "message from (String) ctor");
        check(new StreamCorruptedException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testStreamCorruptedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
