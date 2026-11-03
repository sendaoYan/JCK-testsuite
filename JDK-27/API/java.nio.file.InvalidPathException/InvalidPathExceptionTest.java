/*
 * @test
 * @summary java.nio.file.InvalidPathException conformance: white-box structure + black-box behavior.
 * @run main InvalidPathExceptionTest
 */

import java.nio.file.InvalidPathException;

public class InvalidPathExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(InvalidPathException.class), "hierarchy: extends IllegalArgumentException");

        InvalidPathException e = new InvalidPathException("in put", "reason", 3);
        check("in put".equals(e.getInput()), "getInput");
        check("reason".equals(e.getReason()), "getReason");
        check(e.getIndex() == 3, "getIndex");
        check(e.getMessage().contains("reason"), "message has reason");
        InvalidPathException e2 = new InvalidPathException("x", "why");
        check(e2.getIndex() == -1, "no-index is -1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // embedded NUL is rejected with the offending index
        boolean threw = false;
        try { java.nio.file.Paths.get("bad\u0000path"); }
        catch (InvalidPathException e) {
            threw = true;
            check(e.getIndex() == -1 || e.getIndex() == 3, "index of NUL reported");
            check(e.getInput().equals("bad\u0000path"), "input echoed");
            check(e.getReason() != null, "reason present");
        }
        check(threw, "NUL byte -> InvalidPathException");
        // valid paths do not throw
        check(java.nio.file.Paths.get("plain.txt") != null, "valid path accepted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
