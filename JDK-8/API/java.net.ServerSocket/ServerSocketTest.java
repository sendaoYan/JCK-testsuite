/*
 * @test
 * @summary java.net.ServerSocket conformance: ephemeral bind/close.
 * @run main ServerSocketTest
 */


public class ServerSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.ServerSocket ss = new java.net.ServerSocket(0);
        check(ss.isBound(), "isBound");
        check(ss.getLocalPort() > 0, "getLocalPort > 0");
        ss.close();
        check(ss.isClosed(), "isClosed");        System.out.println("PASS (" + passed + " assertions)");
    }
}
