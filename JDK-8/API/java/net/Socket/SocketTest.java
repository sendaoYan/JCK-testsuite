/*
 * @test
 * @summary java.net.Socket conformance: construct/close.
 * @run main SocketTest
 */


public class SocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.Socket s = new java.net.Socket();
        check(!s.isClosed(), "not closed after construct");
        check(!s.isConnected(), "not connected");
        s.close();
        check(s.isClosed(), "isClosed");
        java.net.Socket s2 = new java.net.Socket(java.net.Proxy.NO_PROXY);
        check(!s2.isConnected(), "unconnected via NO_PROXY");
        s2.close();        System.out.println("PASS (" + passed + " assertions)");
    }
}
