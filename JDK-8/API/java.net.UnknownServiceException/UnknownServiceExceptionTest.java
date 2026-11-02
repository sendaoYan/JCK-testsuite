/*
 * @test
 * @summary java.net.UnknownServiceException conformance: message and hierarchy.
 * @run main UnknownServiceExceptionTest
 */


public class UnknownServiceExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.UnknownServiceException e = new java.net.UnknownServiceException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.IOException, "is IOException");
        java.net.UnknownServiceException e0 = new java.net.UnknownServiceException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
