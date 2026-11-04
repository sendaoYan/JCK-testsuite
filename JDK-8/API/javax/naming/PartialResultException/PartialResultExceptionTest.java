/*
 * @test
 * @summary javax.naming.PartialResultException conformance: message and hierarchy.
 * @run main PartialResultExceptionTest
 */


public class PartialResultExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.PartialResultException e = new javax.naming.PartialResultException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.PartialResultException e0 = new javax.naming.PartialResultException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
