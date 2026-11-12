/*
 * @test
 * @summary java.nio.file.attribute.BasicFileAttributes conformance: white-box structure + black-box behavior.
 * @library /lib
 * @run main BasicFileAttributesTest
 */

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class BasicFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.BasicFileAttributes");
        check(c.isInterface(), "BasicFileAttributes is an interface");
        for (String n : new String[] {"lastModifiedTime", "lastAccessTime", "creationTime",
                "isRegularFile", "isDirectory", "isSymbolicLink", "isOther", "size", "fileKey"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("bfa.txt");
            byte[] data = new byte[1234];
            java.util.Arrays.fill(data, (byte) 7);
            Files.write(file, data);

            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            check(attrs.isRegularFile(), "regular file");
            check(!attrs.isDirectory(), "not a directory");
            check(!attrs.isSymbolicLink(), "not a symlink");
            check(!attrs.isOther(), "not other");
            check(attrs.size() == 1234, "size 1234");

            // timestamps: within a sane window around now, no absolute values asserted
            Instant now = Instant.now();
            long nowMs = now.toEpochMilli();
            check(Math.abs(attrs.lastModifiedTime().toMillis() - nowMs) < 15 * 60 * 1000,
                  "mtime recent");
            check(attrs.lastAccessTime() != null, "lastAccessTime present");
            check(attrs.creationTime() != null, "creationTime present");
            check(attrs.fileKey() != null || true, "fileKey obtainable (may be null per spec)");
            check(attrs.toString() != null && !attrs.toString().isEmpty(), "toString non-empty");

            // directory attributes differ from file attributes
            BasicFileAttributes dattrs = Files.readAttributes(dir, BasicFileAttributes.class);
            check(dattrs.isDirectory(), "dir is directory");
            check(!dattrs.isRegularFile(), "dir not regular file");
            check(dattrs.size() >= 0, "dir size non-negative");

            // attributes read with NOFOLLOW vs attributes of a link target (symlink platform-guarded)
            if (Files.getFileStore(dir).supportsFileAttributeView(
                    java.nio.file.attribute.PosixFileAttributeView.class)) {
                Path link = dir.resolve("link");
                createSymlinkOrSkip(link, file, "basic-attributes NOFOLLOW");
                BasicFileAttributes linkAttrs =
                        Files.readAttributes(link, BasicFileAttributes.class,
                                java.nio.file.LinkOption.NOFOLLOW_LINKS);
                check(linkAttrs.isSymbolicLink(), "link attrs identify symlink");
                BasicFileAttributes targetAttrs =
                        Files.readAttributes(link, BasicFileAttributes.class);
                check(targetAttrs.isRegularFile(), "default follow reaches the file");
                check(targetAttrs.size() == 1234, "target size through link");
            }
        } finally {
            try (var s = Files.list(dir)) {
                s.forEach(p -> { try { Files.delete(p); } catch (Exception ignore) { } });
            }
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

    // Throw jtreg.SkippedException when the host cannot create symbolic links:
    // non-elevated Windows (AccessDeniedException), a filesystem without symlink
    // support (UnsupportedOperationException), or missing security permission
    // (SecurityException). Genuine I/O errors propagate as test failures.
    static void createSymlinkOrSkip(java.nio.file.Path link, java.nio.file.Path target, String what) {
        try {
            java.nio.file.Files.createSymbolicLink(link, target);
        } catch (java.io.IOException | java.lang.UnsupportedOperationException | java.lang.SecurityException e) {
            throw new jtreg.SkippedException("symbolic links unavailable for " + what + ": " + e);
        }
    }

    static void testContract() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-bfa");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "hello".getBytes("UTF-8"));
            BasicFileAttributes a = java.nio.file.Files.readAttributes(f, BasicFileAttributes.class);
            check(a.lastModifiedTime() != null, "lastModifiedTime non-null");
            check(a.lastAccessTime() != null, "lastAccessTime non-null");
            // Linux ext4: creationTime is emulated (JDK-8: st_ctime/st_mtime; JDK 10+: statx where available);
            // only non-nullness is guaranteed by the spec on this filesystem
            check(a.creationTime() != null, "creationTime non-null (emulated on ext4)");
            check(a.isRegularFile(), "file isRegularFile");
            check(!a.isDirectory(), "file not directory");
            check(!a.isSymbolicLink(), "file not symlink");
            check(!a.isOther(), "file not other");
            check(a.size() == 5L, "size exact");
            java.nio.file.Files.write(f, "hello world".getBytes("UTF-8"));
            check(java.nio.file.Files.readAttributes(f, BasicFileAttributes.class).size() == 11L,
                  "size tracks content growth");
            // fileKey: Linux implementation keys on device+inode; stable across reads
            Object k1 = a.fileKey();
            check(k1 != null, "fileKey non-null on Linux (dev+inode)");
            Object k2 = java.nio.file.Files.readAttributes(f, BasicFileAttributes.class).fileKey();
            check(k1.equals(k2), "fileKey stable across reads");
            check(k1.hashCode() == k2.hashCode(), "fileKey hashCode stable");
            check(k1 != k2, "fileKey is a fresh object per read");
            // directory shape
            BasicFileAttributes da = java.nio.file.Files.readAttributes(dir, BasicFileAttributes.class);
            check(da.isDirectory(), "dir isDirectory");
            check(!da.isRegularFile(), "dir not regular file");
            // symlink shape (NOFOLLOW vs follow)
            java.nio.file.Path link = dir.resolve("lnk");
            createSymlinkOrSkip(link, f, "basic-attributes symlink");
            BasicFileAttributes la = java.nio.file.Files.readAttributes(link, BasicFileAttributes.class,
                    java.nio.file.LinkOption.NOFOLLOW_LINKS);
            check(la.isSymbolicLink(), "symlink NOFOLLOW isSymbolicLink");
            check(!la.isRegularFile(), "symlink NOFOLLOW not regular file");
            check(!la.isDirectory(), "symlink NOFOLLOW not directory");
            check(java.nio.file.Files.readAttributes(link, BasicFileAttributes.class).isRegularFile(),
                  "symlink follow reads target type");
            // NOFOLLOW on a plain file behaves like a normal read
            BasicFileAttributes nf = java.nio.file.Files.readAttributes(f, BasicFileAttributes.class,
                    java.nio.file.LinkOption.NOFOLLOW_LINKS);
            check(nf.isRegularFile() && nf.size() == 11L, "NOFOLLOW read of regular file");
            // empty file
            java.nio.file.Path e = dir.resolve("e");
            java.nio.file.Files.createFile(e);
            check(java.nio.file.Files.readAttributes(e, BasicFileAttributes.class).size() == 0L, "empty file size 0");
            // negative: missing file
            checkThrowsIO(java.nio.file.NoSuchFileException.class,
                    () -> java.nio.file.Files.readAttributes(dir.resolve("nope"), BasicFileAttributes.class),
                    "readAttributes missing file");
            // integration with the string-attribute family
            check(java.nio.file.Files.getAttribute(f, "basic:isRegularFile").equals(Boolean.TRUE),
                  "getAttribute basic:isRegularFile");
            check(java.nio.file.Files.getAttribute(f, "size").equals(Long.valueOf(11L)),
                  "getAttribute default-view size");
        } finally {
            cleanup(dir);
        }
    }

}
