/*
 * @test
 * @summary java.security.ProtectionDomain conformance: constructors and accessors.
 * @run main ProtectionDomainTest
 */

import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.Principal;
import java.net.URL;

public class ProtectionDomainTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CodeSource cs = new CodeSource(new URL("http://example.com/"), (java.security.cert.Certificate[]) null);
        Permissions perms = new Permissions();
        ProtectionDomain pd = new ProtectionDomain(cs, perms);
        check(cs.equals(pd.getCodeSource()), "getCodeSource");
        check(perms.equals(pd.getPermissions()), "getPermissions");
        check(pd.getClassLoader() == null, "getClassLoader null");
        check(pd.getPrincipals().length == 0, "getPrincipals empty");
        ProtectionDomain pd2 = new ProtectionDomain(cs, perms, null, new Principal[0]);
        check(pd2.getPrincipals().length == 0, "getPrincipals empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
