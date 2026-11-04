/*
 * @test
 * @summary java.io.NotActiveException API conformance: constructors and inheritance.
 * @run main NotActiveExceptionTest
 */

import java.io.NotActiveException;
import java.io.ObjectStreamException;

public class NotActiveExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNotActiveException() {
        NotActiveException e = new NotActiveException("m");
        check(e instanceof ObjectStreamException, "extends ObjectStreamException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new NotActiveException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testNotActiveException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
