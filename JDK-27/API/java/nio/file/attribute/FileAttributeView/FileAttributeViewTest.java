/*
 * @test
 * @summary java.nio.file.attribute.FileAttributeView conformance: white-box structure + black-box behavior.
 * @run main FileAttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.FileTime;

public class FileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.FileAttributeView");
        check(c.isInterface(), "FileAttributeView is an interface");
        check(AttributeView.class.isAssignableFrom(c), "extends AttributeView");
        check(c.getDeclaredMethods().length == 0, "adds no methods beyond AttributeView");
        for (Class<?> sub : new Class<?>[] {BasicFileAttributeView.class,
                java.nio.file.attribute.FileOwnerAttributeView.class,
                java.nio.file.attribute.PosixFileAttributeView.class})
            check(c.isAssignableFrom(sub), sub.getSimpleName() + " is a FileAttributeView");
        // FileStoreAttributeView extends AttributeView directly, not FileAttributeView
        check(!c.isAssignableFrom(java.nio.file.attribute.FileStoreAttributeView.class),
              "FileStoreAttributeView is NOT a FileAttributeView");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("fav.txt");
            Files.writeString(file, "v");

            // views are obtainable and usable through the interface type
            FileAttributeView view = Files.getFileAttributeView(file,
                    BasicFileAttributeView.class);
            check(view != null && view.name().equals("basic"), "basic view via interface");
            check(((BasicFileAttributeView) view).readAttributes().size() == 1, "usable");

            // FileStore attribute view is also a FileAttributeView
            java.nio.file.FileStore store = Files.getFileStore(file);
            // on many file systems (e.g. ext4) no public store view is installed;
            // the call must still be well-defined and return null rather than throw
            java.nio.file.attribute.FileStoreAttributeView storeView =
                    store.getFileStoreAttributeView(
                            java.nio.file.attribute.FileStoreAttributeView.class);
            check(storeView == null || storeView.name() != null,
                  "store view null-or-named (ext4 installs none)");

            // the interface type works for supportedFileAttributeView checks
            boolean hasBasic = store.supportsFileAttributeView(BasicFileAttributeView.class);
            check(hasBasic, "basic view always supported on default store");
        } finally {
            Files.deleteIfExists(dir.resolve("fav.txt"));
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
        check(FileAttributeView.class.isInterface(), "is an interface");
        check(AttributeView.class.isAssignableFrom(FileAttributeView.class), "extends AttributeView");
        check(FileAttributeView.class.getDeclaredMethods().length == 0,
              "adds no methods beyond AttributeView (pure marker for attribute views)");
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-fav");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            BasicFileAttributeView b = java.nio.file.Files.getFileAttributeView(f, BasicFileAttributeView.class);
            PosixFileAttributeView p = java.nio.file.Files.getFileAttributeView(f, PosixFileAttributeView.class);
            DosFileAttributeView d = java.nio.file.Files.getFileAttributeView(f, DosFileAttributeView.class);
            UserDefinedFileAttributeView u = java.nio.file.Files.getFileAttributeView(f,
                    UserDefinedFileAttributeView.class);
            FileOwnerAttributeView o = java.nio.file.Files.getFileAttributeView(f, FileOwnerAttributeView.class);
            // every concrete attribute view is a FileAttributeView
            FileAttributeView[] views = { b, p, d, u, o };
            String[] names = { "basic", "posix", "dos", "user", "owner" };
            for (int i = 0; i < views.length; i++) {
                check(views[i] instanceof FileAttributeView, names[i] + " view is a FileAttributeView");
            }
            // inheritance matrix
            check(p instanceof BasicFileAttributeView, "posix view is-a basic view");
            check(d instanceof BasicFileAttributeView, "dos view is-a basic view");
            check(!(u instanceof BasicFileAttributeView), "user view is NOT a basic view");
            check(BasicFileAttributeView.class.isAssignableFrom(PosixFileAttributeView.class),
                  "PosixFileAttributeView extends BasicFileAttributeView");
            check(BasicFileAttributeView.class.isAssignableFrom(DosFileAttributeView.class),
                  "DosFileAttributeView extends BasicFileAttributeView");
            check(FileOwnerAttributeView.class.isAssignableFrom(PosixFileAttributeView.class),
                  "PosixFileAttributeView extends FileOwnerAttributeView");
            check(FileOwnerAttributeView.class.isAssignableFrom(AclFileAttributeView.class),
                  "AclFileAttributeView extends FileOwnerAttributeView");
            check(FileAttributeView.class.isAssignableFrom(FileOwnerAttributeView.class),
                  "FileOwnerAttributeView extends FileAttributeView");
            check(!FileAttributeView.class.isAssignableFrom(FileStoreAttributeView.class),
                  "FileStoreAttributeView is NOT a FileAttributeView");
            // views of the same file from different supertype requests stay functional
            BasicFileAttributeView asBasic = (BasicFileAttributeView) (FileAttributeView) p;
            asBasic.setTimes(FileTime.fromMillis(1600000000000L), null, null);
            check(b.readAttributes().lastModifiedTime().toMillis() == 1600000000000L,
                  "write through one face visible through another");
        } finally {
            cleanup(dir);
        }
    }

}
