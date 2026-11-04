/*
 * @test
 * @summary jdk.net.Sockets conformance: socket options helpers.
 * @run main SocketsTest
 */

public class SocketsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        java.net.Socket s = new java.net.Socket();
        jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.TCP_NODELAY, true);
        check(java.lang.Boolean.TRUE.equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.TCP_NODELAY)), "setOption/getOption");
        java.util.Set<java.net.SocketOption<?>> opts = jdk.net.Sockets.supportedOptions(java.net.Socket.class);
        check(opts != null, "supportedOptions");
        check(opts.contains(java.net.StandardSocketOptions.TCP_NODELAY), "contains TCP_NODELAY");
        s.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
