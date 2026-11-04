/*
 * @test
 * @summary java.nio.file.InvalidPathException conformance: input/reason/index.
 * @run main InvalidPathExceptionTest
 */

import java.nio.file.InvalidPathException;

public class InvalidPathExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInvalidPathException() {
        check(IllegalArgumentException.class.isAssignableFrom(InvalidPathException.class),
              "extends IllegalArgumentException");
        InvalidPathException e = new InvalidPathException("input", "reason", 3);
        check("input".equals(e.getInput()), "getInput");
        check("reason".equals(e.getReason()), "getReason");
        check(e.getIndex() == 3, "getIndex");
        check(e.getMessage() != null, "getMessage");
        InvalidPathException e2 = new InvalidPathException("input", "reason");
        check(e2.getIndex() == -1, "default index -1");
    }

    public static void main(String[] args) {
        testInvalidPathException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
