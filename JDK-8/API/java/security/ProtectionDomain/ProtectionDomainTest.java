/*
 * @test
 * @summary java.security.ProtectionDomain conformance: full contract coverage.
 *          2-arg and 4-arg constructors, getCodeSource/getClassLoader/getPrincipals/
 *          getPermissions, implies(Permission), null codesource/permissions semantics,
 *          and toString.
 * @run main ProtectionDomainTest
 */

import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.ProtectionDomain;

public class ProtectionDomainTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        URL u = new URL("file:/tmp/x");
        CodeSource cs = new CodeSource(u, (java.security.cert.Certificate[]) null);
        Permissions perms = new Permissions();
        perms.add(new AllPermission());

        // ---- 2-arg constructor + getters ----
        ProtectionDomain pd = new ProtectionDomain(cs, perms);
        check(pd.getCodeSource() != null, "getCodeSource non-null");
        check(u.equals(pd.getCodeSource().getLocation()), "getCodeSource location");
        check(pd.getClassLoader() == null, "2-arg getClassLoader null");
        check(pd.getPrincipals() != null && pd.getPrincipals().length == 0,
                "2-arg getPrincipals empty");
        check(pd.getPermissions() != null, "2-arg getPermissions non-null");

        // ---- implies(Permission) driven by the permission collection ----
        check(pd.implies(new AllPermission()), "implies AllPermission true");
        check(pd.implies(new java.io.FilePermission("/tmp/x", "read")),
                "implies FilePermission true (AllPermission present)");

        // ---- 4-arg constructor ----
        ClassLoader cl = new ClassLoader() {};
        Principal p1 = new Principal() { public String getName() { return "p1"; } };
        ProtectionDomain pd4 = new ProtectionDomain(cs, perms, cl, new Principal[]{p1});
        check(pd4.getClassLoader() == cl, "4-arg getClassLoader");
        check(pd4.getPrincipals() != null && pd4.getPrincipals().length == 1,
                "4-arg getPrincipals length 1");
        check("p1".equals(pd4.getPrincipals()[0].getName()), "4-arg getPrincipals content");

        // ---- null codesource / permissions allowed (2-arg) ----
        ProtectionDomain pdNullCs = new ProtectionDomain(null, perms);
        check(pdNullCs.getCodeSource() == null, "null codesource -> getCodeSource null");
        ProtectionDomain pdNullPerms = new ProtectionDomain(cs, null);
        check(pdNullPerms.getPermissions() == null, "null permissions -> getPermissions null");

        // ---- toString non-empty ----
        check(pd.toString() != null && pd.toString().length() > 0, "toString non-empty");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
