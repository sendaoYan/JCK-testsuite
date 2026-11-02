/*
 * @test
 * @summary java.net.InetSocketAddress conformance: port/host/unresolved.
 * @run main InetSocketAddressTest
 */


public class InetSocketAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.InetSocketAddress a = new java.net.InetSocketAddress(80);
        check(a.getPort() == 80, "getPort");
        check(!a.isUnresolved(), "isUnresolved false");
        java.net.InetSocketAddress b = new java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), 8080);
        check(b.getPort() == 8080, "getPort loopback");
        check(b.getAddress().isLoopbackAddress(), "getAddress");
        java.net.InetSocketAddress u = java.net.InetSocketAddress.createUnresolved("host", 443);
        check(u.isUnresolved(), "createUnresolved");
        check("host".equals(u.getHostName()), "getHostName");        System.out.println("PASS (" + passed + " assertions)");
    }
}
