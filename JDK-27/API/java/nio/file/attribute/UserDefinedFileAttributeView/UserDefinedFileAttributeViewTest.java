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
import java.nio.file.attribute.FileAttributeView;

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
            } catch (java.io.IOException fe) {
                check(!view.list().contains("jck.num"), "still gone after failed second delete");
            }

            // unknown attribute size fails (ENODATA surfaces as a FileSystemException)
            expectAttrFailure(() -> view.size("no.such.attr"),
                    "size of missing attribute fails with IOException");

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

    static boolean posixProvider() {
        return java.nio.file.FileSystems.getDefault()
                .supportedFileAttributeViews().contains("posix");
    }

    /**
     * A failed user-attribute operation must surface as IOException. The unix
     * provider refines that to FileSystemException (errno wrapper) while the
     * Windows provider reports a plainer IOException; both satisfy the API.
     */
    static void expectAttrFailure(IORunnable r, String msg) {
        try {
            r.run();
        } catch (java.io.IOException e) {
            if (posixProvider() && !(e instanceof java.nio.file.FileSystemException)) {
                throw new AssertionError(msg + ": unix provider should wrap the errno, got " + e);
            }
            passed++;
            return;
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
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-user");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            UserDefinedFileAttributeView v = java.nio.file.Files.getFileAttributeView(f,
                    UserDefinedFileAttributeView.class);
            check(v != null, "user view available on Linux ext4");
            check("user".equals(v.name()), "view name is user");
            check(v instanceof FileAttributeView, "extends FileAttributeView");
            // write/read round trip: byte counts exact
            java.nio.ByteBuffer wb = java.nio.ByteBuffer.wrap("value1".getBytes("UTF-8"));
            int n = v.write("myst", wb);
            check(n == 6, "write returns number of bytes written");
            check(v.size("myst") == 6, "size exact");
            java.nio.ByteBuffer rb = java.nio.ByteBuffer.allocate(64);
            int r = v.read("myst", rb);
            check(r == 6, "read returns attribute size");
            check(rb.position() == 6, "read advanced buffer position by n");
            check(new String(rb.array(), 0, r, "UTF-8").equals("value1"), "content byte-exact");
            // overwrite replaces the value
            v.write("myst", java.nio.ByteBuffer.wrap("zz".getBytes("UTF-8")));
            check(v.size("myst") == 2, "overwrite updates size");
            java.nio.ByteBuffer rb2 = java.nio.ByteBuffer.allocate(64);
            check(new String(rb2.array(), 0, v.read("myst", rb2), "UTF-8").equals("zz"),
                  "overwrite updates content");
            // multiple attributes coexist; list() reports them
            v.write("second", java.nio.ByteBuffer.wrap("ab".getBytes("UTF-8")));
            v.write("third", java.nio.ByteBuffer.wrap("abc".getBytes("UTF-8")));
            java.util.List<String> names = v.list();
            check(names.contains("myst") && names.contains("second") && names.contains("third"),
                  "list contains all written names");
            check(names instanceof java.util.List<?>, "list returns List<String>");
            // delete removes exactly that attribute
            v.delete("second");
            check(!v.list().contains("second"), "delete removes from list");
            check(v.size("myst") == 2, "other attributes untouched by delete");
            // zero-length attribute
            v.write("empty", java.nio.ByteBuffer.allocate(0));
            check(v.size("empty") == 0, "zero-byte attribute size 0");
            check(v.read("empty", java.nio.ByteBuffer.allocate(4)) == 0, "read of zero-byte attribute is 0");
            // write transfers only the buffer's remaining bytes, from its position
            java.nio.ByteBuffer off = java.nio.ByteBuffer.wrap(new byte[] { 9, 9, 1, 2, 3 });
            off.position(2);
            check(v.write("off", off) == 3, "write transfers remaining bytes only");
            check(v.size("off") == 3, "size counts only remaining bytes");
            // read fills the buffer from its current position
            java.nio.ByteBuffer into = java.nio.ByteBuffer.allocate(8);
            into.position(1);
            int rr = v.read("off", into);
            check(rr == 3, "read count with positioned buffer");
            check(into.position() == 4, "read fills from the initial position");
            // negatives: missing attribute -> FileSystemException (errno ENODATA wrapped)
            expectAttrFailure(() -> v.read("gone", java.nio.ByteBuffer.allocate(4)),
                    "read of missing attribute fails with IOException");
            expectAttrFailure(() -> v.size("gone"),
                    "size of missing attribute fails with IOException");
            expectAttrFailure(() -> v.delete("gone"),
                    "delete of missing attribute fails with IOException");
            // buffer smaller than the value -> FileSystemException (ERANGE)
            expectAttrFailure(() -> v.read("myst", java.nio.ByteBuffer.allocate(1)),
                    "read with too-small buffer fails with IOException");
            expectAttrFailure(() -> v.read("myst", java.nio.ByteBuffer.allocate(0)),
                    "read with zero-capacity buffer fails with IOException");
            // null arguments -> NPE
            checkThrowsIO(NullPointerException.class,
                    () -> v.write(null, java.nio.ByteBuffer.wrap(new byte[1])), "write(null name)");
            checkThrowsIO(NullPointerException.class, () -> v.size(null), "size(null name)");
            checkThrowsIO(NullPointerException.class, () -> v.read("myst", null), "read(null buffer)");
            // empty attribute name is not a legal xattr name on Linux
            expectAttrFailure(() -> v.write("", java.nio.ByteBuffer.wrap(new byte[1])),
                    "write with empty name fails with IOException");
            // missing attribute through the Files.getAttribute family
            expectAttrFailure(() -> java.nio.file.Files.getAttribute(f, "user:never_set"),
                    "getAttribute user:missing fails with IOException");
            // Files.setAttribute/getAttribute integration (byte[] values)
            java.nio.file.Files.setAttribute(f, "user:viafiles", "abc".getBytes("UTF-8"));
            check(v.list().contains("viafiles"), "setAttribute user: visible in list()");
            check(v.size("viafiles") == 3, "setAttribute user: size");
            byte[] got = (byte[]) java.nio.file.Files.getAttribute(f, "user:viafiles");
            check(new String(got, "UTF-8").equals("abc"), "getAttribute user: byte-exact round-trip");
            // user attributes also work on directories
            UserDefinedFileAttributeView dv = java.nio.file.Files.getFileAttributeView(dir,
                    UserDefinedFileAttributeView.class);
            dv.write("dattr", java.nio.ByteBuffer.wrap(new byte[] { 7 }));
            check(dv.size("dattr") == 1, "directory user attribute write");
            dv.delete("dattr");
            check(!dv.list().contains("dattr"), "directory user attribute delete");
        } finally {
            cleanup(dir);
        }
    }

}
