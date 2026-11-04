/*
 * @test
 * @summary java.nio.file.FileStore conformance: white-box structure + black-box behavior (JDK-8).
 * @run main FileStoreTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

public class FileStoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Op { void run() throws Exception; }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
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

    // ===== deepened contract coverage (batch A9): probe-confirmed on this machine =====
    static void testContractMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-store2");
        try {
            Path f = dir.resolve("f");
            Files.write(f, new byte[] {1, 2});
            Path sub = dir.resolve("sub");
            Files.createDirectory(sub);

            FileStore st = Files.getFileStore(f);
            check(st.name() != null && !st.name().isEmpty(), "name non-empty (device on Linux)");
            check(st.type() != null && !st.type().isEmpty(), "type non-empty (fs type)");
            check(!st.isReadOnly(), "writable store");
            check(st.toString() != null && !st.toString().isEmpty(), "toString non-empty");
            check(st.toString().contains(st.name()), "toString contains name");
            check(Files.getFileStore(sub).name().equals(st.name()),
                  "directory in the same store reports the same name");
            check(Files.getFileStore(dir).name().equals(st.name()),
                  "parent directory same store");

            // space triplets: bounded, consistent
            long total = st.getTotalSpace();
            long usable = st.getUsableSpace();
            long unallocated = st.getUnallocatedSpace();
            check(total > 0, "total positive");
            check(usable >= 0 && usable <= total, "usable bounded by total");
            check(unallocated >= 0 && unallocated <= total, "unallocated bounded by total");

            // attribute bridge mirrors the space getters (Long values identical)
            check((Long) st.getAttribute("totalSpace") == total, "attribute totalSpace identical");
            check((Long) st.getAttribute("usableSpace") == usable, "attribute usableSpace identical");
            check((Long) st.getAttribute("unallocatedSpace") == unallocated,
                  "attribute unallocatedSpace identical");
            check(st.getAttribute("totalSpace") instanceof Long, "space attribute boxed Long");
            // exact negative type (probe-confirmed: UOE, not IAE)
            checkThrows(UnsupportedOperationException.class,
                  () -> st.getAttribute("jck-nope"),
                  "getAttribute unknown -> UnsupportedOperationException");
            checkThrows(NullPointerException.class,
                  () -> st.getAttribute(null), "getAttribute(null) NPE");

            // view support matrix (Linux ext4: basic/owner/posix/user; acl absent)
            check(st.supportsFileAttributeView("basic"), "supports basic by name");
            check(st.supportsFileAttributeView("posix"), "supports posix by name (Linux)");
            check(st.supportsFileAttributeView("owner"), "supports owner by name");
            check(!st.supportsFileAttributeView("acl"), "acl view unsupported on this fs");
            check(!st.supportsFileAttributeView("unknown-view"), "unknown view false, no throw");
            check(st.supportsFileAttributeView(BasicFileAttributeView.class),
                  "supports basic by class");
            check(!st.supportsFileAttributeView(AclFileAttributeView.class),
                  "acl view by class false");

            // file-store attribute view bridge: no public implementation -> null
            check(st.getFileStoreAttributeView(FileStoreAttributeView.class) == null,
                  "getFileStoreAttributeView(basic bridge) -> null (no public view type)");

            // nonexistent path -> exact type
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.getFileStore(dir.resolve("ghost")),
                  "getFileStore nonexistent -> NoSuchFileException");
        } finally {
            java.util.List<Path> order = new java.util.ArrayList<>();
            try (java.util.stream.Stream<Path> s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder()).forEach(order::add);
            }
            for (Path p : order) { try { Files.deleteIfExists(p); } catch (Exception ignore) { } }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
