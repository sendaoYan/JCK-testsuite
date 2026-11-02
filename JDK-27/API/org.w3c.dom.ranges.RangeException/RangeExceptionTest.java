/*
 * @test
 * @summary org.w3c.dom.ranges.RangeException conformance: superclass, constructor.
 * @run main RangeExceptionTest
 */

import org.w3c.dom.ranges.RangeException;

public class RangeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(RangeException.class),
              "extends RuntimeException");

        RangeException e = new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, "b");
        check(e.code == RangeException.BAD_BOUNDARYPOINTS_ERR, "code field");
        check("b".equals(e.getMessage()), "message");
        check(RangeException.INVALID_NODE_TYPE_ERR != RangeException.BAD_BOUNDARYPOINTS_ERR,
              "distinct error codes");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
