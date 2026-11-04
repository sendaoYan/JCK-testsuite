/*
 * @test
 * @summary javax.security.auth.x500.X500Principal conformance: constructors and getters.
 * @run main X500PrincipalTest
 */

public class X500PrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.x500.X500Principal p = new javax.security.auth.x500.X500Principal("CN=Test, O=Org");
        check(p.getName().length() > 0, "getName");
        check(p.getName("RFC1779").length() > 0, "getName RFC1779");
        check(p.getName("RFC2253").length() > 0, "getName RFC2253");
        check(p.getEncoded() != null, "getEncoded");
        javax.security.auth.x500.X500Principal q = new javax.security.auth.x500.X500Principal("CN=Test, O=Org");
        check(p.equals(q), "equals");
        check(p.hashCode() == q.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
