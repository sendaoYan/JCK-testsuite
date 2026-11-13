/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipal conformance: white-box structure + black-box behavior.
 * @run main UserPrincipalTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;

public class UserPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.UserPrincipal");
        check(c.isInterface(), "UserPrincipal is an interface");
        check(java.security.Principal.class.isAssignableFrom(c), "extends Principal");
        check(c.getDeclaredMethods().length == 0, "adds no methods beyond Principal");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("u.txt");
            Files.writeString(file, "u");
            UserPrincipalLookupService lookup =
                    dir.getFileSystem().getUserPrincipalLookupService();

            // lookup of the current user works and equals the file's owner
            String me = System.getProperty("user.name");
            UserPrincipal principal = lookup.lookupPrincipalByName(me);
            check(principal != null, "current user principal found");
            check(principal.getName() != null, "name non-null");
            UserPrincipal owner = Files.getOwner(file);
            check(owner.equals(principal), "file owner equals current user");

            // Principal face: toString/implies available
            check(principal.toString() != null, "toString non-null");
            check(!principal.implies(new javax.security.auth.Subject())
                  || true, "implies(Subject) callable");

            // principals are equal for equal names
            UserPrincipal again = lookup.lookupPrincipalByName(me);
            check(principal.equals(again) && principal.hashCode() == again.hashCode(),
                  "lookup deterministic");

            // a bogus name yields UserPrincipalNotFoundException
            boolean upnfe = false;
            try { lookup.lookupPrincipalByName("jck-no-such-user-xyz"); }
            catch (java.nio.file.attribute.UserPrincipalNotFoundException e) {
                upnfe = true;
            }
            check(upnfe, "unknown user -> UserPrincipalNotFoundException");
        } finally {
            Files.deleteIfExists(dir.resolve("u.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
        // The Windows provider may report the domain-qualified user form.
        String me = System.getProperty("user.name");
        boolean win = System.getProperty("os.name", "").toLowerCase().contains("windows");
        check(u.getName().equals(me)
              || (win && (u.getName().equalsIgnoreCase(me)
                          || u.getName().toLowerCase().endsWith("\\" + me.toLowerCase()))),
              "getName names the current user: " + u.getName());
        check(u.toString().equals(u.getName()), "toString is the principal name");
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
        // owner reported by the filesystem equals the looked-up principal
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-up");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            check(java.nio.file.Files.readAttributes(f, PosixFileAttributes.class).owner().equals(u),
                  "file owner equals looked-up principal");
            check(java.nio.file.Files.getOwner(f) instanceof UserPrincipal,
                  "Files.getOwner returns a UserPrincipal");
        } finally {
            cleanup(dir);
        }
    }

}
