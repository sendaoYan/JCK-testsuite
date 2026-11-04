/*
 * @test
 * @summary java.net.Inet6Address conformance: loopback via bytes.
 * @run main Inet6AddressTest
 */


public class Inet6AddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        byte[] lb = new byte[16]; lb[15] = 1;
        java.net.Inet6Address a = (java.net.Inet6Address) java.net.InetAddress.getByAddress(lb);
        check(a.isLoopbackAddress(), "isLoopbackAddress");
        check("0:0:0:0:0:0:0:1".equals(a.getHostAddress()), "getHostAddress");
        check(a.getAddress().length == 16, "16 bytes");
        check(a instanceof java.net.InetAddress, "is InetAddress");        System.out.println("PASS (" + passed + " assertions)");
    }
}
