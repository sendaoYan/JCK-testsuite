/*
 * @test
 * @summary java.io.ObjectStreamException API conformance: abstract supertype of serialization errors.
 * @run main ObjectStreamExceptionTest
 */

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

public class ObjectStreamExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectStreamException() {
        ObjectStreamException e = new InvalidObjectException("reason");
        check(e instanceof IOException, "extends IOException");
        check("reason".equals(e.getMessage()), "message propagated from subclass");
    }

    public static void main(String[] args) {
        testObjectStreamException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
