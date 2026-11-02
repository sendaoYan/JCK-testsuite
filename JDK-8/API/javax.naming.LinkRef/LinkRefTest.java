/*
 * @test
 * @summary javax.naming.LinkRef conformance: link name.
 * @run main LinkRefTest
 */


public class LinkRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.LinkRef lr = new javax.naming.LinkRef("cn=target");
        check("cn=target".equals(lr.getLinkName()), "getLinkName");
        check("javax.naming.LinkRef".equals(lr.getClassName()), "getClassName");
        javax.naming.LinkRef lr2 = new javax.naming.LinkRef(new javax.naming.CompositeName("a/b"));
        check("a/b".equals(lr2.getLinkName()), "getLinkName from Name");        System.out.println("PASS (" + passed + " assertions)");
    }
}
