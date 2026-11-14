/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipalLookupService conformance: hierarchy and methods.
 * @run main UserPrincipalLookupServiceTest
 */

import java.nio.file.attribute.UserPrincipalLookupService;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalNotFoundException;

public class UserPrincipalLookupServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean windowsHost() {
        return System.getProperty("os.name", "").toLowerCase().contains("windows");
    }

    static void testUserPrincipalLookupService() {
        check(Modifier.isAbstract(UserPrincipalLookupService.class.getModifiers()), "is abstract");

        Set<String> names = new HashSet<String>();
        for (Method m : UserPrincipalLookupService.class.getMethods()) names.add(m.getName());
        String[] required = { "lookupPrincipalByName", "lookupPrincipalByGroupName" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testUserPrincipalLookupService();
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
        check(java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService() == ls,
              "same service instance from the same filesystem");
        check(java.lang.reflect.Modifier.isAbstract(UserPrincipalLookupService.class.getModifiers()),
              "class is abstract (provider-implemented)");
        // name lookup contract
        UserPrincipal u = ls.lookupPrincipalByName(System.getProperty("user.name"));
        check(u != null, "lookupPrincipalByName(current user) non-null");
        check(u.getName() != null && !u.getName().isEmpty(),
              "name non-empty (Windows returns DOMAIN\\user, not user.name)");
        check(ls.lookupPrincipalByName(System.getProperty("user.name")).equals(u), "repeat lookup equal");
        // group lookup contract (posix-only: file group comes from the posix view)
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-ls");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            java.nio.file.FileStore st = java.nio.file.Files.getFileStore(f);
            if (st.supportsFileAttributeView("posix")) {
                String groupName = java.nio.file.Files.readAttributes(f, PosixFileAttributes.class).group().getName();
                GroupPrincipal g = ls.lookupPrincipalByGroupName(groupName);
                check(g != null, "lookupPrincipalByGroupName(file group) non-null");
                check(g.getName().equals(groupName), "group name round-trip");
                check(ls.lookupPrincipalByGroupName(groupName).equals(g), "repeat group lookup equal");
            } else {
                check(true, "group lookup skipped: no posix view on this fs");
            }
        } finally {
            cleanup(dir);
        }
        // unknown names -> UserPrincipalNotFoundException
        try {
            ls.lookupPrincipalByName("nosuchuser_a12");
            check(false, "unknown user lookup must throw");
        } catch (UserPrincipalNotFoundException e) {
            check("nosuchuser_a12".equals(e.getName()), "UPNFE carries requested name");
        }
        try {
            ls.lookupPrincipalByGroupName("nosuchgroup_a12");
            check(false, "unknown group lookup must throw");
        } catch (UserPrincipalNotFoundException e) {
            check("nosuchgroup_a12".equals(e.getName()), "UPNFE carries requested group name");
        }
        if (windowsHost()) {
            // LookupAccountName("") on Windows resolves the empty name to the
            // current user instead of reporting an unknown account; both a
            // returned principal and a UserPrincipalNotFoundException are legal.
            try {
                UserPrincipal empty = ls.lookupPrincipalByName("");
                check(empty != null, "Windows may resolve \"\" to a principal");
            } catch (UserPrincipalNotFoundException e) {
                check(true, "or report an empty name as unknown");
            }
        } else {
            try {
                ls.lookupPrincipalByName("");
                check(false, "empty user lookup must throw");
            } catch (UserPrincipalNotFoundException e) {
                check(true, "empty user name -> UserNFE");
            }
        }
        // null arguments -> NPE
        checkThrowsIO(NullPointerException.class, () -> ls.lookupPrincipalByName(null),
                "lookupPrincipalByName(null) NPE");
        checkThrowsIO(NullPointerException.class, () -> ls.lookupPrincipalByGroupName(null),
                "lookupPrincipalByGroupName(null) NPE");
        // user-defined implementations implement the two abstract methods and may throw IOException
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        UserPrincipalLookupService custom = new UserPrincipalLookupService() {
            public UserPrincipal lookupPrincipalByName(String name) throws java.io.IOException {
                calls.add("byName:" + name);
                return new UserPrincipal() {
                    public String getName() { return name; }
                    public String toString() { return name; }
                };
            }
            public GroupPrincipal lookupPrincipalByGroupName(String group) throws java.io.IOException {
                calls.add("byGroup:" + group);
                throw new java.io.IOException("boom");
            }
        };
        check(custom.lookupPrincipalByName("x").getName().equals("x"), "custom byName");
        check(calls.equals(java.util.Arrays.asList("byName:x")), "custom records calls");
        checkThrowsIO(java.io.IOException.class, () -> custom.lookupPrincipalByGroupName("y"),
                "custom byGroup propagates IOException");
        check(calls.size() == 2 && calls.get(1).equals("byGroup:y"), "custom records group call");
        check(ls.toString() != null, "service toString non-null");
    }

}
