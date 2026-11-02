/*
 * @test
 * @summary java.net.PortUnreachableException conformance: message and hierarchy.
 * @run main PortUnreachableExceptionTest
 */


public class PortUnreachableExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.PortUnreachableException e = new java.net.PortUnreachableException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.net.SocketException, "is SocketException");
        java.net.PortUnreachableException e0 = new java.net.PortUnreachableException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
