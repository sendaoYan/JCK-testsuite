/*
 * @test
 * @summary java.net.DatagramPacket conformance: constructors and accessors.
 * @run main DatagramPacketTest
 */


public class DatagramPacketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        byte[] buf = new byte[16];
        java.net.DatagramPacket p = new java.net.DatagramPacket(buf, buf.length);
        check(p.getData().length == 16, "getData length");
        check(p.getLength() == 16, "getLength");
        check(p.getAddress() == null, "no address by default");
        check(p.getPort() == -1, "port -1 by default");
        java.net.InetAddress la = java.net.InetAddress.getLoopbackAddress();
        java.net.DatagramPacket p2 = new java.net.DatagramPacket(buf, 8, la, 9999);
        check(p2.getAddress().equals(la), "getAddress");
        check(p2.getPort() == 9999, "getPort");
        check(p2.getLength() == 8, "getLength");
        p2.setAddress(la); p2.setPort(1000); p2.setData(buf, 2, 6);
        check(p2.getLength() == 6, "setData length");
        check(p2.getPort() == 1000, "setPort");
        p2.setLength(4);
        check(p2.getLength() == 4, "setLength");        System.out.println("PASS (" + passed + " assertions)");
    }
}
