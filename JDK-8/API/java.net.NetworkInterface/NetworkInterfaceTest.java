/*
 * @test
 * @summary java.net.NetworkInterface conformance: enumerate/getByInetAddress.
 * @run main NetworkInterfaceTest
 */


public class NetworkInterfaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.Enumeration<java.net.NetworkInterface> ifs = java.net.NetworkInterface.getNetworkInterfaces();
        check(ifs != null, "getNetworkInterfaces non-null");
        java.net.NetworkInterface ni = java.net.NetworkInterface.getByInetAddress(java.net.InetAddress.getLoopbackAddress());
        check(ni != null, "loopback interface");
        check(ni.isLoopback(), "isLoopback");
        check(ni.getName() != null && ni.getName().length() > 0, "getName");        System.out.println("PASS (" + passed + " assertions)");
    }
}
