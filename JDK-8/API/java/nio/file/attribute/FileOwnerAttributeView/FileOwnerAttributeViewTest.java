/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.attribute.FileOwnerAttributeView conformance: hierarchy and methods.
 * @run main FileOwnerAttributeViewTest
 */

import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.UserPrincipal;

public class FileOwnerAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileOwnerAttributeView() {
        check(FileOwnerAttributeView.class.isInterface(), "is interface");
        check(FileAttributeView.class.isAssignableFrom(FileOwnerAttributeView.class), "extends FileAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : FileOwnerAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "getOwner", "setOwner" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testFileOwnerAttributeView();
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
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-fo");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            FileOwnerAttributeView v = java.nio.file.Files.getFileAttributeView(f, FileOwnerAttributeView.class);
            check(v != null, "owner view available");
            check("owner".equals(v.name()), "view name is owner");
            check(v instanceof FileAttributeView, "extends FileAttributeView");
            UserPrincipalLookupService ls = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
            UserPrincipal self = ls.lookupPrincipalByName(System.getProperty("user.name"));
            check(v.getOwner() != null, "getOwner non-null");
            check(v.getOwner().equals(self), "getOwner equals lookup of current user");
            // setOwner round-trip to the file's own owner (guaranteed permitted)
            v.setOwner(self);
            check(v.getOwner().equals(self), "setOwner self round-trip");
            checkThrowsIO(NullPointerException.class, () -> v.setOwner(null), "setOwner(null) NPE");
            // foreign-provider principal is rejected before reaching the filesystem
            final UserPrincipal foreign = new UserPrincipal() {
                public String getName() { return self.getName(); }
                public String toString() { return getName(); }
            };
            checkThrowsIO(java.nio.file.ProviderMismatchException.class, () -> v.setOwner(foreign),
                    "setOwner with foreign principal");
            // operations on a missing file
            FileOwnerAttributeView miss = java.nio.file.Files.getFileAttributeView(dir.resolve("nope"),
                    FileOwnerAttributeView.class);
            checkThrowsIO(java.nio.file.NoSuchFileException.class, () -> miss.getOwner(),
                    "getOwner on missing file");
            checkThrowsIO(java.nio.file.NoSuchFileException.class, () -> miss.setOwner(self),
                    "setOwner on missing file");
            // the posix view also exposes the owner face (its name stays "posix")
            PosixFileAttributeView pv = java.nio.file.Files.getFileAttributeView(f, PosixFileAttributeView.class);
            check(pv instanceof FileOwnerAttributeView, "posix view is-a FileOwnerAttributeView");
            check(pv.getOwner().equals(self), "posix view getOwner");
            pv.setOwner(self);
            check(pv.getOwner().equals(self), "posix view setOwner round-trip");
            check("posix".equals(pv.name()), "posix view name unchanged");
            // Files helpers and the string-attribute family
            check(java.nio.file.Files.getOwner(f).equals(self), "Files.getOwner");
            java.nio.file.Files.setOwner(f, self);
            check(java.nio.file.Files.getOwner(f).equals(self), "Files.setOwner round-trip");
            java.nio.file.Files.setAttribute(f, "owner:owner", self);
            check(java.nio.file.Files.getAttribute(f, "owner:owner").equals(self),
                  "setAttribute owner:owner round-trip");
            java.nio.file.Files.setAttribute(f, "posix:owner", self);
            check(java.nio.file.Files.getAttribute(f, "posix:owner").equals(self),
                  "setAttribute posix:owner round-trip");
            // owner view of a directory works the same way
            FileOwnerAttributeView dv = java.nio.file.Files.getFileAttributeView(dir,
                    FileOwnerAttributeView.class);
            check(dv.getOwner().equals(self), "directory getOwner");
        } finally {
            cleanup(dir);
        }
    }

}
