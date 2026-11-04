/*
 * @test
 * @summary javax.naming.NotContextException conformance: message and hierarchy.
 * @run main NotContextExceptionTest
 */


public class NotContextExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.NotContextException e = new javax.naming.NotContextException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.NotContextException e0 = new javax.naming.NotContextException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
