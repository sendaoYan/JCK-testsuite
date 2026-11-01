/*
 * @test
 * @summary java.io.SyncFailedException API conformance: constructors and inheritance.
 * @run main SyncFailedExceptionTest
 */

import java.io.IOException;
import java.io.SyncFailedException;

public class SyncFailedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSyncFailedException() {
        SyncFailedException e = new SyncFailedException("m");
        check(e instanceof IOException, "extends IOException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testSyncFailedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
