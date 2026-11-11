/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.attribute.PosixFileAttributes conformance: white-box structure + black-box behavior.
 * @run main PosixFileAttributesTest
 */

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.BasicFileAttributes;

public class PosixFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.PosixFileAttributes");
        check(c.isInterface(), "PosixFileAttributes is an interface");
        check(java.nio.file.attribute.BasicFileAttributes.class.isAssignableFrom(c),
              "extends BasicFileAttributes");
        for (String n : new String[] {"owner", "group", "permissions"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        if (!java.nio.file.FileSystems.getDefault().supportedFileAttributeViews()
                .contains("posix")) {
            System.out.println("NOTE: posix view unsupported; behavioral part skipped");
            return;
        }
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("p.txt");
            Files.writeString(file, "posix");

            PosixFileAttributes attrs = Files.readAttributes(file, PosixFileAttributes.class);
            check(attrs.isRegularFile() && attrs.size() == 5, "basic face still works");
            check(attrs.owner() != null && attrs.owner().getName() != null, "owner non-null");
            check(attrs.group() != null && attrs.group().getName() != null, "group non-null");
            Set<PosixFilePermission> perms = attrs.permissions();
            check(perms != null && !perms.isEmpty(), "permissions non-empty");
            check(perms.contains(PosixFilePermission.OWNER_READ), "owner can read own file");
            check(perms.contains(PosixFilePermission.OWNER_WRITE), "owner can write own file");

            // changing permissions is reflected in a fresh read
            java.util.Set<PosixFilePermission> tighter = java.util.EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(file, tighter);
            PosixFileAttributes after = Files.readAttributes(file, PosixFileAttributes.class);
            check(after.permissions().equals(tighter), "tightened permissions visible");

            // owner equals Files.getOwner
            check(attrs.owner().equals(Files.getOwner(file)), "owner consistent with Files.getOwner");

            // the same permissions power the directory view
            PosixFileAttributes dattrs = Files.readAttributes(dir, PosixFileAttributes.class);
            check(dattrs.isDirectory() && dattrs.permissions()
                    .contains(PosixFilePermission.OWNER_EXECUTE), "dir owner execute bit");
        } finally {
            Files.deleteIfExists(dir.resolve("p.txt"));
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
