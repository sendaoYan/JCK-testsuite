/*
 * @test
 * @summary java.nio.file.FileStore conformance: white-box structure + black-box behavior (JDK-8).
 * @run main FileStoreTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileStore");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "FileStore is abstract");
        check(c.getInterfaces().length == 0, "plain abstract class (no interfaces)");
        for (String n : new String[] {"name", "type", "isReadOnly", "getTotalSpace",
                "getUsableSpace", "getUnallocatedSpace", "supportsFileAttributeView",
                "getFileStoreAttributeView", "getAttribute"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("fs.txt");
            Files.write(f, "data".getBytes(StandardCharsets.UTF_8));
            FileStore store = Files.getFileStore(f);

            check(store.name() != null && !store.name().isEmpty(), "name non-empty");
            check(store.type() != null && !store.type().isEmpty(), "type non-empty");
            check(!store.isReadOnly(), "default store writable");

            long total = store.getTotalSpace();
            long usable = store.getUsableSpace();
            long unallocated = store.getUnallocatedSpace();
            check(total > 0, "total positive");
            check(usable > 0 && usable <= total, "usable within total");
            check(unallocated > 0 && unallocated <= total, "unallocated within total");

            check(store.supportsFileAttributeView("basic"), "basic view by name");
            check(store.supportsFileAttributeView(
                    java.nio.file.attribute.BasicFileAttributeView.class),
                  "basic view by class");
            check(store.supportsFileAttributeView(
                    java.nio.file.attribute.FileOwnerAttributeView.class),
                  "owner view supported");

            check((Long) store.getAttribute("totalSpace") == total, "totalSpace attribute");
            check((Long) store.getAttribute("usableSpace") == usable, "usableSpace attribute");
            check((Long) store.getAttribute("unallocatedSpace") == unallocated,
                  "unallocatedSpace attribute");

            boolean iae = false;
            try { store.getAttribute("jck-nope"); }
            catch (IllegalArgumentException e) { iae = true; }
            catch (UnsupportedOperationException e) { iae = true; }
            check(iae, "unknown attribute -> IllegalArgumentException");

            check(Files.getFileStore(dir) != null, "store of directory obtainable");
        } finally {
            try { Files.deleteIfExists(dir.resolve("fs.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
