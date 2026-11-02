/*
 * @test
 * @summary javax.naming.NamingException conformance: message and hierarchy.
 * @run main NamingExceptionTest
 */


public class NamingExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.NamingException e = new javax.naming.NamingException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.lang.Exception, "is Exception");
        check(e.getRemainingName() == null, "remainingName null");
        check(e.getRootCause() == null, "rootCause null");
        java.lang.Exception cause = new java.lang.Exception("cause");
        e.setRootCause(cause);
        check(e.getRootCause() == cause, "setRootCause");
        javax.naming.NamingException e0 = new javax.naming.NamingException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
