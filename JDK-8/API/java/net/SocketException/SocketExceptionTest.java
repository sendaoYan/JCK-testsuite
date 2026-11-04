/*
 * @test
 * @summary java.net.SocketException conformance: message and hierarchy.
 * @run main SocketExceptionTest
 */


public class SocketExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.SocketException e = new java.net.SocketException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.IOException, "is IOException");
        java.net.SocketException e0 = new java.net.SocketException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
