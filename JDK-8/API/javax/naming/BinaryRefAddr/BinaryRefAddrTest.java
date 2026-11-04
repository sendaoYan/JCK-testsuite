/*
 * @test
 * @summary javax.naming.BinaryRefAddr conformance: type/content.
 * @run main BinaryRefAddrTest
 */


public class BinaryRefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.BinaryRefAddr a = new javax.naming.BinaryRefAddr("url", new byte[]{1, 2, 3});
        check("url".equals(a.getType()), "getType");
        byte[] c = (byte[]) a.getContent();
        check(c.length == 3 && c[0] == 1, "getContent");        System.out.println("PASS (" + passed + " assertions)");
    }
}
