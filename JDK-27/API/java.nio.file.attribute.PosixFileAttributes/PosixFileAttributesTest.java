/*
 * @test
 * @summary java.nio.file.attribute.PosixFileAttributes conformance: white-box structure + black-box behavior.
 * @run main PosixFileAttributesTest
 */

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
