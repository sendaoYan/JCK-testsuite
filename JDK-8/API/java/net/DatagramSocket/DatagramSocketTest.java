/*
 * @test
 * @summary java.net.DatagramSocket conformance: bind/close on ephemeral port.
 * @run main DatagramSocketTest
 */


public class DatagramSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.DatagramSocket ds = new java.net.DatagramSocket();
        check(ds.isBound(), "isBound");
        check(ds.getLocalPort() >= 0, "getLocalPort");
        check(!ds.isClosed(), "not closed");
        ds.close();
        check(ds.isClosed(), "isClosed");        System.out.println("PASS (" + passed + " assertions)");
    }
}
