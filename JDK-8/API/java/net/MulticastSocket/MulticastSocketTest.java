/*
 * @test
 * @summary java.net.MulticastSocket conformance: bind/close.
 * @run main MulticastSocketTest
 */


public class MulticastSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.MulticastSocket ms = new java.net.MulticastSocket();
        check(ms.isBound(), "isBound");
        check(!ms.isClosed(), "not closed");
        ms.close();
        check(ms.isClosed(), "isClosed");        System.out.println("PASS (" + passed + " assertions)");
    }
}
