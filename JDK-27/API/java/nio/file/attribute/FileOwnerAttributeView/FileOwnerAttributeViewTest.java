/*
 * @test
 * @summary java.nio.file.attribute.FileOwnerAttributeView conformance: white-box structure + black-box behavior.
 * @run main FileOwnerAttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.FileAttributeView;

public class FileOwnerAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.FileOwnerAttributeView");
        check(c.isInterface(), "FileOwnerAttributeView is an interface");
        check(java.nio.file.attribute.FileAttributeView.class.isAssignableFrom(c),
              "extends FileAttributeView");
        for (String n : new String[] {"name", "getOwner", "setOwner"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("own.txt");
            Files.writeString(file, "o");

            FileOwnerAttributeView view = Files.getFileAttributeView(file,
                    FileOwnerAttributeView.class);
            check(view != null, "owner view available");
            check(view.name().equals("owner"), "view name is owner");

            UserPrincipal me = view.getOwner();
            check(me != null && me.getName() != null && !me.getName().isEmpty(), "owner readable");

            // getOwner agrees with Files.getOwner and string-form read
            check(me.equals(Files.getOwner(file)), "consistent with Files.getOwner");
            Object attr = Files.getAttribute(file, "owner:owner");
            check(me.equals(attr), "string-form owner read");

            // setOwner with the same identity is a no-op success
            view.setOwner(me);
            check(view.getOwner().equals(me), "setOwner round trip");

            // lookup service integration: principal from lookup is settable
            java.nio.file.attribute.UserPrincipalLookupService lookup =
                    dir.getFileSystem().getUserPrincipalLookupService();
            UserPrincipal current = lookup.lookupPrincipalByName(me.getName());
            view.setOwner(current);
            check(view.getOwner().equals(me), "principal from lookup accepted");

            // setOwner(null) throws NullPointerException
            boolean npe = false;
            try { view.setOwner(null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "setOwner(null) -> NullPointerException");

            // view of a missing file: getOwner fails with NoSuchFileException
            FileOwnerAttributeView missing = Files.getFileAttributeView(
                    dir.resolve("nope"), FileOwnerAttributeView.class);
            boolean nsf = false;
            try { if (missing != null) missing.getOwner(); }
            catch (java.nio.file.NoSuchFileException e) { nsf = true; }
            check(nsf, "getOwner on missing file -> NoSuchFileException");
        } finally {
            Files.deleteIfExists(dir.resolve("own.txt"));
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
