/*
 * @test
 * @summary javax.naming.TimeLimitExceededException conformance: message and hierarchy.
 * @run main TimeLimitExceededExceptionTest
 */


public class TimeLimitExceededExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.TimeLimitExceededException e = new javax.naming.TimeLimitExceededException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.LimitExceededException, "is LimitExceededException");
        javax.naming.TimeLimitExceededException e0 = new javax.naming.TimeLimitExceededException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
