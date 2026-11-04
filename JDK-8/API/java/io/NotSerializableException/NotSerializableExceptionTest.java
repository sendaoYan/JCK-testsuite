/*
 * @test
 * @summary java.io.NotSerializableException API conformance: constructors and inheritance.
 * @run main NotSerializableExceptionTest
 */

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;

public class NotSerializableExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNotSerializableException() {
        NotSerializableException e = new NotSerializableException("msg");
        check(e instanceof ObjectStreamException, "extends ObjectStreamException");
        check(e instanceof IOException, "extends IOException");
        check("msg".equals(e.getMessage()), "message from (String) ctor");
        check(new NotSerializableException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testNotSerializableException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
