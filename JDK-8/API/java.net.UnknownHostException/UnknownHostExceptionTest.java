/*
 * @test
 * @summary java.net.UnknownHostException conformance: message and hierarchy.
 * @run main UnknownHostExceptionTest
 */


public class UnknownHostExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.UnknownHostException e = new java.net.UnknownHostException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.IOException, "is IOException");
        java.net.UnknownHostException e0 = new java.net.UnknownHostException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
