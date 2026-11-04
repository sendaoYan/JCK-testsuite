/*
 * @test
 * @summary java.net.NoRouteToHostException conformance: message and hierarchy.
 * @run main NoRouteToHostExceptionTest
 */


public class NoRouteToHostExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.NoRouteToHostException e = new java.net.NoRouteToHostException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.net.SocketException, "is SocketException");
        java.net.NoRouteToHostException e0 = new java.net.NoRouteToHostException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
