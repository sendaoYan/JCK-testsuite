/*
 * @test
 * @summary java.nio.file.attribute.DosFileAttributeView conformance: white-box structure + black-box behavior.
 * @run main DosFileAttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class DosFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.DosFileAttributeView");
        check(c.isInterface(), "DosFileAttributeView is an interface");
        check(java.nio.file.attribute.BasicFileAttributeView.class.isAssignableFrom(c),
              "extends BasicFileAttributeView");
        for (String n : new String[] {"name", "readAttributes", "setReadOnly", "setHidden",
                "setArchive", "setSystem"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("dv.txt");
            Files.writeString(file, "v");
            DosFileAttributeView view = Files.getFileAttributeView(file,
                    DosFileAttributeView.class);
            if (view == null) {
                System.out.println("NOTE: dos view unsupported here; behavioral part skipped");
                return;
            }
            check(view.name().equals("dos"), "view name is dos");

            // readAttributes through the view
            DosFileAttributes attrs = view.readAttributes();
            check(attrs.isRegularFile() && attrs.size() == 1, "read via dos view");

            // inherited setTimes works through the dos view
            FileTime fixed = FileTime.from(Instant.parse("2020-06-01T12:00:00Z"));
            view.setTimes(fixed, null, null);
            check(view.readAttributes().lastModifiedTime().toMillis() == fixed.toMillis(),
                  "setTimes via dos view");

            // string-form access through the dos scheme
            Object ro = Files.getAttribute(file, "dos:readonly");
            check(ro instanceof Boolean, "dos:readonly readable");

            // missing file: operations fail with NoSuchFileException
            DosFileAttributeView missing = Files.getFileAttributeView(dir.resolve("gone"),
                    DosFileAttributeView.class);
            boolean nsf = false;
            try { if (missing != null) missing.readAttributes(); }
            catch (java.nio.file.NoSuchFileException e) { nsf = true; }
            check(nsf, "readAttributes missing -> NoSuchFileException");
            boolean nsf2 = false;
            try { if (missing != null) missing.setHidden(true); }
            catch (java.nio.file.NoSuchFileException e) { nsf2 = true; }
            check(nsf2, "setHidden missing -> NoSuchFileException");
        } finally {
            Files.deleteIfExists(dir.resolve("dv.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
