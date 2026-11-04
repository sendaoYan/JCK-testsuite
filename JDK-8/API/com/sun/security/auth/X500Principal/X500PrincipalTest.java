/*
 * @test
 * @summary com.sun.security.auth.X500Principal conformance: behavior of constructors, accessors and value semantics.
 * @run main X500PrincipalTest
 */

public class X500PrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.auth.X500Principal p = new com.sun.security.auth.X500Principal("CN=Alice");
        check(p instanceof java.security.Principal, "implements java.security.Principal");
        check(p.getName() != null, "getName");
        check(p.toString() != null, "toString");
        check(p.equals(new com.sun.security.auth.X500Principal("CN=Alice")), "equals");
        check(p.hashCode() == new com.sun.security.auth.X500Principal("CN=Alice").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
