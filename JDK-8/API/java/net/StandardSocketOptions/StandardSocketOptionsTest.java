/*
 * @test
 * @summary java.net.StandardSocketOptions conformance: option name fields.
 * @run main StandardSocketOptionsTest
 */


public class StandardSocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check("SO_KEEPALIVE".equals(java.net.StandardSocketOptions.SO_KEEPALIVE.name()), "SO_KEEPALIVE name");
        check("TCP_NODELAY".equals(java.net.StandardSocketOptions.TCP_NODELAY.name()), "TCP_NODELAY name");
        check(java.net.StandardSocketOptions.SO_RCVBUF != null, "SO_RCVBUF non-null");
        check(java.net.StandardSocketOptions.SO_SNDBUF.type().equals(Integer.class), "SO_SNDBUF type");
        check(java.net.StandardSocketOptions.SO_KEEPALIVE.type().equals(Boolean.class), "SO_KEEPALIVE type");        System.out.println("PASS (" + passed + " assertions)");
    }
}
