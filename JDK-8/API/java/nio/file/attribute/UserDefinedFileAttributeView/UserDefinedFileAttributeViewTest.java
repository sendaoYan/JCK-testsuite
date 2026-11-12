/*
 * @test
 * @summary java.nio.file.attribute.UserDefinedFileAttributeView conformance: hierarchy and methods.
 * @run main UserDefinedFileAttributeViewTest
 */

import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;

public class UserDefinedFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUserDefinedFileAttributeView() {
        check(UserDefinedFileAttributeView.class.isInterface(), "is interface");
        check(FileAttributeView.class.isAssignableFrom(UserDefinedFileAttributeView.class), "extends FileAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : UserDefinedFileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "list", "size", "read", "write", "delete" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testUserDefinedFileAttributeView();
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
            // negatives: missing attribute -> IOException (Linux wraps errno ENODATA as
            // FileSystemException, Windows reports a plain IOException; accept both).
            checkThrowsIO(java.io.IOException.class, () -> v.read("gone", java.nio.ByteBuffer.allocate(4)),
                    "read of missing attribute");
            checkThrowsIO(java.io.IOException.class, () -> v.size("gone"),
                    "size of missing attribute");
            checkThrowsIO(java.io.IOException.class, () -> v.delete("gone"),
                    "delete of missing attribute");
            // buffer smaller than the value -> IOException (Linux ERANGE = FileSystemException)
            checkThrowsIO(java.io.IOException.class,
                    () -> v.read("myst", java.nio.ByteBuffer.allocate(1)), "read with too-small buffer");
            checkThrowsIO(java.io.IOException.class,
                    () -> v.read("myst", java.nio.ByteBuffer.allocate(0)), "read with zero-capacity buffer");
            // null arguments -> NPE
            checkThrowsIO(NullPointerException.class,
                    () -> v.write(null, java.nio.ByteBuffer.wrap(new byte[1])), "write(null name)");
            checkThrowsIO(NullPointerException.class, () -> v.size(null), "size(null name)");
            checkThrowsIO(NullPointerException.class, () -> v.read("myst", null), "read(null buffer)");
            // empty attribute name is not a legal xattr name -> IOException
            checkThrowsIO(java.io.IOException.class,
                    () -> v.write("", java.nio.ByteBuffer.wrap(new byte[1])), "write with empty name");
            // missing attribute through the Files.getAttribute family
            checkThrowsIO(java.io.IOException.class,
                    () -> java.nio.file.Files.getAttribute(f, "user:never_set"),
                    "getAttribute user:missing");
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
