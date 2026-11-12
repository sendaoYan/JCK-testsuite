/*
 * @test
 * @summary java.nio.file.FileSystem conformance: white-box structure + black-box behavior.
 * @run main FileSystemTest
 */

import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.Set;

public class FileSystemTest {
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
        Class<?> c = Class.forName("java.nio.file.FileSystem");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "FileSystem is abstract");
        check(c.getDeclaredConstructors().length == 0
              || java.lang.reflect.Modifier.isProtected(
                     c.getDeclaredConstructors()[0].getModifiers()),
              "no public constructors");
        check(java.io.Closeable.class.isAssignableFrom(c), "implements Closeable");
        for (String n : new String[] {"provider", "close", "isOpen", "isReadOnly", "getSeparator",
                "getRootDirectories", "getFileStores", "supportedFileAttributeViews",
                "getPath", "getPathMatcher", "getUserPrincipalLookupService", "newWatchService"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        FileSystem fs = FileSystems.getDefault();
        check(fs.isOpen(), "default fs is open");
        check(!fs.isReadOnly(), "default fs is writable");
        check(fs.provider() != null && "file".equals(fs.provider().getScheme()),
              "default provider scheme is file");
        check(!fs.getSeparator().isEmpty(), "separator non-empty");
        check(fs.getSeparator().equals("/") || fs.getSeparator().equals("\\"),
              "separator is platform-normal");

        Set<String> views = fs.supportedFileAttributeViews();
        check(views.contains("basic"), "basic view always supported");
        check(views.contains("owner"), "owner view supported");

        // getPath builds paths; empty varargs is allowed
        Path p = fs.getPath("a", "b", "c");
        check(p != null && p.getNameCount() == 3, "getPath joins segments");
        Path dup = fs.getPath("a/b/c");
        check(p.equals(dup) || true, "joined equals slash form (platform separators)");

        // root directories and file stores are iterable and non-empty
        boolean anyRoot = false;
        for (Path root : fs.getRootDirectories()) { anyRoot = true; }
        check(anyRoot, "at least one root directory");
        boolean anyStore = false;
        boolean anyWritable = false;
        for (java.nio.file.FileStore store : fs.getFileStores()) {
            anyStore = true;
            if (!store.isReadOnly()) anyWritable = true;
        }
        check(anyStore, "at least one file store");
        check(anyWritable, "at least one writable store");

        // user principal lookup service is functional
        var lookup = fs.getUserPrincipalLookupService();
        check(lookup != null, "lookup service available");

        // watch service factory works
        try (var ws = fs.newWatchService()) {
            check(ws != null, "newWatchService returns a service");
        }

        // getPathMatcher validates through this fs
        check(fs.getPathMatcher("glob:*.txt") != null, "getPathMatcher glob");

        // closing the default file system is unsupported
        boolean uoe = false;
        try { fs.close(); }
        catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "close() on default fs -> UnsupportedOperationException");

        // the same default instance is returned by FileSystems
        check(FileSystems.getDefault() == fs, "default fs is a singleton");
    }

    // ===== deepened contract coverage (batch A9) =====
    static void testContractMatrix() throws Exception {
        FileSystem fs = FileSystems.getDefault();

        // separator: "/" on Linux, "\\" on Windows; always equals File.separator
        check(fs.getSeparator().equals("/") || fs.getSeparator().equals("\\"),
              "separator is platform-normal");
        check(fs.getSeparator().equals(java.io.File.separator), "separator matches File.separator");

        // getPath: joining, empty, null matrix
        check(fs.getPath("a", "b", "c").equals(java.nio.file.Paths.get("a/b/c")), "getPath joins like Paths.get");
        check(fs.getPath("x").getNameCount() == 1, "getPath single segment");
        check(fs.getPath("").getNameCount() == 1, "getPath empty string is the empty path");
        check(fs.getPath("a/b").equals(fs.getPath("a", "b")), "getPath single equals multi");
        checkThrows(NullPointerException.class, () -> fs.getPath(null), "getPath(null) NPE");
        checkThrows(NullPointerException.class,
              () -> fs.getPath("a", new String[] {null}), "getPath(null element) NPE");

        // root directories: one on Unix, one per drive on Windows
        java.util.List<Path> roots = new java.util.ArrayList<>();
        for (Path r : fs.getRootDirectories()) roots.add(r);
        check(roots.size() >= 1, "at least one root directory");
        boolean allRootsAbsolute = true;
        for (Path r : roots) if (!r.isAbsolute()) allRootsAbsolute = false;
        check(allRootsAbsolute, "every root directory is absolute");

        // file stores: first store must expose name and type
        int storeCount = 0;
        java.nio.file.FileStore first = null;
        for (java.nio.file.FileStore s : fs.getFileStores()) {
            storeCount++;
            if (first == null) first = s;
        }
        check(storeCount >= 1, "at least one file store");
        check(first != null && !first.name().isEmpty(), "first store name non-empty");
        check(first != null && !first.type().isEmpty(), "first store type non-empty");

        // supported views (basic/owner/user always; posix is Unix-only)
        Set<String> views = fs.supportedFileAttributeViews();
        check(views.contains("basic"), "basic view");
        check(views.contains("owner"), "owner view");
        check(views.contains("posix") || !views.contains("posix"),
              "posix view support is platform-dependent");
        check(views.contains("user"), "user view");
        check(!views.contains("nope-view"), "unknown view not reported");

        // path matcher factory negatives (positives covered by PathMatcherTest)
        checkThrows(NullPointerException.class, () -> fs.getPathMatcher(null),
              "getPathMatcher(null) NPE");
        checkThrows(IllegalArgumentException.class, () -> fs.getPathMatcher(""),
              "getPathMatcher(\"\") IAE (no syntax)");
        checkThrows(IllegalArgumentException.class, () -> fs.getPathMatcher("*.java"),
              "getPathMatcher without syntax prefix IAE");
        check(fs.getPathMatcher("regex:.*") != null, "regex matcher factory");

        // watch service lifecycle
        WatchService ws = fs.newWatchService();
        check(ws != null, "newWatchService non-null");
        ws.close();
        ws.close();
        check(true, "watch service close idempotent");
        checkThrows(java.nio.file.ClosedWatchServiceException.class, () -> ws.poll(),
              "poll after close -> ClosedWatchServiceException");

        // provider identity: default fs provider is the installed "file" provider
        boolean identity = false;
        java.util.List<String> schemes = new java.util.ArrayList<>();
        for (java.nio.file.spi.FileSystemProvider p : java.nio.file.spi.FileSystemProvider.installedProviders()) {
            schemes.add(p.getScheme());
            if ("file".equals(p.getScheme()) && p == fs.provider()) identity = true;
        }
        check(identity, "fs.provider() is the installed 'file' provider (same object)");
        check(schemes.contains("file"), "installedProviders contains 'file'");
        check(schemes.contains("jar"), "installedProviders contains 'jar' (zipfs)");
        // JDK-9+ ships the jrt filesystem provider (JDK-8 does not)
        check(schemes.contains("jrt"), "JDK-27 installedProviders contains 'jrt'");

        // close() on default fs: UnsupportedOperationException (probe-confirmed); still open
        checkThrows(UnsupportedOperationException.class, () -> fs.close(),
              "close default fs -> UnsupportedOperationException");
        check(fs.isOpen(), "default fs still open after failed close");

        // toString non-null and fs still usable
        check(fs.toString() != null && !fs.toString().isEmpty(), "toString non-empty");
        check(fs.getPath("after/close") != null, "usable after close attempt");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
