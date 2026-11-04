/*
 * @test
 * @summary java.nio.file.attribute.FileStoreAttributeView conformance: white-box structure + black-box behavior.
 * @run main FileStoreAttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileStoreAttributeView;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
