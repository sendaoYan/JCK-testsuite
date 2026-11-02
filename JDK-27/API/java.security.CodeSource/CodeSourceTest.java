/*
 * @test
 * @summary java.security.CodeSource conformance: constructor, getLocation, implies, equals/hashCode.
 * @run main CodeSourceTest
 */

import java.security.CodeSource;
import java.net.URL;

public class CodeSourceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        URL loc = new URL("http://example.com/app.jar");
        CodeSource cs = new CodeSource(loc, (java.security.cert.Certificate[]) null);
        check(loc.equals(cs.getLocation()), "getLocation");
        check(cs.getCertificates() == null, "getCertificates null");
        check(cs.getCodeSigners() == null, "getCodeSigners null");
        check(cs.implies(cs), "implies self");
        check(cs.equals(cs), "equals self");
        CodeSource cs2 = new CodeSource(loc, (java.security.cert.Certificate[]) null);
        check(cs.equals(cs2), "equals same location");
        check(cs.hashCode() == cs2.hashCode(), "hashCode equal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
