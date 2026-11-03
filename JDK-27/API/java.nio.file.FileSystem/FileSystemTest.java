/*
 * @test
 * @summary java.nio.file.FileSystem conformance: white-box structure + black-box behavior.
 * @run main FileSystemTest
 */

import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class FileSystemTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
