/*
 * @test
 * @summary java.nio.file.attribute.PosixFileAttributeView conformance: white-box structure + black-box behavior.
 * @run main PosixFileAttributeViewTest
 */

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

public class PosixFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.PosixFileAttributeView");
        check(c.isInterface(), "PosixFileAttributeView is an interface");
        check(java.nio.file.attribute.BasicFileAttributeView.class.isAssignableFrom(c),
              "extends BasicFileAttributeView");
        check(java.nio.file.attribute.FileOwnerAttributeView.class.isAssignableFrom(c),
              "extends FileOwnerAttributeView");
        for (String n : new String[] {"name", "readAttributes", "setPermissions", "setGroup"})
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
            Path file = dir.resolve("pv.txt");
            Files.writeString(file, "view");

            PosixFileAttributeView view = Files.getFileAttributeView(file,
                    PosixFileAttributeView.class);
            check(view != null, "view available");
            check(view.name().equals("posix"), "view name is posix");

            // read via view
            PosixFileAttributes attrs = view.readAttributes();
            check(attrs.isRegularFile() && attrs.size() == 4, "read via posix view");

            // setPermissions round trip
            Set<PosixFilePermission> rw = EnumSet.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_READ);
            view.setPermissions(rw);
            check(view.readAttributes().permissions().equals(rw), "setPermissions via view");

            // setTimes inherited through posix view
            FileTime fixed = FileTime.from(Instant.parse("2019-12-31T23:59:59Z"));
            view.setTimes(fixed, null, null);
            check(view.readAttributes().lastModifiedTime().toMillis() == fixed.toMillis(),
                  "setTimes via posix view");

            // setOwner/setGroup with the current identities (stable, permission-free)
            view.setOwner(attrs.owner());
            check(view.getOwner().equals(attrs.owner()), "setOwner round trip");
            view.setGroup(attrs.group());
            check(view.readAttributes().group().equals(attrs.group()), "setGroup round trip");

            // string-form read of posix attributes
            Object permStr = Files.getAttribute(file, "posix:permissions");
            check(permStr instanceof java.util.Set, "string-form permissions readable");

            // view of a missing file: readAttributes fails NoSuchFile
            PosixFileAttributeView missing = Files.getFileAttributeView(
                    dir.resolve("gone"), PosixFileAttributeView.class);
            boolean threw = false;
            try { if (missing != null) missing.readAttributes(); }
            catch (java.nio.file.NoSuchFileException e) { threw = true; }
            check(threw, "readAttributes missing -> NoSuchFileException");
        } finally {
            Files.deleteIfExists(dir.resolve("pv.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
