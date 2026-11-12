/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipal conformance: hierarchy and methods.
 * @run main UserPrincipalTest
 */

import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.Files;
import java.nio.file.Path;

public class UserPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUserPrincipal() {
        check(UserPrincipal.class.isInterface(), "is interface");
        check(Principal.class.isAssignableFrom(UserPrincipal.class), "extends Principal");
        Set<String> names = new HashSet<String>();
        for (Method m : UserPrincipal.class.getMethods()) names.add(m.getName());
        String[] required = { "getName" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testUserPrincipal();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===== A12 contract deepening (appended) =====
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    interface IORunnable { void run() throws java.io.IOException; }

    static void checkThrowsIO(Class<? extends Throwable> type, IORunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void cleanup(java.nio.file.Path p) {
        try {
            if (java.nio.file.Files.isDirectory(p)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                         java.nio.file.Files.newDirectoryStream(p)) {
                    for (java.nio.file.Path c : ds) cleanup(c);
                }
            }
            java.nio.file.Files.deleteIfExists(p);
        } catch (Exception ignore) { }
    }

    static void testContract() throws Exception {
        UserPrincipalLookupService ls = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
        check(ls != null, "default lookup service non-null");
        UserPrincipal u = ls.lookupPrincipalByName(System.getProperty("user.name"));
        check(u != null, "lookup of current user non-null");
        check(u.getName() != null && !u.getName().isEmpty(),
              "getName non-empty (Windows returns DOMAIN\\user, not user.name)");
        check(u.toString() != null, "toString non-null");
        check(u instanceof java.security.Principal, "is a Principal");
        check(!(u instanceof GroupPrincipal), "user principal is not a GroupPrincipal");
        // repeat lookups: equal values, distinct objects, consistent hashCode
        UserPrincipal u2 = ls.lookupPrincipalByName(System.getProperty("user.name"));
        check(u.equals(u2), "repeat lookup equal");
        check(u != u2, "repeat lookup distinct object");
        check(u.hashCode() == u2.hashCode(), "hashCode consistent across lookups");
        check(!u.equals(null), "equals null is false");
        check(!u.equals("user"), "equals foreign type is false");
        // Principal.implies default: false for null/empty subject, true when this principal is in the subject
        check(!u.implies(null), "implies(null) is false (Principal default)");
        javax.security.auth.Subject empty = new javax.security.auth.Subject();
        check(!u.implies(empty), "implies(empty subject) is false");
        java.util.Set<java.security.Principal> ps = new java.util.HashSet<java.security.Principal>();
        ps.add(u);
        javax.security.auth.Subject withSelf = new javax.security.auth.Subject(false, ps,
                new java.util.HashSet<Object>(), new java.util.HashSet<Object>());
        check(u.implies(withSelf), "implies(subject containing self) is true");
        // custom implementation carries the contract
        final UserPrincipal custom = new UserPrincipal() {
            public String getName() { return "custom-user"; }
            public String toString() { return "custom-user"; }
        };
        check("custom-user".equals(custom.getName()), "custom getName");
        check(custom.equals(custom), "custom equals self");
        // owner via Files.getOwner (owner view works on all providers; PosixFileAttributes
        // is Unix-only and would fail on Windows).
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-up");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            UserPrincipal owner = java.nio.file.Files.getOwner(f);
            check(owner instanceof UserPrincipal, "Files.getOwner returns a UserPrincipal");
            check(owner.getName() != null && !owner.getName().isEmpty(),
                  "file owner name non-empty");
        } finally {
            cleanup(dir);
        }
    }

}
