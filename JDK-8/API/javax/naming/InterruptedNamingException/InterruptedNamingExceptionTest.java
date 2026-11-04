/*
 * @test
 * @summary javax.naming.InterruptedNamingException conformance: message and hierarchy.
 * @run main InterruptedNamingExceptionTest
 */


public class InterruptedNamingExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.InterruptedNamingException e = new javax.naming.InterruptedNamingException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.InterruptedNamingException e0 = new javax.naming.InterruptedNamingException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
