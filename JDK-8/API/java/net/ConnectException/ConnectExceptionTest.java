/*
 * @test
 * @summary java.net.ConnectException conformance: message and hierarchy.
 * @run main ConnectExceptionTest
 */


public class ConnectExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.ConnectException e = new java.net.ConnectException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.net.SocketException, "is SocketException");
        java.net.ConnectException e0 = new java.net.ConnectException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
