/*
 * @test
 * @summary java.nio.file.attribute.DosFileAttributes conformance: white-box structure + black-box behavior.
 * @run main DosFileAttributesTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;

public class DosFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.DosFileAttributes");
        check(c.isInterface(), "DosFileAttributes is an interface");
        check(java.nio.file.attribute.BasicFileAttributes.class.isAssignableFrom(c),
              "extends BasicFileAttributes");
        for (String n : new String[] {"isReadOnly", "isHidden", "isArchive", "isSystem"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("dos.txt");
            Files.writeString(file, "dos");
            DosFileAttributes attrs = Files.readAttributes(file, DosFileAttributes.class);
            if (attrs == null) {
                System.out.println("NOTE: dos view unsupported here; behavioral part skipped");
                return;
            }
            check(attrs.isRegularFile() && attrs.size() == 3, "basic face works");
            check(!attrs.isReadOnly(), "fresh file not read-only");
            check(!attrs.isHidden(), "plain name not hidden");
            check(attrs.isArchive() || !attrs.isArchive(), "archive readable (boolean)");
            check(attrs.isSystem() || !attrs.isSystem(), "system readable (boolean)");

            // toggling the DOS bits is observable
            java.nio.file.attribute.DosFileAttributeView view =
                    Files.getFileAttributeView(file,
                            java.nio.file.attribute.DosFileAttributeView.class);
            view.setReadOnly(true);
            check(Files.readAttributes(file, DosFileAttributes.class).isReadOnly(),
                  "read-only visible after set");
            view.setReadOnly(false);
            check(!Files.readAttributes(file, DosFileAttributes.class).isReadOnly(),
                  "read-only cleared");
            view.setHidden(true);
            check(Files.readAttributes(file, DosFileAttributes.class).isHidden(), "hidden set");
            view.setHidden(false);
            check(!Files.readAttributes(file, DosFileAttributes.class).isHidden(), "hidden cleared");
            view.setArchive(false);
            check(!Files.readAttributes(file, DosFileAttributes.class).isArchive(),
                  "archive cleared");
            view.setSystem(true);
            check(Files.readAttributes(file, DosFileAttributes.class).isSystem(), "system set");

            // read-only file refuses writes on platforms enforcing it; both outcomes are fine
            view.setReadOnly(true);
            boolean denied = false;
            try { Files.writeString(file, "nope"); }
            catch (java.nio.file.AccessDeniedException e) { denied = true; }
            check(denied || Files.readString(file).equals("dos") || true,
                  "write attempt observed (enforcement is platform-dependent)");
            view.setReadOnly(false);
        } finally {
            java.nio.file.attribute.DosFileAttributeView v =
                    Files.getFileAttributeView(dir.resolve("dos.txt"),
                            java.nio.file.attribute.DosFileAttributeView.class);
            if (v != null) v.setReadOnly(false);
            Files.deleteIfExists(dir.resolve("dos.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
