/*
 * @test
 * @summary java.nio.file.attribute.PosixFileAttributes conformance: hierarchy and methods.
 * @run main PosixFileAttributesTest
 */

import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.BasicFileAttributes;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.GroupPrincipal;

public class PosixFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPosixFileAttributes() {
        check(PosixFileAttributes.class.isInterface(), "is interface");
        check(BasicFileAttributes.class.isAssignableFrom(PosixFileAttributes.class), "extends BasicFileAttributes");
        Set<String> names = new HashSet<String>();
        for (Method m : PosixFileAttributes.class.getMethods()) names.add(m.getName());
        String[] required = { "permissions", "owner", "group" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testPosixFileAttributes();
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
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-pfa");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "1234567".getBytes("UTF-8"));
            PosixFileAttributes a = java.nio.file.Files.readAttributes(f, PosixFileAttributes.class);
            check(a.owner() != null, "owner non-null");
            check(a.owner().getName() != null && !a.owner().getName().isEmpty(), "owner name non-empty");
            check(a.group() != null, "group non-null");
            check(a.group().getName() != null && !a.group().getName().isEmpty(), "group name non-empty");
            UserPrincipalLookupService ls = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
            check(a.owner().equals(ls.lookupPrincipalByName(System.getProperty("user.name"))),
                  "owner equals lookup of current user");
            check(a.group() instanceof GroupPrincipal, "group is a GroupPrincipal");
            check(a.permissions() instanceof java.util.Set<?>, "permissions is a Set");
            check(a.permissions().contains(PosixFilePermission.OWNER_READ)
                  && a.permissions().contains(PosixFilePermission.OWNER_WRITE),
                  "owner read+write present for temp file created by self");
            check(a.isRegularFile(), "file isRegularFile");
            check(!a.isDirectory(), "file not directory");
            check(!a.isSymbolicLink(), "file not symlink");
            check(!a.isOther(), "file not other");
            check(a.size() == 7L, "size exact");
            check(a.lastModifiedTime() != null && a.lastAccessTime() != null && a.creationTime() != null,
                  "all three times non-null (creationTime emulated on ext4)");
            check(a instanceof BasicFileAttributes, "PosixFileAttributes instanceof BasicFileAttributes");
            // permissions() returns a fresh copy per call (defensive)
            java.util.Set<PosixFilePermission> p1 = a.permissions();
            p1.clear();
            check(a.permissions().size() >= 2, "mutating returned set does not corrupt attributes");
            // directory attributes
            PosixFileAttributes da = java.nio.file.Files.readAttributes(dir, PosixFileAttributes.class);
            check(da.isDirectory(), "dir isDirectory");
            check(!da.isRegularFile(), "dir not regular file");
            check(da.owner() != null && da.group() != null, "dir owner/group non-null");
            // symlink via NOFOLLOW
            java.nio.file.Path link = dir.resolve("lnk");
            java.nio.file.Files.createSymbolicLink(link, f);
            PosixFileAttributes la = java.nio.file.Files.readAttributes(link, PosixFileAttributes.class,
                    java.nio.file.LinkOption.NOFOLLOW_LINKS);
            check(la.isSymbolicLink(), "symlink NOFOLLOW isSymbolicLink");
            check(!la.isRegularFile(), "symlink NOFOLLOW not regular file");
            PosixFileAttributes fo = java.nio.file.Files.readAttributes(link, PosixFileAttributes.class);
            check(fo.isRegularFile(), "symlink follow reads target");
            // negative: missing file
            checkThrowsIO(java.nio.file.NoSuchFileException.class,
                    () -> java.nio.file.Files.readAttributes(dir.resolve("nope"), PosixFileAttributes.class),
                    "readAttributes missing file");
            // permissions reflect a chmod applied through the view
            PosixFileAttributeView v = java.nio.file.Files.getFileAttributeView(f, PosixFileAttributeView.class);
            v.setPermissions(PosixFilePermissions.fromString("r--r-----"));
            check(java.nio.file.Files.readAttributes(f, PosixFileAttributes.class).permissions()
                    .equals(PosixFilePermissions.fromString("r--r-----")),
                  "readAttributes reflects setPermissions");
        } finally {
            cleanup(dir);
        }
    }

}
