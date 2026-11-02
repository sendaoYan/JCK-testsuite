/*
 * @test
 * @summary java.net.BindException conformance: message and hierarchy.
 * @run main BindExceptionTest
 */


public class BindExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.BindException e = new java.net.BindException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.net.SocketException, "is SocketException");
        java.net.BindException e0 = new java.net.BindException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
