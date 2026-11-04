/*
 * @test
 * @summary org.w3c.dom.ranges.RangeException conformance: superclass, constructor, code field.
 * @run main RangeExceptionTest
 */

public class RangeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(org.w3c.dom.ranges.RangeException.class),
              "extends RuntimeException");
        check(org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR == org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR, "BAD_BOUNDARYPOINTS_ERR field");
        check(org.w3c.dom.ranges.RangeException.INVALID_NODE_TYPE_ERR == org.w3c.dom.ranges.RangeException.INVALID_NODE_TYPE_ERR, "INVALID_NODE_TYPE_ERR field");
        org.w3c.dom.ranges.RangeException e = new org.w3c.dom.ranges.RangeException(org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR, "m");
        check(e.code == org.w3c.dom.ranges.RangeException.BAD_BOUNDARYPOINTS_ERR, "code field");
        check("m".equals(e.getMessage()), "message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
