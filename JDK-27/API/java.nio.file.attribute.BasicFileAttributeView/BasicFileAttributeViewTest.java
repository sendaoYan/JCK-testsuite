/*
 * @test
 * @summary java.nio.file.attribute.BasicFileAttributeView conformance: white-box structure + black-box behavior.
 * @run main BasicFileAttributeViewTest
 */

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class BasicFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.BasicFileAttributeView");
        check(c.isInterface(), "BasicFileAttributeView is an interface");
        check(java.nio.file.attribute.FileAttributeView.class.isAssignableFrom(c),
              "extends FileAttributeView");
        for (String n : new String[] {"name", "readAttributes", "setTimes"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("b.txt");
            Files.writeString(file, "hello");

            BasicFileAttributeView view = Files.getFileAttributeView(file,
                    BasicFileAttributeView.class);
            check(view != null, "view available");
            check(view.name().equals("basic"), "view name is basic");

            // readAttributes through the view
            BasicFileAttributes attrs = view.readAttributes();
            check(attrs.isRegularFile() && attrs.size() == 5, "read via view");

            // setTimes with fixed times then verify each timestamp readback
            FileTime mtime = FileTime.from(Instant.parse("2021-03-04T05:06:07Z"));
            FileTime atime = FileTime.from(Instant.parse("2022-07-08T09:10:11Z"));
            view.setTimes(mtime, atime, null);
            BasicFileAttributes after = view.readAttributes();
            check(after.lastModifiedTime().toMillis() == mtime.toMillis(), "mtime set");
            check(after.lastAccessTime().toMillis() == atime.toMillis(), "atime set (fs granularity permitting)");
            check(after.size() == 5, "size unchanged by setTimes");

            // setTimes(null, null, null): no change
            view.setTimes(null, null, null);
            check(view.readAttributes().lastModifiedTime().toMillis() == mtime.toMillis(),
                  "null setTimes leaves values");

            // Files.getLastModifiedTime agrees with the view
            check(Files.getLastModifiedTime(file).toMillis() == mtime.toMillis(),
                  "Files/getLastModifiedTime consistent with view");

            // view of a missing file read fails with NoSuchFileException
            BasicFileAttributeView missing = Files.getFileAttributeView(
                    dir.resolve("nope"), BasicFileAttributeView.class);
            boolean threw = false;
            try { if (missing != null) missing.readAttributes(); }
            catch (java.nio.file.NoSuchFileException e) { threw = true; }
            check(threw, "readAttributes on missing file -> NoSuchFileException");

            // string-form attribute access interop ("basic:lastModifiedTime")
            Object m = Files.getAttribute(file, "basic:lastModifiedTime");
            check(((FileTime) m).toMillis() == mtime.toMillis(), "string-form basic view read");
        } finally {
            Files.deleteIfExists(dir.resolve("b.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
