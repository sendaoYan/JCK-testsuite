/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.attribute.GroupPrincipal conformance: hierarchy and methods.
 * @run main GroupPrincipalTest
 */

import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.UserPrincipalNotFoundException;

public class GroupPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGroupPrincipal() {
        check(GroupPrincipal.class.isInterface(), "is interface");
        check(UserPrincipal.class.isAssignableFrom(GroupPrincipal.class), "extends UserPrincipal");
        Set<String> names = new HashSet<String>();
        for (Method m : GroupPrincipal.class.getMethods()) names.add(m.getName());
        String[] required = { "getName" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testGroupPrincipal();
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
        check(UserPrincipal.class.isAssignableFrom(GroupPrincipal.class), "extends UserPrincipal");
        check(java.security.Principal.class.isAssignableFrom(GroupPrincipal.class),
              "extends Principal transitively");
        check(GroupPrincipal.class.getTypeParameters().length == 0, "not generic");
        UserPrincipalLookupService ls = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-gp");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            // discover the current group from the file's own attributes, then look it up
            GroupPrincipal fileGroup = java.nio.file.Files.readAttributes(f, PosixFileAttributes.class).group();
            GroupPrincipal g = ls.lookupPrincipalByGroupName(fileGroup.getName());
            check(g != null, "group lookup non-null");
            check(g.getName().equals(fileGroup.getName()), "group getName round-trip");
            check(g instanceof GroupPrincipal, "is a GroupPrincipal");
            check(g instanceof UserPrincipal, "is a UserPrincipal");
            check(g instanceof java.security.Principal, "is a Principal");
            check(g.toString().equals(g.getName()), "toString is the name");
            GroupPrincipal g2 = ls.lookupPrincipalByGroupName(fileGroup.getName());
            check(g.equals(g2), "repeat group lookup equal");
            check(g.hashCode() == g2.hashCode(), "group hashCode consistent");
            // user and group principals are distinct types even when the names coincide (Unix impl)
            UserPrincipal u = ls.lookupPrincipalByName(System.getProperty("user.name"));
            check(!u.equals(g), "user principal not equal to group principal");
            check(!g.equals(u), "group principal not equal to user principal (symmetry)");
            // a group principal is usable wherever a UserPrincipal is expected
            UserPrincipal asUP = g;
            check(asUP.getName().equals(g.getName()), "group as UserPrincipal");
            // setGroup with the looked-up group round-trips through the view
            PosixFileAttributeView pv = java.nio.file.Files.getFileAttributeView(f, PosixFileAttributeView.class);
            pv.setGroup(g);
            check(pv.readAttributes().group().equals(g), "setGroup with looked-up group");
            // custom GroupPrincipal implementation is implementable, but foreign to the provider
            final GroupPrincipal custom = new GroupPrincipal() {
                public String getName() { return g.getName(); }
                public String toString() { return getName(); }
            };
            check("custom".length() > 0 && custom.getName().equals(g.getName()), "custom group principal");
            checkThrowsIO(java.nio.file.ProviderMismatchException.class, () -> pv.setGroup(custom),
                    "setGroup with custom principal rejected");
            // missing group name -> UserPrincipalNotFoundException
            try {
                ls.lookupPrincipalByGroupName("nosuchgroup_a12");
                check(false, "missing group lookup must throw");
            } catch (UserPrincipalNotFoundException e) {
                check("nosuchgroup_a12".equals(e.getName()), "UPNFE carries requested group name");
            }
            checkThrowsIO(NullPointerException.class, () -> ls.lookupPrincipalByGroupName(null),
                    "lookupPrincipalByGroupName(null) NPE");
            try {
                ls.lookupPrincipalByGroupName("");
                check(false, "empty group lookup must throw");
            } catch (UserPrincipalNotFoundException e) {
                check(true, "empty group name -> UserPrincipalNotFoundException");
            }
        } finally {
            cleanup(dir);
        }
    }

}
