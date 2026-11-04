/*
 * @test
 * @summary org.ietf.jgss.Oid conformance: constructors, toString, equals, getDER.
 * @run main OidTest
 */

public class OidTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.ietf.jgss.Oid oid = new org.ietf.jgss.Oid("1.2.3.4");
        check("1.2.3.4".equals(oid.toString()), "toString");
        check(oid.equals(new org.ietf.jgss.Oid("1.2.3.4")), "equals");
        check(oid.containedIn(new org.ietf.jgss.Oid[]{new org.ietf.jgss.Oid("1.2.3.4")}), "containedIn");
        check(oid.getDER() != null, "getDER");
        org.ietf.jgss.Oid oid2 = new org.ietf.jgss.Oid(new byte[]{0x06, 0x03, 0x2A, 0x03, 0x04});
        check("1.2.3.4".equals(oid2.toString()), "byte[] ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
