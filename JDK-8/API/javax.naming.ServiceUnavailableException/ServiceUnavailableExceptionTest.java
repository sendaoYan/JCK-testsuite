/*
 * @test
 * @summary javax.naming.ServiceUnavailableException conformance: message and hierarchy.
 * @run main ServiceUnavailableExceptionTest
 */


public class ServiceUnavailableExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ServiceUnavailableException e = new javax.naming.ServiceUnavailableException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.ServiceUnavailableException e0 = new javax.naming.ServiceUnavailableException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
