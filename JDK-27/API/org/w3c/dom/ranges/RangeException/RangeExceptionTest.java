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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(RuntimeException.class.isAssignableFrom(RangeException.class),
              "extends RuntimeException");

        RangeException e = new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, "b");
        check(e.code == RangeException.BAD_BOUNDARYPOINTS_ERR, "code field");
        check("b".equals(e.getMessage()), "message");
        check(RangeException.INVALID_NODE_TYPE_ERR != RangeException.BAD_BOUNDARYPOINTS_ERR,
              "distinct error codes");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(RangeException.BAD_BOUNDARYPOINTS_ERR == 1, "BAD_BOUNDARYPOINTS_ERR == 1");
        check(RangeException.INVALID_NODE_TYPE_ERR == 2, "INVALID_NODE_TYPE_ERR == 2");
        org.w3c.dom.ranges.RangeException e = new org.w3c.dom.ranges.RangeException((short) 3, "msg");
        check(e.code == (short) 3, "code field set");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
