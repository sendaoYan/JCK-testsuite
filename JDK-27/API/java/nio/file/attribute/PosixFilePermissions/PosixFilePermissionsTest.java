/*
 * @test
 * @summary java.nio.file.attribute.PosixFilePermissions conformance: white-box structure + black-box behavior.
 * @run main PosixFilePermissionsTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.TreeSet;

public class PosixFilePermissionsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.PosixFilePermissions");
        check(Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()),
              "final utility class");
        check(c.getDeclaredConstructors().length == 0
              || java.util.Arrays.stream(c.getDeclaredConstructors())
                   .allMatch(k -> Modifier.isPrivate(k.getModifiers())),
              "no public ctor");
        check(java.util.Arrays.stream(c.getMethods())
              .filter(m -> m.getDeclaringClass().equals(c))
              .allMatch(m -> Modifier.isStatic(m.getModifiers())), "all own methods static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // fromString parses all 9 rwx slots
        Set<PosixFilePermission> rw = PosixFilePermissions.fromString("rw-r--r--");
        check(rw.size() == 4, "4 permissions parsed");
        check(rw.contains(PosixFilePermission.OWNER_READ), "owner read");
        check(rw.contains(PosixFilePermission.OWNER_WRITE), "owner write");
        check(rw.contains(PosixFilePermission.GROUP_READ), "group read");
        check(rw.contains(PosixFilePermission.OTHERS_READ), "others read");

        // toString/fromString round trip for representative masks
        for (String mask : new String[] {"rwxrwxrwx", "rwx------", "rw-rw----",
                                         "---------", "r-xr-x---"}) {
            check(PosixFilePermissions.toString(PosixFilePermissions.fromString(mask)).equals(mask),
                  "round trip " + mask);
        }

        // toString uses sorted fixed order
        Set<PosixFilePermission> perm = new TreeSet<>(Set.of(
                PosixFilePermission.OTHERS_WRITE, PosixFilePermission.OWNER_READ));
        check(PosixFilePermissions.toString(perm).equals("r------w-"),
              "fixed rwx order: " + PosixFilePermissions.toString(perm));

        // malformed strings are rejected
        for (String bad : new String[] {"", "rwxrwxrw", "rwxrwxrwxrwx", "abcdefghi", "Rwxrwxrwx"}) {
            boolean threw = false;
            try { PosixFilePermissions.fromString(bad); }
            catch (IllegalArgumentException e) { threw = true; }
            check(threw, "fromString(\"" + bad + "\") -> IllegalArgumentException");
        }

        // asFileAttribute: name/value contract without touching the filesystem
        FileAttribute<Set<PosixFilePermission>> attr =
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
        check(attr.name().equals("posix:permissions"), "attribute name");
        check(attr.value().equals(PosixFilePermissions.fromString("rw-------")), "attribute value");

        // Files integration when POSIX is supported: apply and read back
        if (java.nio.file.FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
            try {
                java.nio.file.Path f = dir.resolve("p.txt");
                java.nio.file.Files.writeString(f, "x");
                java.nio.file.Files.setPosixFilePermissions(f,
                        PosixFilePermissions.fromString("rw-------"));
                check(java.nio.file.Files.getPosixFilePermissions(f)
                      .equals(PosixFilePermissions.fromString("rw-------")),
                      "permissions applied to real file");
            } finally {
                java.nio.file.Files.deleteIfExists(dir.resolve("p.txt"));
                java.nio.file.Files.deleteIfExists(dir);
            }
        } else {
            System.out.println("NOTE: POSIX attribute view unsupported; file part skipped");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
