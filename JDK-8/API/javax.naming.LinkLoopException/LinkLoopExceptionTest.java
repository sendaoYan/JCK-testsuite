/*
 * @test
 * @summary javax.naming.LinkLoopException conformance: message and hierarchy.
 * @run main LinkLoopExceptionTest
 */


public class LinkLoopExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.LinkLoopException e = new javax.naming.LinkLoopException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.LinkException, "is LinkException");
        javax.naming.LinkLoopException e0 = new javax.naming.LinkLoopException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
