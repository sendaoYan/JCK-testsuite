/*
 * @test
 * @summary java.nio.file.attribute.PosixFileAttributeView conformance: hierarchy and methods.
 * @run main PosixFileAttributeViewTest
 */

import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.FileAttributeView;

public class PosixFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPosixFileAttributeView() {
        check(PosixFileAttributeView.class.isInterface(), "is interface");
        check(BasicFileAttributeView.class.isAssignableFrom(PosixFileAttributeView.class), "extends BasicFileAttributeView");
        check(FileOwnerAttributeView.class.isAssignableFrom(PosixFileAttributeView.class), "extends FileOwnerAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : PosixFileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "setPermissions", "setGroup" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testPosixFileAttributeView();
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
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-pv");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            PosixFileAttributeView v = java.nio.file.Files.getFileAttributeView(f, PosixFileAttributeView.class);
            check(v != null, "posix view available on Linux tmpfs/ext4");
            check("posix".equals(v.name()), "view name is posix");
            check(v instanceof BasicFileAttributeView, "extends BasicFileAttributeView");
            check(v instanceof FileOwnerAttributeView, "extends FileOwnerAttributeView");
            check(v instanceof FileAttributeView, "extends FileAttributeView (transitively)");
            check(v.readAttributes() != null, "readAttributes non-null");
            check(v.readAttributes().owner() != null, "readAttributes owner non-null");
            // setPermissions round-trip matrix
            String[] modes = { "rwxr-x---", "rw-r--r--", "r--------", "rwxrwxrwx", "---------" };
            for (String m : modes) {
                v.setPermissions(PosixFilePermissions.fromString(m));
                check(PosixFilePermissions.toString(v.readAttributes().permissions()).equals(m),
                        "setPermissions round-trip '" + m + "'");
            }
            v.setPermissions(PosixFilePermissions.fromString("rw-r--r--"));
            check(v.readAttributes().permissions().equals(PosixFilePermissions.fromString("rw-r--r--")),
                  "setPermissions set equality");
            checkThrowsIO(NullPointerException.class, () -> v.setPermissions(null), "setPermissions(null) NPE");
            // setTimes matrix: null argument means "leave unchanged" (probed on Linux)
            v.setTimes(FileTime.fromMillis(1000000000000L), FileTime.fromMillis(1100000000000L), null);
            check(v.readAttributes().lastModifiedTime().toMillis() == 1000000000000L, "mtime set");
            check(v.readAttributes().lastAccessTime().toMillis() == 1100000000000L, "atime set");
            v.setTimes(null, FileTime.fromMillis(1200000000000L), null);
            check(v.readAttributes().lastModifiedTime().toMillis() == 1000000000000L,
                  "atime-only setTimes leaves mtime");
            check(v.readAttributes().lastAccessTime().toMillis() == 1200000000000L, "atime updated");
            long mBefore = v.readAttributes().lastModifiedTime().toMillis();
            long aBefore = v.readAttributes().lastAccessTime().toMillis();
            v.setTimes(null, null, FileTime.fromMillis(1300000000000L));
            check(v.readAttributes().lastModifiedTime().toMillis() == mBefore,
                  "ctime-only setTimes leaves mtime (Linux cannot set change time)");
            check(v.readAttributes().lastAccessTime().toMillis() == aBefore,
                  "ctime-only setTimes leaves atime");
            v.setTimes(null, null, null);
            check(v.readAttributes().lastModifiedTime().toMillis() == mBefore, "all-null setTimes is a no-op");
            check(v.readAttributes().lastAccessTime().toMillis() == aBefore, "all-null setTimes leaves atime");
            // pre-epoch timestamps round-trip through utimensat
            v.setTimes(FileTime.fromMillis(-1000000000L), null, null);
            check(v.readAttributes().lastModifiedTime().toMillis() == -1000000000L, "pre-epoch mtime round-trip");
            v.setTimes(FileTime.fromMillis(1000000000000L), FileTime.fromMillis(1100000000000L), null);
            // setGroup round-trip to the file's own group (guaranteed allowed)
            GroupPrincipal g = v.readAttributes().group();
            v.setGroup(g);
            check(v.readAttributes().group().equals(g), "setGroup to own group round-trip");
            checkThrowsIO(NullPointerException.class, () -> v.setGroup(null), "setGroup(null) NPE");
            // owner via FileOwnerAttributeView face of the posix view
            UserPrincipal self = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService()
                    .lookupPrincipalByName(System.getProperty("user.name"));
            check(v.getOwner().equals(self), "posix view getOwner equals current user");
            v.setOwner(self);
            check(v.getOwner().equals(self), "posix view setOwner self round-trip");
            // negative: operations on a missing file
            PosixFileAttributeView miss = java.nio.file.Files.getFileAttributeView(dir.resolve("nope"),
                    PosixFileAttributeView.class);
            checkThrowsIO(java.nio.file.NoSuchFileException.class,
                    () -> miss.setPermissions(PosixFilePermissions.fromString("rw-r--r--")),
                    "setPermissions on missing file");
            checkThrowsIO(java.nio.file.NoSuchFileException.class, () -> miss.readAttributes(),
                    "readAttributes on missing file");
            checkThrowsIO(java.nio.file.NoSuchFileException.class, () -> miss.setGroup(g),
                    "setGroup on missing file");
            checkThrowsIO(java.nio.file.NoSuchFileException.class,
                    () -> miss.setTimes(FileTime.fromMillis(1L), null, null), "setTimes on missing file");
            // foreign-provider principal is rejected
            final GroupPrincipal foreign = new GroupPrincipal() {
                public String getName() { return g.getName(); }
                public String toString() { return getName(); }
            };
            checkThrowsIO(java.nio.file.ProviderMismatchException.class, () -> v.setGroup(foreign),
                    "setGroup with foreign principal");
        } finally {
            cleanup(dir);
        }
    }

}
