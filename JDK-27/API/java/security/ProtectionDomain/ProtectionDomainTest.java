/*
 * @test
 * @summary java.security.ProtectionDomain conformance: white-box structure + black-box behavior.
 * @run main ProtectionDomainTest
 */

import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.Principal;
import java.net.URL;
import java.security.*;
import java.util.*;

public class ProtectionDomainTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        CodeSource cs = new CodeSource(new URL("http://example.com/"), (java.security.cert.Certificate[]) null);
        Permissions perms = new Permissions();
        ProtectionDomain pd = new ProtectionDomain(cs, perms);
        check(cs.equals(pd.getCodeSource()), "getCodeSource");
        check(perms.equals(pd.getPermissions()), "getPermissions");
        check(pd.getClassLoader() == null, "getClassLoader null");
        check(pd.getPrincipals().length == 0, "getPrincipals empty");
        ProtectionDomain pd2 = new ProtectionDomain(cs, perms, null, new Principal[0]);
        check(pd2.getPrincipals().length == 0, "getPrincipals empty");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        CodeSource cs = new CodeSource(new java.net.URL("file:/tmp/jck.jar"), (CodeSigner[]) null);
        // All-permissions domain implies everything
        Permissions all = new Permissions();
        all.add(new AllPermission());
        ProtectionDomain pdAll = new ProtectionDomain(cs, all);
        check(pdAll.getCodeSource() == cs, "codeSource");
        check(pdAll.getPermissions() == all, "permissions");
        check(pdAll.implies(new SecurityPermission("any")), "implies all");
        // Static permissions domain with one permission implies only that permission's cone
        Permissions one = new Permissions();
        one.add(new SecurityPermission("jck.*"));
        ProtectionDomain pdOne = new ProtectionDomain(cs, one);
        check(pdOne.implies(new SecurityPermission("jck.a")), "implies member");
        check(!pdOne.implies(new SecurityPermission("other")), "no implies");
        check(!pdOne.implies(new AllPermission()), "no implies AllPermission");
        // Domain with no permissions implies nothing
        ProtectionDomain pdNone = new ProtectionDomain(cs, new Permissions());
        check(!pdNone.implies(new SecurityPermission("x")), "empty implies nothing");
        // Null CodeSource and null permissions allowed; implies is then false
        ProtectionDomain pdNull = new ProtectionDomain(null, null);
        check(pdNull.getCodeSource() == null && pdNull.getPermissions() == null, "null fields");
        check(!pdNull.implies(new SecurityPermission("x")), "null-perms implies false");
        // ClassLoader-aware constructor retains loader and principals
        ClassLoader cl = ProtectionDomain.class.getClassLoader();
        Principal[] principals = { new javax.security.auth.x500.X500Principal("CN=jck") };
        ProtectionDomain pdFull = new ProtectionDomain(cs, all, cl, principals);
        check(pdFull.getClassLoader() == cl, "class loader retained");
        check(java.util.Arrays.equals(pdFull.getPrincipals(), principals), "principals retained");
        // getPrincipals returns defensive copy
        principals[0] = null;
        check(pdFull.getPrincipals()[0] != null, "principals defensive copy");
        // static implies with an empty PermissionCollection is still false
        check(!pdNull.implies(new AllPermission()), "static implies false");
        // toString non-null
        check(pdFull.toString() != null && !pdFull.toString().isEmpty(), "toString");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
