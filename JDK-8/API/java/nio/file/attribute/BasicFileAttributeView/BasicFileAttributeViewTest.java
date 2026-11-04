/*
 * @test
 * @summary java.nio.file.attribute.BasicFileAttributeView conformance: hierarchy and methods.
 * @run main BasicFileAttributeViewTest
 */

import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;

public class BasicFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasicFileAttributeView() {
        check(BasicFileAttributeView.class.isInterface(), "is interface");
        check(FileAttributeView.class.isAssignableFrom(BasicFileAttributeView.class), "extends FileAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : BasicFileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "readAttributes", "setTimes" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testBasicFileAttributeView();
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
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-bv");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            BasicFileAttributeView v = java.nio.file.Files.getFileAttributeView(f, BasicFileAttributeView.class);
            check(v != null, "basic view available");
            check("basic".equals(v.name()), "view name is basic");
            check(v instanceof FileAttributeView, "extends FileAttributeView");
            check(v.readAttributes() != null, "readAttributes non-null");
            check(v.readAttributes().isRegularFile(), "readAttributes isRegularFile");
            check(v.readAttributes().size() == 1L, "readAttributes size");
            // setTimes(mtime) round-trip at millisecond precision
            v.setTimes(FileTime.fromMillis(1599999999999L), null, null);
            check(v.readAttributes().lastModifiedTime().toMillis() == 1599999999999L, "mtime round-trip");
            // pre-epoch mtime round-trips (utimensat allows it on Linux)
            v.setTimes(FileTime.fromMillis(-1000000000L), null, null);
            check(v.readAttributes().lastModifiedTime().toMillis() == -1000000000L, "pre-epoch mtime round-trip");
            v.setTimes(FileTime.fromMillis(1599999999999L), null, null);
            // atime-only must not touch mtime
            v.setTimes(null, FileTime.fromMillis(1200000000000L), null);
            check(v.readAttributes().lastModifiedTime().toMillis() == 1599999999999L,
                  "atime-only setTimes leaves mtime");
            check(v.readAttributes().lastAccessTime().toMillis() == 1200000000000L, "atime set");
            // ctime-only is a no-op on Linux (change time cannot be set); mtime/atime unchanged
            v.setTimes(null, null, FileTime.fromMillis(1300000000000L));
            check(v.readAttributes().lastModifiedTime().toMillis() == 1599999999999L,
                  "ctime-only setTimes leaves mtime on Linux");
            check(v.readAttributes().lastAccessTime().toMillis() == 1200000000000L,
                  "ctime-only setTimes leaves atime on Linux");
            // all-null is an explicit no-op (documented)
            v.setTimes(null, null, null);
            check(v.readAttributes().lastModifiedTime().toMillis() == 1599999999999L, "all-null setTimes no-op");
            // inheritance matrix: posix/dos views are basic views
            check(java.nio.file.Files.getFileAttributeView(f, PosixFileAttributeView.class)
                    instanceof BasicFileAttributeView, "PosixFileAttributeView is-a BasicFileAttributeView");
            check(java.nio.file.Files.getFileAttributeView(f, DosFileAttributeView.class)
                    instanceof BasicFileAttributeView, "DosFileAttributeView is-a BasicFileAttributeView");
            // view operations on missing file
            BasicFileAttributeView miss = java.nio.file.Files.getFileAttributeView(dir.resolve("nope"),
                    BasicFileAttributeView.class);
            checkThrowsIO(java.nio.file.NoSuchFileException.class,
                    () -> miss.setTimes(FileTime.fromMillis(1L), null, null), "setTimes on missing file");
            checkThrowsIO(java.nio.file.NoSuchFileException.class, () -> miss.readAttributes(),
                    "readAttributes on missing file");
            // Files helpers and string attributes hit the same view
            java.nio.file.Files.setLastModifiedTime(f, FileTime.fromMillis(1234567890000L));
            check(java.nio.file.Files.getLastModifiedTime(f).toMillis() == 1234567890000L,
                  "Files.set/getLastModifiedTime round-trip");
            check(v.readAttributes().lastModifiedTime().toMillis() == 1234567890000L,
                  "view readAttributes agrees with Files helper");
            java.nio.file.Files.setAttribute(f, "basic:lastModifiedTime", FileTime.fromMillis(1111111111111L));
            check(java.nio.file.Files.getAttribute(f, "basic:lastModifiedTime") instanceof FileTime,
                  "getAttribute basic:lastModifiedTime is FileTime");
            check(((FileTime) java.nio.file.Files.getAttribute(f, "basic:lastModifiedTime")).toMillis()
                    == 1111111111111L, "setAttribute basic:lastModifiedTime round-trip");
        } finally {
            cleanup(dir);
        }
    }

}
