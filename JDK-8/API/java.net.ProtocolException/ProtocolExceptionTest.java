/*
 * @test
 * @summary java.net.ProtocolException conformance: message and hierarchy.
 * @run main ProtocolExceptionTest
 */


public class ProtocolExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.ProtocolException e = new java.net.ProtocolException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.IOException, "is IOException");
        java.net.ProtocolException e0 = new java.net.ProtocolException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
