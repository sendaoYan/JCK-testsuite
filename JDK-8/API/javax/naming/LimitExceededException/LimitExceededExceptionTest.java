/*
 * @test
 * @summary javax.naming.LimitExceededException conformance: message and hierarchy.
 * @run main LimitExceededExceptionTest
 */


public class LimitExceededExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.LimitExceededException e = new javax.naming.LimitExceededException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.LimitExceededException e0 = new javax.naming.LimitExceededException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
