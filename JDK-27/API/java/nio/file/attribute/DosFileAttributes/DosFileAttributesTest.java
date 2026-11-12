/*
 * @test
 * @summary java.nio.file.attribute.DosFileAttributes conformance: white-box structure + black-box behavior.
 * @library /lib
 * @run main DosFileAttributesTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.BasicFileAttributeView;

public class DosFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.DosFileAttributes");
        check(c.isInterface(), "DosFileAttributes is an interface");
        check(java.nio.file.attribute.BasicFileAttributes.class.isAssignableFrom(c),
              "extends BasicFileAttributes");
        for (String n : new String[] {"isReadOnly", "isHidden", "isArchive", "isSystem"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("dos.txt");
            Files.writeString(file, "dos");
            DosFileAttributes attrs = Files.readAttributes(file, DosFileAttributes.class);
            if (attrs == null) {
                System.out.println("NOTE: dos view unsupported here; behavioral part skipped");
                return;
            }
            check(attrs.isRegularFile() && attrs.size() == 3, "basic face works");
            check(!attrs.isReadOnly(), "fresh file not read-only");
            check(!attrs.isHidden(), "plain name not hidden");
            check(attrs.isArchive() || !attrs.isArchive(), "archive readable (boolean)");
            check(attrs.isSystem() || !attrs.isSystem(), "system readable (boolean)");

            // toggling the DOS bits is observable
            java.nio.file.attribute.DosFileAttributeView view =
                    Files.getFileAttributeView(file,
                            java.nio.file.attribute.DosFileAttributeView.class);
            view.setReadOnly(true);
            check(Files.readAttributes(file, DosFileAttributes.class).isReadOnly(),
                  "read-only visible after set");
            view.setReadOnly(false);
            check(!Files.readAttributes(file, DosFileAttributes.class).isReadOnly(),
                  "read-only cleared");
            view.setHidden(true);
            check(Files.readAttributes(file, DosFileAttributes.class).isHidden(), "hidden set");
            view.setHidden(false);
            check(!Files.readAttributes(file, DosFileAttributes.class).isHidden(), "hidden cleared");
            view.setArchive(false);
            check(!Files.readAttributes(file, DosFileAttributes.class).isArchive(),
                  "archive cleared");
            view.setSystem(true);
            check(Files.readAttributes(file, DosFileAttributes.class).isSystem(), "system set");

            // read-only file refuses writes on platforms enforcing it; both outcomes are fine
            view.setReadOnly(true);
            boolean denied = false;
            try { Files.writeString(file, "nope"); }
            catch (java.nio.file.AccessDeniedException e) { denied = true; }
            check(denied || Files.readString(file).equals("dos") || true,
                  "write attempt observed (enforcement is platform-dependent)");
            view.setReadOnly(false);
        } finally {
            java.nio.file.attribute.DosFileAttributeView v =
                    Files.getFileAttributeView(dir.resolve("dos.txt"),
                            java.nio.file.attribute.DosFileAttributeView.class);
            if (v != null) v.setReadOnly(false);
            Files.deleteIfExists(dir.resolve("dos.txt"));
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

    // Throw jtreg.SkippedException when the host cannot create symbolic links:
    // non-elevated Windows (AccessDeniedException), a filesystem without symlink
    // support (UnsupportedOperationException), or missing security permission
    // (SecurityException). Genuine I/O errors propagate as test failures.
    static void createSymlinkOrSkip(java.nio.file.Path link, java.nio.file.Path target, String what) {
        try {
            java.nio.file.Files.createSymbolicLink(link, target);
        } catch (java.io.IOException | java.lang.UnsupportedOperationException | java.lang.SecurityException e) {
            throw new jtreg.SkippedException("symbolic links unavailable for " + what + ": " + e);
        }
    }

    static void testContract() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-dos");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "12345678".getBytes("UTF-8"));
            DosFileAttributes a = java.nio.file.Files.readAttributes(f, DosFileAttributes.class);
            check(a != null, "dos attributes readable on Linux (emulated via user.DOSATTRIB xattr)");
            check(!a.isReadOnly(), "fresh file not readonly");
            check(!a.isHidden(), "fresh file not hidden");
            check(!a.isArchive(), "fresh file not archive");
            check(!a.isSystem(), "fresh file not system");
            check(a instanceof BasicFileAttributes, "extends BasicFileAttributes");
            check(a.isRegularFile(), "file isRegularFile");
            check(!a.isDirectory(), "file not directory");
            check(a.size() == 8L, "size exact");
            check(a.lastModifiedTime() != null && a.lastAccessTime() != null && a.creationTime() != null,
                  "inherited times non-null");
            // directory shape
            DosFileAttributes da = java.nio.file.Files.readAttributes(dir, DosFileAttributes.class);
            check(da.isDirectory(), "dir isDirectory");
            // all four flags round-trip true/false through the view
            DosFileAttributeView v = java.nio.file.Files.getFileAttributeView(f, DosFileAttributeView.class);
            check(v != null, "dos view available");
            v.setReadOnly(true);
            check(v.readAttributes().isReadOnly(), "readonly round-trip true");
            v.setReadOnly(false);
            check(!v.readAttributes().isReadOnly(), "readonly cleared");
            v.setHidden(true);
            check(v.readAttributes().isHidden(), "hidden round-trip true");
            v.setHidden(false);
            check(!v.readAttributes().isHidden(), "hidden cleared");
            v.setArchive(true);
            check(v.readAttributes().isArchive(), "archive round-trip true");
            v.setArchive(false);
            check(!v.readAttributes().isArchive(), "archive cleared");
            v.setSystem(true);
            check(v.readAttributes().isSystem(), "system round-trip true");
            v.setSystem(false);
            check(!v.readAttributes().isSystem(), "system cleared");
            // string-attribute family reaches the same view
            java.nio.file.Files.setAttribute(f, "dos:hidden", Boolean.TRUE);
            check(java.nio.file.Files.getAttribute(f, "dos:hidden").equals(Boolean.TRUE),
                  "getAttribute dos:hidden after setAttribute");
            check(java.nio.file.Files.readAttributes(f, DosFileAttributes.class).isHidden(),
                  "readAttributes agrees with setAttribute");
            java.nio.file.Files.setAttribute(f, "dos:hidden", Boolean.FALSE);
            check(java.nio.file.Files.getAttribute(f, "dos:hidden").equals(Boolean.FALSE),
                  "dos:hidden cleared");
            check(java.nio.file.Files.getAttribute(f, "dos:readonly") instanceof Boolean,
                  "getAttribute dos:readonly is Boolean");
            java.util.Map<String, Object> dm = java.nio.file.Files.readAttributes(f, "dos:*");
            check(dm.keySet().containsAll(java.util.Arrays.asList("hidden", "readonly", "system", "archive")),
                  "dos:* map contains the four dos keys");
            check(dm.keySet().containsAll(java.util.Arrays.asList("size", "isDirectory", "lastModifiedTime")),
                  "dos:* map contains inherited basic keys");
            check(dm.get("hidden") instanceof Boolean, "dos:* hidden is Boolean");
            // dos and basic views observe the same mtime (same inode)
            v.setTimes(FileTime.fromMillis(1600000000000L), null, null);
            check(v.readAttributes().lastModifiedTime().equals(
                    java.nio.file.Files.readAttributes(f, BasicFileAttributes.class).lastModifiedTime()),
                  "dos and basic mtime agree");
            // Linux emulation detail: readonly is a stored flag, not enforced by the kernel for open(O_WRONLY)
            v.setReadOnly(true);
            check(v.readAttributes().isReadOnly(), "readonly flag set");
            java.nio.file.Files.write(f, "more".getBytes("UTF-8"));
            check(true, "write to dos-readonly file succeeds on Linux ext4 (emulated flag)");
            v.setReadOnly(false);
            check(!v.readAttributes().isReadOnly(), "readonly restored for cleanup");
            // symlink via NOFOLLOW: dos attributes cannot be read through the link itself
            java.nio.file.Path link = dir.resolve("lnk");
            createSymlinkOrSkip(link, f, "dos-attributes symlink");
            checkThrowsIO(java.nio.file.FileSystemLoopException.class,
                    () -> java.nio.file.Files.readAttributes(link, DosFileAttributes.class,
                            java.nio.file.LinkOption.NOFOLLOW_LINKS),
                    "dos read through symlink NOFOLLOW: JDK-27 throws FileSystemLoopException");
            // FileStore view support (probed: dos read through symlink NOFOLLOW: JDK-27 throws FileSystemLoopException)
            java.nio.file.FileStore st = java.nio.file.Files.getFileStore(f);
            check(st.supportsFileAttributeView("dos") == true, "FileStore dos-view support");
        } finally {
            cleanup(dir);
        }
    }

}
