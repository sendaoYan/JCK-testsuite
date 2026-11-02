/*
 * @test
 * @summary java.net.Inet4Address conformance: loopback via bytes.
 * @run main Inet4AddressTest
 */


public class Inet4AddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.Inet4Address a = (java.net.Inet4Address) java.net.InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        check(a.isLoopbackAddress(), "isLoopbackAddress");
        check("127.0.0.1".equals(a.getHostAddress()), "getHostAddress");
        check(a.getAddress().length == 4, "4 bytes");
        check(a instanceof java.net.InetAddress, "is InetAddress");        System.out.println("PASS (" + passed + " assertions)");
    }
}
