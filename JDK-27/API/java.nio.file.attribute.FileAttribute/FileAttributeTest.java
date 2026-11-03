/*
 * @test
 * @summary java.nio.file.attribute.FileAttribute conformance: white-box structure + black-box behavior.
 * @run main FileAttributeTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class FileAttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static FileAttribute<Object> named(String n, Object v) {
        return new FileAttribute<>() {
            public String name() { return n; }
            public Object value() { return v; }
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.FileAttribute");
        check(c.isInterface(), "FileAttribute is an interface");
        check(c.getTypeParameters().length == 1, "generic interface");
        for (String n : new String[] {"name", "value"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // custom implementation carries name/value verbatim
        FileAttribute<Object> attr = named("posix:permissions", "rw-r--r--");
        check(attr.name().equals("posix:permissions"), "name passthrough");
        check(attr.value().equals("rw-r--r--"), "value passthrough");

        // Files.createFile accepts varargs attributes (none needed) and
        // PosixFilePermissions.asFileAttribute builds a standard instance
        FileAttribute<Set<java.nio.file.attribute.PosixFilePermission>> std =
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--------"));
        check(std.name().equals("posix:permissions"), "asFileAttribute name");
        check(std.value().equals(java.util.EnumSet.of(
                java.nio.file.attribute.PosixFilePermission.OWNER_READ)), "asFileAttribute value");

        // creating a file with that attribute yields matching permissions (posix-guarded)
        if (java.nio.file.FileSystems.getDefault().supportedFileAttributeViews()
                .contains("posix")) {
            Path dir = Files.createTempDirectory("jck");
            try {
                Path p = dir.resolve("fa.txt");
                Files.createFile(p, std);
                Set<java.nio.file.attribute.PosixFilePermission> got =
                        Files.getPosixFilePermissions(p);
                check(got.equals(std.value()), "created file carries supplied attributes");
            } finally {
                Files.deleteIfExists(dir.resolve("fa.txt"));
                Files.deleteIfExists(dir);
            }
        }

        // createDirectory with attributes also honors them
        if (java.nio.file.FileSystems.getDefault().supportedFileAttributeViews()
                .contains("posix")) {
            Path dir = Files.createTempDirectory("jck2");
            try {
                Path sub = dir.resolve("sub");
                Files.createDirectory(sub, std);
                check(Files.getPosixFilePermissions(sub).equals(std.value()),
                      "created directory carries supplied attributes");
            } finally {
                Files.deleteIfExists(dir.resolve("sub"));
                Files.deleteIfExists(dir);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
