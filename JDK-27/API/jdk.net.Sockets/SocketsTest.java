/*
 * @test
 * @summary jdk.net.Sockets conformance: sockets options.
 * @run main SocketsTest
 */

public class SocketsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        java.net.Socket s = new java.net.Socket();
        jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_REUSEADDR, true);
        check(Boolean.TRUE.equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_REUSEADDR)), "set/get option");
        check(jdk.net.Sockets.supportedOptions(java.net.Socket.class) != null, "supportedOptions");
        s.close();
        java.net.ServerSocket ss = new java.net.ServerSocket(0);
        jdk.net.Sockets.setOption(ss, java.net.StandardSocketOptions.SO_REUSEADDR, true);
        check(Boolean.TRUE.equals(jdk.net.Sockets.getOption(ss, java.net.StandardSocketOptions.SO_REUSEADDR)), "server set/get option");
        ss.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
