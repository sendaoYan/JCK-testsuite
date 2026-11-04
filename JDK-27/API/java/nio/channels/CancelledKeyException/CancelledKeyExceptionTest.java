/*
 * @test
 * @summary java.nio.channels.CancelledKeyException conformance: white-box structure + black-box behavior.
 * @run main CancelledKeyExceptionTest
 */

import java.nio.channels.CancelledKeyException;

public class CancelledKeyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(CancelledKeyException.class), "hierarchy: extends IllegalStateException");
        check(new CancelledKeyException() != null, "no-arg constructor");
        check(new CancelledKeyException().getMessage() == null, "no-arg message is null");
        check(new CancelledKeyException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
            pipe.source().configureBlocking(false);
            java.nio.channels.SelectionKey key =
                    pipe.source().register(sel, java.nio.channels.SelectionKey.OP_READ);
            key.cancel();
            try {
                key.interestOps(java.nio.channels.SelectionKey.OP_READ);
                check(false, "ops on cancelled key should throw");
            } catch (CancelledKeyException e) {
                passed++;
            }
            sel.selectNow();
        } finally {
            try { pipe.sink().close(); } catch (Exception ignore) { }
            try { pipe.source().close(); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
