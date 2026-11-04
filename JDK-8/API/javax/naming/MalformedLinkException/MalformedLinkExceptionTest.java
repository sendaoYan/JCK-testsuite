/*
 * @test
 * @summary javax.naming.MalformedLinkException conformance: message and hierarchy.
 * @run main MalformedLinkExceptionTest
 */


public class MalformedLinkExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.MalformedLinkException e = new javax.naming.MalformedLinkException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.LinkException, "is LinkException");
        javax.naming.MalformedLinkException e0 = new javax.naming.MalformedLinkException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
