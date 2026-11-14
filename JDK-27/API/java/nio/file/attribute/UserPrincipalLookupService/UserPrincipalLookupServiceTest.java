/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipalLookupService conformance: white-box structure + black-box behavior.
 * @run main UserPrincipalLookupServiceTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.PosixFileAttributes;
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

    /** The Windows provider may report the qualified DOMAIN\\user form. */
    static boolean namesCurrentUser(String name) {
        String me = System.getProperty("user.name");
        return name.equals(me)
            || (windowsHost() && (name.equalsIgnoreCase(me)
                                  || name.toLowerCase().endsWith("\\" + me.toLowerCase())));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.UserPrincipalLookupService");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "UserPrincipalLookupService is abstract");
        check(c.getDeclaredConstructors().length == 0
              || java.lang.reflect.Modifier.isProtected(
                     c.getDeclaredConstructors()[0].getModifiers()),
              "no public constructors");
        for (String n : new String[] {"lookupPrincipalByName", "lookupPrincipalByGroupName"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            UserPrincipalLookupService lookup =
                    dir.getFileSystem().getUserPrincipalLookupService();
            check(lookup != null, "default fs provides the service");

            // user lookup: current user, deterministic identity
            String me = System.getProperty("user.name");
            var u1 = lookup.lookupPrincipalByName(me);
            var u2 = lookup.lookupPrincipalByName(me);
            check(u1 != null && u1.equals(u2), "user lookup deterministic");
            check(namesCurrentUser(u1.getName()), "name preserved: " + u1.getName());

            // group lookup: on a POSIX provider the user's primary group is
            // discoverable through file metadata; Windows has no posix view.
            if (dir.getFileSystem().supportedFileAttributeViews().contains("posix")) {
                Path probe = dir.resolve("probe");
                Files.writeString(probe, "p");
                GroupPrincipal group = Files.readAttributes(probe,
                        java.nio.file.attribute.PosixFileAttributes.class).group();
                GroupPrincipal found = lookup.lookupPrincipalByGroupName(group.getName());
                check(found != null && found.equals(group), "group lookup round trip");
            } else {
                System.out.println("NOTE: no posix view on this provider; group round-trip skipped");
            }

            // error cases
            boolean upnfe = false;
            try { lookup.lookupPrincipalByName("jck-ghost-user"); }
            catch (java.nio.file.attribute.UserPrincipalNotFoundException e) { upnfe = true; }
            check(upnfe, "unknown user -> UserPrincipalNotFoundException");
            boolean gnf = false;
            try { lookup.lookupPrincipalByGroupName("jck-ghost-group"); }
            catch (java.nio.file.attribute.UserPrincipalNotFoundException e) { gnf = true; }
            check(gnf, "unknown group -> UserPrincipalNotFoundException");

            // null names are rejected outright
            boolean npe = false;
            try { lookup.lookupPrincipalByName(null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "null user name -> NullPointerException");
            boolean npe2 = false;
            try { lookup.lookupPrincipalByGroupName(null); }
            catch (NullPointerException e) { npe2 = true; }
            check(npe2, "null group name -> NullPointerException");
        } finally {
            Files.deleteIfExists(dir.resolve("probe"));
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
        check(java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService() == ls,
              "same service instance from the same filesystem");
        check(java.lang.reflect.Modifier.isAbstract(UserPrincipalLookupService.class.getModifiers()),
              "class is abstract (provider-implemented)");
        // name lookup contract
        UserPrincipal u = ls.lookupPrincipalByName(System.getProperty("user.name"));
        check(u != null, "lookupPrincipalByName(current user) non-null");
        check(namesCurrentUser(u.getName()), "name round-trip: " + u.getName());
        check(ls.lookupPrincipalByName(System.getProperty("user.name")).equals(u), "repeat lookup equal");
        // group lookup contract (POSIX file metadata only)
        if (java.nio.file.FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-ls");
            try {
                java.nio.file.Path f = dir.resolve("f");
                java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
                String groupName = java.nio.file.Files.readAttributes(f, PosixFileAttributes.class).group().getName();
                GroupPrincipal g = ls.lookupPrincipalByGroupName(groupName);
                check(g != null, "lookupPrincipalByGroupName(file group) non-null");
                check(g.getName().equals(groupName), "group name round-trip");
                check(ls.lookupPrincipalByGroupName(groupName).equals(g), "repeat group lookup equal");
            } finally {
                cleanup(dir);
            }
        }
        // unknown names -> UserPrincipalNotFoundException
        try {
            ls.lookupPrincipalByName("nosuchuser_a12");
            check(false, "unknown user lookup must throw");
        } catch (UserPrincipalNotFoundException e) {
            // JDK-25+ throws UPNFE with an empty name (probed on 25/26/27); JDK-8 preserved the name.
                // JDK-25+ normally leaves the name empty; a provider may still
                // preserve the requested name (both are legal).
                check(e.getName() == null || e.getName().isEmpty()
                      || e.getName().equals("nosuchuser_a12"),
                      "UPNFE name is empty or the requested name: " + e.getName());
        }
        try {
            ls.lookupPrincipalByGroupName("nosuchgroup_a12");
            check(false, "unknown group lookup must throw");
        } catch (UserPrincipalNotFoundException e) {
            // JDK-25+ throws UPNFE with an empty name (probed on 25/26/27); JDK-8 preserved the name.
                check(e.getName() == null || e.getName().isEmpty()
                      || e.getName().equals("nosuchgroup_a12"),
                      "UPNFE name is empty or the requested name: " + e.getName());
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
