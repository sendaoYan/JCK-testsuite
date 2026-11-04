/*
 * @test
 * @summary java.nio.file.attribute.UserDefinedFileAttributeView conformance: white-box structure + black-box behavior.
 * @run main UserDefinedFileAttributeViewTest
 */

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;

public class UserDefinedFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.UserDefinedFileAttributeView");
        check(c.isInterface(), "UserDefinedFileAttributeView is an interface");
        check(java.nio.file.attribute.FileAttributeView.class.isAssignableFrom(c),
              "extends FileAttributeView");
        for (String n : new String[] {"name", "list", "size", "read", "write", "delete"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("uda.txt");
            Files.writeString(file, "u");
            UserDefinedFileAttributeView view = Files.getFileAttributeView(file,
                    UserDefinedFileAttributeView.class);
            if (view == null) {
                System.out.println("NOTE: user view unsupported here; behavioral part skipped");
                return;
            }
            check(view.name().equals("user"), "view name is user");

            // initially empty
            check(view.list().isEmpty(), "no user attributes initially");

            // write a binary and a text attribute; both listed
            view.write("jck.mood", ByteBuffer.wrap("calm".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            view.write("jck.num", ByteBuffer.wrap(new byte[] {1, 2, 3}));
            List<String> names = view.list();
            check(names.size() == 2, "two attributes listed");
            check(names.contains("jck.mood") && names.contains("jck.num"), "names listed");

            // size then read round trip
            check(view.size("jck.num") == 3, "size reported");
            ByteBuffer buf = ByteBuffer.allocate(16);
            int n = view.read("jck.mood", buf);
            check(n == 4, "read length");
            check(new String(buf.array(), 0, n, java.nio.charset.StandardCharsets.UTF_8)
                  .equals("calm"), "read content");
            // an exactly-sized buffer reads the whole value
            ByteBuffer exact = ByteBuffer.allocate(view.size("jck.mood"));
            check(view.read("jck.mood", exact) == 4, "exact-size read consumes all");

            // overwrite replaces the value
            view.write("jck.mood", ByteBuffer.wrap(new byte[] {9}));
            check(view.size("jck.mood") == 1, "overwrite shrinks value");

            // delete removes exactly that name
            view.delete("jck.num");
            check(!view.list().contains("jck.num") && view.list().size() == 1, "deleted");
            // deleting a non-existent name is reported via the return contract of list
            try {
                view.delete("jck.num"); // already gone: ENODATA may surface
                check(view.list().size() == 1, "no resurrection after second delete");
            } catch (java.nio.file.FileSystemException fe) {
                check(!view.list().contains("jck.num"), "still gone after failed second delete");
            }

            // unknown attribute size fails (ENODATA surfaces as a FileSystemException)
            boolean nsf = false;
            try { view.size("no.such.attr"); }
            catch (java.nio.file.FileSystemException e) { nsf = true; }
            check(nsf, "size of missing attribute -> FileSystemException");

            // cleanup deletes the remaining attribute so file removal is clean
            view.delete("jck.mood");
            check(view.list().isEmpty(), "all attributes removed");
        } finally {
            Files.deleteIfExists(dir.resolve("uda.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
