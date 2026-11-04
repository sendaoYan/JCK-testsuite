/*
 * @test
 * @summary java.nio.channels.ClosedSelectorException conformance: white-box structure + black-box behavior.
 * @run main ClosedSelectorExceptionTest
 */

import java.nio.channels.ClosedSelectorException;

public class ClosedSelectorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(ClosedSelectorException.class), "hierarchy: extends IllegalStateException");
        check(new ClosedSelectorException() != null, "no-arg constructor");
        check(new ClosedSelectorException().getMessage() == null, "no-arg message is null");
        check(new ClosedSelectorException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.channels.Selector sel = java.nio.channels.Selector.open();
        sel.close();
        try {
            sel.keys();
            check(false, "keys() after close should throw");
        } catch (ClosedSelectorException e) {
            passed++;
        }
        try {
            sel.selectNow();
            check(false, "selectNow() after close should throw");
        } catch (ClosedSelectorException e) {
            passed++;
        } catch (java.io.IOException io) {
            check(false, "expected ClosedSelectorException, got " + io);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
