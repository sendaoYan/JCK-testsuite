/*
 * @test
 * @summary javax.naming.StringRefAddr conformance: type/content/toString.
 * @run main StringRefAddrTest
 */


public class StringRefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.StringRefAddr a = new javax.naming.StringRefAddr("url", "http://x/");
        check("url".equals(a.getType()), "getType");
        check("http://x/".equals(a.getContent()), "getContent");
        check(a.toString() != null, "toString");        System.out.println("PASS (" + passed + " assertions)");
    }
}
