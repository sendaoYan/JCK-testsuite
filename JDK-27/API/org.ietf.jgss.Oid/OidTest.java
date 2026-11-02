/*
 * @test
 * @summary org.ietf.jgss.Oid conformance: OID string DER and containment.
 * @run main OidTest
 */

import org.ietf.jgss.*;

public class OidTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Oid oid = new Oid("1.2.840.113554.1.2.2");
        check("1.2.840.113554.1.2.2".equals(oid.toString()), "toString");
        check(oid.getDER().length > 0, "getDER non-empty");
        check(oid.equals(new Oid("1.2.840.113554.1.2.2")), "equals");
        check(oid.hashCode() == new Oid("1.2.840.113554.1.2.2").hashCode(), "hashCode");
        check(oid.containedIn(new Oid[]{new Oid("1.2.840.113554.1.2.2")}), "containedIn");
        check(new Oid(new byte[]{6, 9, 42, -122, 72, -122, -9, 13, 1, 2, 2}).getDER().length > 0, "byte[] ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
