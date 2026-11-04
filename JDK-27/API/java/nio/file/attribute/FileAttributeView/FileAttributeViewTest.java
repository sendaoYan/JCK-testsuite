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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
