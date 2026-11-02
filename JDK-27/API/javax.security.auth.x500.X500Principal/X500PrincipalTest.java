/*
 * @test
 * @summary javax.security.auth.x500.X500Principal conformance: X500 principal names and encoding.
 * @run main X500PrincipalTest
 */

import javax.security.auth.x500.*;
import java.util.HashMap;

public class X500PrincipalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        X500Principal p = new X500Principal("CN=Alice");
        check("CN=Alice".equals(p.getName()), "getName");
        check("CN=Alice".equals(p.getName(X500Principal.RFC2253)), "getName RFC2253");
        check(p.getEncoded().length > 0, "getEncoded non-empty");
        check(p.equals(new X500Principal("CN=Alice")), "equals");
        check(p.hashCode() == new X500Principal("CN=Alice").hashCode(), "hashCode");
        check(new X500Principal(p.getEncoded()).getName().equals("CN=Alice"), "byte[] ctor");
        check(p.toString().contains("CN=Alice"), "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
