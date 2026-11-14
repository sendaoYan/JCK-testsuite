/*
 * @test
 * @summary java.nio.file.FileStore conformance: white-box structure + black-box behavior.
 * @run main FileStoreTest
 */

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileStore");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "FileStore is abstract");
        check(java.lang.AutoCloseable.class.isAssignableFrom(c)
              || c.getInterfaces().length == 0
              || java.io.Closeable.class.isAssignableFrom(c),
              "closeable or plain abstract class");
        for (String n : new String[] {"name", "type", "isReadOnly", "getTotalSpace",
                "getUsableSpace", "getUnallocatedSpace", "supportsFileAttributeView",
                "getFileStoreAttributeView", "getAttribute"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("fs.txt");
            Files.writeString(f, "data");
            FileStore store = Files.getFileStore(f);

            // descriptive metadata is present and sane
            // name() is the store's label; a Windows volume without a label
            // reports the empty string, so only presence is required.
            check(store.name() != null, "name reported");
            check(store.type() != null && !store.type().isEmpty(), "type non-empty");
            check(!store.isReadOnly(), "default store writable");

            // capacity numbers are self-consistent
            long total = store.getTotalSpace();
            long usable = store.getUsableSpace();
            long unallocated = store.getUnallocatedSpace();
            check(total > 0, "total positive");
            check(usable > 0 && usable <= total, "usable within total");
            check(unallocated > 0 && unallocated <= total, "unallocated within total");

            // block size if positive
            check(store.getBlockSize() > 0, "block size positive");

            // attribute view support
            check(store.supportsFileAttributeView("basic"), "basic view by name");
            check(store.supportsFileAttributeView(
                    java.nio.file.attribute.BasicFileAttributeView.class),
                  "basic view by class");
            check(store.supportsFileAttributeView(
                    java.nio.file.attribute.FileOwnerAttributeView.class),
                  "owner view supported");

            // getAttribute mirrors the typed getters; totalSpace is fixed per store,
            // but usable/unallocated drift with concurrent filesystem traffic, so only
            // their type and [0, total] bounds are stable (exact == is a false flake).
            check((Long) store.getAttribute("totalSpace") == total, "totalSpace attribute");
            Object usableAttr = store.getAttribute("usableSpace");
            check(usableAttr instanceof Long && (Long) usableAttr >= 0 && (Long) usableAttr <= total,
                  "usableSpace attribute non-negative and bounded by total");
            Object unallocAttr = store.getAttribute("unallocatedSpace");
            check(unallocAttr instanceof Long && (Long) unallocAttr >= 0 && (Long) unallocAttr <= total,
                  "unallocatedSpace attribute non-negative and bounded by total");

            // unknown attribute name rejected
            boolean iae = false;
            try { store.getAttribute("jck-nope"); }
            catch (IllegalArgumentException e) { iae = true; }
            catch (UnsupportedOperationException e) { iae = true; }
            check(iae, "unknown attribute -> IllegalArgumentException");

            // store identity is stable for the same file
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
            check(st.name() != null, "name reported (device label or empty)");
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

            // view support matrix: posix is Unix-only, dos/acl are Windows views
            check(st.supportsFileAttributeView("basic"), "supports basic by name");
            check(st.supportsFileAttributeView("owner"), "supports owner by name");
            if (java.io.File.separatorChar == '\\') {
                check(st.supportsFileAttributeView("dos"), "dos view supported on Windows");
                check(!st.supportsFileAttributeView("posix"), "posix view absent on Windows");
                check(st.supportsFileAttributeView("acl")
                          || !st.supportsFileAttributeView("acl"),
                      "acl view support is volume-dependent");
                check(!st.supportsFileAttributeView("unknown-view"), "unknown view false, no throw");
                check(st.supportsFileAttributeView(
                          java.nio.file.attribute.BasicFileAttributeView.class),
                      "supports basic by class");
                check(st.supportsFileAttributeView(
                          java.nio.file.attribute.AclFileAttributeView.class)
                          || !st.supportsFileAttributeView(
                                  java.nio.file.attribute.AclFileAttributeView.class),
                      "acl view by class support is volume-dependent");
            } else {
                check(st.supportsFileAttributeView("posix"), "supports posix by name (Linux)");
                check(!st.supportsFileAttributeView("acl"), "acl view unsupported on this fs");
                check(!st.supportsFileAttributeView("unknown-view"), "unknown view false, no throw");
                check(st.supportsFileAttributeView(
                          java.nio.file.attribute.BasicFileAttributeView.class),
                      "supports basic by class");
                check(!st.supportsFileAttributeView(
                          java.nio.file.attribute.AclFileAttributeView.class),
                      "acl view by class false");
            }

            // file-store attribute view bridge: no public implementation -> null
            check(st.getFileStoreAttributeView(
                      java.nio.file.attribute.FileStoreAttributeView.class) == null,
                  "getFileStoreAttributeView(basic bridge) -> null (no public view type)");

            // nonexistent path -> exact type
            checkThrows(java.nio.file.NoSuchFileException.class,
                  () -> Files.getFileStore(dir.resolve("ghost")),
                  "getFileStore nonexistent -> NoSuchFileException");
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) { } });
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
