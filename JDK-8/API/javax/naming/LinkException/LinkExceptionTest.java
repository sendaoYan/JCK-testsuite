/*
 * @test
 * @summary javax.naming.LinkException conformance: message and hierarchy.
 * @run main LinkExceptionTest
 */


public class LinkExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.LinkException e = new javax.naming.LinkException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        check(e.getLinkRemainingName() == null, "linkRemainingName null");
        check(e.getLinkResolvedName() == null, "linkResolvedName null");
        check(e.getLinkResolvedObj() == null, "linkResolvedObj null");
        check(e.getLinkExplanation() == null, "linkExplanation null");
        javax.naming.LinkException e0 = new javax.naming.LinkException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
