/*
 * @test
 * @summary java.net.InetAddress conformance: localhost/loopback/local.
 * @run main InetAddressTest
 */


public class InetAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.InetAddress local = java.net.InetAddress.getByName("localhost");
        check(local != null, "getByName localhost");
        check(local.isLoopbackAddress(), "localhost is loopback");
        java.net.InetAddress loop = java.net.InetAddress.getLoopbackAddress();
        check(loop.isLoopbackAddress(), "getLoopbackAddress");
        check(loop.getHostAddress() != null, "getHostAddress non-null");
        check(loop.getAddress().length == 4 || loop.getAddress().length == 16, "address length 4 or 16");
        check(local.equals(loop) || "localhost".equals(local.getHostName()), "host name");        System.out.println("PASS (" + passed + " assertions)");
    }
}
