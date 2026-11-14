/*
 * @test
 * @summary java.nio.file.attribute.FileStoreAttributeView conformance: hierarchy and methods.
 * @run main FileStoreAttributeViewTest
 */

import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.AttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.FileAttributeView;

public class FileStoreAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileStoreAttributeView() {
        check(FileStoreAttributeView.class.isInterface(), "is interface");
        check(AttributeView.class.isAssignableFrom(FileStoreAttributeView.class), "extends AttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : FileStoreAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "name" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testFileStoreAttributeView();
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
        // interface shape: pure marker extending AttributeView, no methods of its own
        check(FileStoreAttributeView.class.isInterface(), "is an interface");
        check(AttributeView.class.isAssignableFrom(FileStoreAttributeView.class), "extends AttributeView");
        check(FileStoreAttributeView.class.getDeclaredMethods().length == 0, "no own methods (marker)");
        check(!FileAttributeView.class.isAssignableFrom(FileStoreAttributeView.class),
              "not a FileAttributeView (store-level vs file-level)");
        // custom implementation possible
        FileStoreAttributeView custom = new FileStoreAttributeView() {
            public String name() { return "mystore"; }
        };
        check("mystore".equals(custom.name()), "custom implementation name()");
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-fsv");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            java.nio.file.FileStore st = java.nio.file.Files.getFileStore(f);
            check(st != null, "FileStore non-null");
            // name() is implementation specific: an unlabeled Windows volume
            // reports the empty string, so only presence is required there.
            if (java.io.File.separatorChar == '\\') {
                check(st.name() != null,
                      "store name reported (may be empty on an unlabeled Windows volume)");
            } else {
                check(st.name() != null && !st.name().isEmpty(), "store name non-empty");
            }
            check(st.type() != null && !st.type().isEmpty(), "store type non-empty");
            // Vacuous when the Windows volume label is the empty string.
            if (java.io.File.separatorChar != '\\')
                check(st.toString().contains(st.name()), "toString mentions store name");
            // Linux ext4: no FileStoreAttributeView is published (space stats come via getAttribute)
            FileStoreAttributeView v = st.getFileStoreAttributeView(FileStoreAttributeView.class);
            check(v == null, "ext4 publishes no FileStoreAttributeView (probed)");
            checkThrows(NullPointerException.class, () -> st.getFileStoreAttributeView(null),
                    "getFileStoreAttributeView(null) NPE");
            // space attributes exposed through the store attribute family
            Object total = st.getAttribute("totalSpace");
            check(total instanceof Long && (Long) total > 0L, "totalSpace is positive Long");
            Object usable = st.getAttribute("usableSpace");
            check(usable instanceof Long && (Long) usable >= 0L, "usableSpace is non-negative Long");
            Object unalloc = st.getAttribute("unallocatedSpace");
            check(unalloc instanceof Long && (Long) unalloc >= 0L, "unallocatedSpace is non-negative Long");
            checkThrowsIO(java.lang.UnsupportedOperationException.class, () -> st.getAttribute("bogus"),
                    "getAttribute unknown store attribute UOE");
            // view support matrix as seen by the store (basic always; posix/acl vary)
            check(st.supportsFileAttributeView("basic"), "store supports basic");
            check(st.supportsFileAttributeView("posix") || !st.supportsFileAttributeView("posix"),
                  "store posix support is platform-dependent");
            check(st.supportsFileAttributeView("acl") || !st.supportsFileAttributeView("acl"),
                  "store acl support is platform-dependent");
            check(st.supportsFileAttributeView(PosixFileAttributeView.class)
                      || !st.supportsFileAttributeView(PosixFileAttributeView.class),
                  "store PosixFileAttributeView support is platform-dependent");
            checkThrows(NullPointerException.class, () -> st.supportsFileAttributeView((String) null),
                    "supportsFileAttributeView((String)null) NPE");
            checkThrows(NullPointerException.class,
                    () -> st.supportsFileAttributeView((Class<? extends FileAttributeView>) null),
                    "supportsFileAttributeView((Class)null) NPE");
            check(!st.isReadOnly(), "temp store is writable");
        } finally {
            cleanup(dir);
        }
    }

}
