/*
 * @test
 * @summary java.net.InterfaceAddress conformance: address/prefix via loopback.
 * @run main InterfaceAddressTest
 */


public class InterfaceAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.NetworkInterface ni = java.net.NetworkInterface.getByInetAddress(java.net.InetAddress.getLoopbackAddress());
        check(ni != null, "loopback interface present");
        boolean found = false;
        for (java.net.InterfaceAddress ia : ni.getInterfaceAddresses()) {
            check(ia.getAddress() != null, "interface has address");
            check(ia.getNetworkPrefixLength() >= 0, "prefix length");
            found = true;
        }
        check(found, "at least one interface address");        System.out.println("PASS (" + passed + " assertions)");
    }
}
