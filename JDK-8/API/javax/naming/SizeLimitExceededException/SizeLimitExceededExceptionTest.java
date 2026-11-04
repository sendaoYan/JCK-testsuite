/*
 * @test
 * @summary javax.naming.SizeLimitExceededException conformance: message and hierarchy.
 * @run main SizeLimitExceededExceptionTest
 */


public class SizeLimitExceededExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.SizeLimitExceededException e = new javax.naming.SizeLimitExceededException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.LimitExceededException, "is LimitExceededException");
        javax.naming.SizeLimitExceededException e0 = new javax.naming.SizeLimitExceededException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
