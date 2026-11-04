/*
 * @test
 * @summary java.security.CodeSource conformance: constructors, getters, implies.
 * @run main CodeSourceTest
 */

public class CodeSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.URL u = new java.net.URL("file:/tmp/x");
        java.security.CodeSource c1 = new java.security.CodeSource(u, (java.security.cert.Certificate[]) null);
        check(u.equals(c1.getLocation()), "getLocation");
        check(c1.getCertificates() == null, "getCertificates null");
        java.security.CodeSource c2 = new java.security.CodeSource(u, new java.security.CodeSigner[0]);
        check(c2.getCodeSigners() != null, "getCodeSigners");
        check(c1.implies(c1), "implies self");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
