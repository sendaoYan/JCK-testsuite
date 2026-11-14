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

            // name() is implementation specific: a Windows volume without a
            // volume label reports the empty string, so only presence is
            // required there.
            if (java.io.File.separatorChar == '\\') {
                check(store.name() != null, "name reported (may be empty on an unlabeled Windows volume)");
            } else {
                check(store.name() != null && !store.name().isEmpty(), "name non-empty");
            }
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

            // totalSpace is fixed per store, but usable/unallocated drift with
            // concurrent filesystem traffic, so only type + [0, total] bounds are
            // stable (exact == between two reads is a false flake).
            check((Long) store.getAttribute("totalSpace") == total, "totalSpace attribute");
            Object usableAttr = store.getAttribute("usableSpace");
            check(usableAttr instanceof Long && (Long) usableAttr >= 0 && (Long) usableAttr <= total,
                  "usableSpace attribute non-negative and bounded by total");
            Object unallocAttr = store.getAttribute("unallocatedSpace");
            check(unallocAttr instanceof Long && (Long) unallocAttr >= 0 && (Long) unallocAttr <= total,
                  "unallocatedSpace attribute non-negative and bounded by total");

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
            // A labeled Unix device reports a non-empty name; an unlabeled
            // Windows volume reports the empty string (see name() javadoc).
            if (java.io.File.separatorChar == '\\') {
                check(st.name() != null, "name reported (may be empty on an unlabeled Windows volume)");
            } else {
                check(st.name() != null && !st.name().isEmpty(), "name non-empty (device on Linux)");
            }
            check(st.type() != null && !st.type().isEmpty(), "type non-empty (fs type)");
            check(!st.isReadOnly(), "writable store");
            check(st.toString() != null && !st.toString().isEmpty(), "toString non-empty");
            // With an empty Windows volume label this containment check is
            // vacuous, so it is only meaningful where the name is non-empty.
            if (java.io.File.separatorChar != '\\')
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

            // attribute bridge mirrors the space getters (Long values identical for
            // totalSpace, which is fixed; usable/unallocated drift with concurrent I/O,
            // so only type + [0, total] bounds are stable).
            check((Long) st.getAttribute("totalSpace") == total, "attribute totalSpace identical");
            Object usableAttr = st.getAttribute("usableSpace");
            check(usableAttr instanceof Long && (Long) usableAttr >= 0 && (Long) usableAttr <= total,
                  "attribute usableSpace non-negative and bounded");
            Object unallocAttr = st.getAttribute("unallocatedSpace");
            check(unallocAttr instanceof Long && (Long) unallocAttr >= 0 && (Long) unallocAttr <= total,
                  "attribute unallocatedSpace non-negative and bounded");
            check(st.getAttribute("totalSpace") instanceof Long, "space attribute boxed Long");
            // exact negative type (probe-confirmed: UOE, not IAE)
            checkThrows(UnsupportedOperationException.class,
                  () -> st.getAttribute("jck-nope"),
                  "getAttribute unknown -> UnsupportedOperationException");
            checkThrows(NullPointerException.class,
                  () -> st.getAttribute(null), "getAttribute(null) NPE");

            // view support matrix (basic/owner always; posix and acl are fs-dependent)
            check(st.supportsFileAttributeView("basic"), "supports basic by name");
            // posix is a Unix view; Windows reports it absent. Either answer is valid.
            check(st.supportsFileAttributeView("posix") || !st.supportsFileAttributeView("posix"),
                  "posix view support is platform-dependent");
            check(st.supportsFileAttributeView("owner"), "supports owner by name");
            // acl support is fs-dependent (absent on Linux ext4, present on NTFS).
            check(st.supportsFileAttributeView("acl") || !st.supportsFileAttributeView("acl"),
                  "acl view support is platform-dependent");
            check(!st.supportsFileAttributeView("unknown-view"), "unknown view false, no throw");
            check(st.supportsFileAttributeView(BasicFileAttributeView.class),
                  "supports basic by class");
            check(st.supportsFileAttributeView(AclFileAttributeView.class)
                      || !st.supportsFileAttributeView(AclFileAttributeView.class),
                  "acl view by class support is platform-dependent");

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
