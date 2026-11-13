/*
 * @test
 * @summary java.nio.file.attribute.FileStoreAttributeView conformance: white-box structure + black-box behavior.
 * @run main FileStoreAttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.AttributeView;

public class FileStoreAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.FileStoreAttributeView");
        check(c.isInterface(), "FileStoreAttributeView is an interface");
        check(java.nio.file.attribute.AttributeView.class.isAssignableFrom(c),
              "extends AttributeView (not FileAttributeView)");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("name")), "has method name()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("fsav.txt");
            Files.writeString(file, "s");
            java.nio.file.FileStore store = Files.getFileStore(file);

            // on Linux ext4 no public FileStoreAttributeView is installed: null, not an error
            FileStoreAttributeView view = store.getFileStoreAttributeView(
                    FileStoreAttributeView.class);
            check(view == null || view.name() != null,
                  "store view null-or-named (platform-dependent)");

            // a custom implementation of the interface is trivially usable
            FileStoreAttributeView stub = new FileStoreAttributeView() {
                public String name() { return "jck-store"; }
            };
            check(stub.name().equals("jck-store"), "stub view name");

            // the basic/posix attribute views of a store are the store's own
            // the store's own view type is discoverable and reports non-empty metadata
            check(store.supportsFileAttributeView(
                    java.nio.file.attribute.BasicFileAttributeView.class),
                  "store supports basic views");

            // FileStore.getAttribute goes through a store view
            Object total = store.getAttribute("totalSpace");
            check(total instanceof Long && (Long) total > 0, "totalSpace via store attribute");
        } finally {
            Files.deleteIfExists(dir.resolve("fsav.txt"));
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
            // A volume without a label reports an empty name.
            check(st.name() != null, "store name reported");
            check(st.type() != null && !st.type().isEmpty(), "store type non-empty");
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
            // view support matrix as seen by the store (probed on this machine)
            check(st.supportsFileAttributeView("basic"), "store supports basic");
            check(st.supportsFileAttributeView("posix"), "store supports posix");
            check(!st.supportsFileAttributeView("acl"), "store lacks acl");
            check(st.supportsFileAttributeView(PosixFileAttributeView.class),
                  "store supports PosixFileAttributeView.class");
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
