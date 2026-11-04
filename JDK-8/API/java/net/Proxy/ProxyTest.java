/*
 * @test
 * @summary java.net.Proxy conformance: type/address/NO_PROXY.
 * @run main ProxyTest
 */


public class ProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.Proxy none = java.net.Proxy.NO_PROXY;
        check(none.type() == java.net.Proxy.Type.DIRECT, "NO_PROXY type DIRECT");
        check(none.address() == null, "NO_PROXY address null");
        java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress("example.com", 80));
        check(p.type() == java.net.Proxy.Type.HTTP, "type");
        check(p.address() != null, "address");
        java.net.Proxy p2 = new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress("example.com", 80));
        check(p.equals(p2) && p.hashCode() == p2.hashCode(), "equals/hashCode");        System.out.println("PASS (" + passed + " assertions)");
    }
}
