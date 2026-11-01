/*
 * @test
 * @summary java.util.prefs.BackingStoreException conformance: constructors/hierarchy.
 * @run main BackingStoreExceptionTest
 */

import java.util.prefs.BackingStoreException;

public class BackingStoreExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBackingStoreException() {
        BackingStoreException e = new BackingStoreException("msg");
        check(e instanceof Exception, "is Exception");
        check("msg".equals(e.getMessage()), "message");
        BackingStoreException e2 = new BackingStoreException(new RuntimeException("c"));
        check(e2.getCause() instanceof RuntimeException, "cause");
    }

    public static void main(String[] args) {
        testBackingStoreException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
