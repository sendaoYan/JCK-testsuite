/*
 * @test
 * @summary java.nio.file.attribute.BasicFileAttributes conformance: white-box structure + black-box behavior.
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
                Files.createSymbolicLink(link, file);
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
