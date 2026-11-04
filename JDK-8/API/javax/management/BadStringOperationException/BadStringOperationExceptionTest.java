/*
 * @test
 * @summary javax.management.BadStringOperationException conformance: full
 *          contract coverage. operation-derived toString (getMessage stays
 *          null because the op is not passed to the superclass) and Exception
 *          hierarchy.
 * @run main BadStringOperationExceptionTest
 */

import javax.management.BadStringOperationException;

public class BadStringOperationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        BadStringOperationException x = new BadStringOperationException("-");
        check("BadStringOperationException: -".equals(x.toString()), "toString");
        check(x.getMessage() == null, "getMessage null (op not message)");
        check(x instanceof Exception, "extends Exception");
        check(x.getCause() == null, "no cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
