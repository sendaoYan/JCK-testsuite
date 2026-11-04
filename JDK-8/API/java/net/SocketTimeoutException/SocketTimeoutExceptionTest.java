/*
 * @test
 * @summary java.net.SocketTimeoutException conformance: message and hierarchy.
 * @run main SocketTimeoutExceptionTest
 */


public class SocketTimeoutExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.SocketTimeoutException e = new java.net.SocketTimeoutException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.InterruptedIOException, "is InterruptedIOException");
        check(e.bytesTransferred == 0, "bytesTransferred 0");
        java.net.SocketTimeoutException e0 = new java.net.SocketTimeoutException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
