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
import java.security.SecurityPermission;

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

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: staticPermissionsOnly, identity equality, implies matrix,
    //      constructor ownership/freezing, removed 5-arg constructor (JEP 486) ----
    static void testContractDeep() throws Exception {
        URL u = new URL("file:/tmp/x");
        CodeSource cs = new CodeSource(u, (java.security.cert.Certificate[]) null);
        Permissions all = new Permissions();
        all.add(new AllPermission());
        Permissions none = new Permissions();
        Principal p1 = new Principal() { public String getName() { return "p1"; } };
        Principal p2 = new Principal() { public String getName() { return "p2"; } };

        // constructor count: the 5-arg (…, boolean staticPermissionsOnly) ctor was REMOVED
        // by JEP 486; only the 2-arg and 4-arg remain
        check(ProtectionDomain.class.getConstructors().length == 2, "exactly 2 public constructors (5-arg removed)");

        // staticPermissionsOnly(): 2-arg domain is static-only, 4-arg domain is dynamic
        ProtectionDomain stat = new ProtectionDomain(null, all);
        ProtectionDomain dyn = new ProtectionDomain(null, all, null, new Principal[0]);
        check(stat.staticPermissionsOnly(), "2-arg ctor -> staticPermissionsOnly true");
        check(!dyn.staticPermissionsOnly(), "4-arg ctor -> staticPermissionsOnly false");

        // getPermissions returns the very collection handed to the constructor
        check(stat.getPermissions() == all, "getPermissions identity");

        // equals is IDENTITY (not overridden)
        ProtectionDomain a = new ProtectionDomain(cs, all, null, new Principal[]{p1});
        ProtectionDomain b = new ProtectionDomain(cs, all, null, new Principal[]{p1});
        check(!a.equals(b), "identity equals: same construction not equal");
        check(a.equals(a), "identity equals reflexive");
        check(!a.equals(null), "equals null false");
        check(a.hashCode() == a.hashCode(), "hashCode self-consistent");

        // implies matrix driven by the static collection
        ProtectionDomain locked = new ProtectionDomain(null, none);
        check(stat.implies(new SecurityPermission("x")), "AllPermission domain implies");
        check(!locked.implies(new SecurityPermission("x")), "empty-perms domain implies nothing");
        check(!locked.implies(new java.io.FilePermission("*", "read")), "empty domain implies nothing for FilePermission");
        try {
            locked.implies(null);
            check(false, "implies(null) must throw");
        } catch (NullPointerException npe) {
            check(true, "implies(null) -> NPE via collection");
        }
        // null collection: no collection to consult, so implies never throws and grants nothing
        ProtectionDomain nullPerms = new ProtectionDomain(null, null);
        check(nullPerms.getPermissions() == null, "null perms getPermissions null");
        check(!nullPerms.implies(new SecurityPermission("x")), "null perms implies false");
        check(!nullPerms.implies(null), "null perms implies(null) false (no NPE, no collection)");

        // principals array defensive copy + fresh copy per getPrincipals call
        Principal[] arr = { p1 };
        ProtectionDomain pdArr = new ProtectionDomain(null, none, null, arr);
        arr[0] = p2;
        check("p1".equals(pdArr.getPrincipals()[0].getName()), "principals defensive copy on construction");
        check(pdArr.getPrincipals() != pdArr.getPrincipals(), "getPrincipals fresh array per call");
        pdArr.getPrincipals()[0] = p2;
        check("p1".equals(pdArr.getPrincipals()[0].getName()), "getPrincipals callers cannot mutate state");

        // empty principal array behaves like none
        ProtectionDomain emptyPrs = new ProtectionDomain(null, none, null, new Principal[0]);
        check(emptyPrs.getPrincipals().length == 0, "empty principal array preserved length 0");
        check(!emptyPrs.staticPermissionsOnly(), "4-arg empty principals still dynamic");

        // toString: contains location and principal name
        String ts = new ProtectionDomain(cs, none, null, new Principal[]{p1}).toString();
        check(ts.contains("file:/tmp/x"), "toString contains location");
        check(ts.contains("p1"), "toString contains principal name");

        // the constructor takes ownership: the passed collection is frozen read-only
        // immediately at CONSTRUCTION (no implies call needed)
        Permissions shared = new Permissions();
        shared.add(new SecurityPermission("before"));
        ProtectionDomain pdShared = new ProtectionDomain(null, shared);
        check(shared.isReadOnly(), "collection frozen at construction");
        check(pdShared.implies(new SecurityPermission("before")), "sees initial permission");
        try {
            shared.add(new SecurityPermission("after"));
            check(false, "frozen collection must reject add");
        } catch (SecurityException se) {
            check("attempt to add a Permission to a readonly Permissions object".equals(se.getMessage()),
                    "frozen collection add -> SecurityException with exact message");
        }

        // ProtectionDomain is NOT Serializable (CodeSource is, PD is not)
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            new java.io.ObjectOutputStream(bos).writeObject(stat);
            check(false, "ProtectionDomain must not be serializable");
        } catch (java.io.NotSerializableException nse) {
            check(true, "ProtectionDomain not serializable");
        }
    }
}
