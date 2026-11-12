/*
 * @test
 * @summary java.nio.file.attribute.BasicFileAttributes conformance: via readAttributes.
 * @run main BasicFileAttributesTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class BasicFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasicFileAttributes() throws Exception {
        Path dir = Files.createTempDirectory("jck-bfa");
        Path file = dir.resolve("f");
        Files.write(file, "hello".getBytes("UTF-8"));
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        check(attrs.isRegularFile(), "isRegularFile");
        check(!attrs.isDirectory(), "not directory");
        check(!attrs.isSymbolicLink(), "not symbolic link");
        check(!attrs.isOther(), "not other");
        check(attrs.size() == 5, "size");
        check(attrs.creationTime() != null, "creationTime");
        check(attrs.lastModifiedTime() != null, "lastModifiedTime");
        check(attrs.lastAccessTime() != null, "lastAccessTime");
        check(attrs.fileKey() != null || attrs.fileKey() == null, "fileKey callable");
        Files.delete(file);
        Files.delete(dir);
    }

    public static void main(String[] args) throws Exception {
        testBasicFileAttributes();
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
            // fileKey is filesystem-defined: Linux keys on device+inode, Windows has
            // no inode so it may be null. When non-null it must be stable across reads.
            Object k1 = a.fileKey();
            if (k1 != null) {
                Object k2 = java.nio.file.Files.readAttributes(f, BasicFileAttributes.class).fileKey();
                check(k1.equals(k2), "fileKey stable across reads");
                check(k1.hashCode() == k2.hashCode(), "fileKey hashCode stable");
                check(k1 != k2, "fileKey is a fresh object per read");
            } else {
                check(java.nio.file.Files.readAttributes(f, BasicFileAttributes.class).fileKey() == null,
                      "fileKey consistently null on this fs");
            }
            // directory shape
            BasicFileAttributes da = java.nio.file.Files.readAttributes(dir, BasicFileAttributes.class);
            check(da.isDirectory(), "dir isDirectory");
            check(!da.isRegularFile(), "dir not regular file");
            // symlink shape (NOFOLLOW vs follow). Windows without elevated or
            // developer mode rejects createSymbolicLink (AccessDeniedException),
            // so the symlink block is skipped there rather than failing.
            java.nio.file.Path link = dir.resolve("lnk");
            boolean symlinkMade = false;
            try {
                java.nio.file.Files.createSymbolicLink(link, f);
                symlinkMade = true;
            } catch (java.io.IOException | java.lang.UnsupportedOperationException | java.lang.SecurityException e) {
                // symlink not creatable on this host/filesystem (e.g. no privilege)
            }
            if (symlinkMade) {
                BasicFileAttributes la = java.nio.file.Files.readAttributes(link, BasicFileAttributes.class,
                        java.nio.file.LinkOption.NOFOLLOW_LINKS);
                check(la.isSymbolicLink(), "symlink NOFOLLOW isSymbolicLink");
                check(!la.isRegularFile(), "symlink NOFOLLOW not regular file");
                check(!la.isDirectory(), "symlink NOFOLLOW not directory");
                check(java.nio.file.Files.readAttributes(link, BasicFileAttributes.class).isRegularFile(),
                      "symlink follow reads target type");
            }
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
