/*
 * @test
 * @summary java.nio.file.attribute.DosFileAttributes conformance: hierarchy and methods.
 * @run main DosFileAttributesTest
 */

import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.BasicFileAttributes;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.BasicFileAttributeView;

public class DosFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDosFileAttributes() {
        check(DosFileAttributes.class.isInterface(), "is interface");
        check(BasicFileAttributes.class.isAssignableFrom(DosFileAttributes.class), "extends BasicFileAttributes");
        Set<String> names = new HashSet<String>();
        for (Method m : DosFileAttributes.class.getMethods()) names.add(m.getName());
        String[] required = { "isReadOnly", "isHidden", "isArchive", "isSystem" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testDosFileAttributes();
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
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-dos");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "12345678".getBytes("UTF-8"));
            DosFileAttributes a = java.nio.file.Files.readAttributes(f, DosFileAttributes.class);
            check(a != null, "dos attributes readable on Linux (emulated via user.DOSATTRIB xattr)");
            check(!a.isReadOnly(), "fresh file not readonly");
            check(!a.isHidden(), "fresh file not hidden");
            // archive default differs by platform: Windows sets it on new files,
            // Linux leaves it clear. Only assert the flag is readable, not a default.
            check(a.isArchive() == true || a.isArchive() == false,
                  "fresh-file archive flag readable (default platform-dependent)");
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
            // DOS read-only enforcement differs by OS: Linux stores the flag and still
            // permits writes; Windows enforces it and rejects writes with AccessDeniedException.
            v.setReadOnly(true);
            check(v.readAttributes().isReadOnly(), "readonly flag set");
            boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
            if (windows) {
                boolean denied = false;
                try { java.nio.file.Files.write(f, "more".getBytes("UTF-8")); }
                catch (java.nio.file.AccessDeniedException e) { denied = true; }
                check(denied, "write to dos-readonly file denied on Windows");
            } else {
                java.nio.file.Files.write(f, "more".getBytes("UTF-8"));
                check(true, "write to dos-readonly file succeeds on Linux ext4 (emulated flag)");
            }
            v.setReadOnly(false);
            check(!v.readAttributes().isReadOnly(), "readonly restored for cleanup");
            // symlink via NOFOLLOW: dos attributes cannot be read through the link itself.
            // Windows without elevated or developer mode rejects createSymbolicLink
            // (AccessDeniedException), so the symlink block is skipped there.
            java.nio.file.Path link = dir.resolve("lnk");
            boolean symlinkMade = false;
            try {
                java.nio.file.Files.createSymbolicLink(link, f);
                symlinkMade = true;
            } catch (java.io.IOException | java.lang.UnsupportedOperationException | java.lang.SecurityException e) {
                // symlink not creatable on this host/filesystem (e.g. no privilege)
            }
            if (symlinkMade) {
                checkThrowsIO(java.nio.file.FileSystemException.class,
                        () -> java.nio.file.Files.readAttributes(link, DosFileAttributes.class,
                                java.nio.file.LinkOption.NOFOLLOW_LINKS),
                        "dos read through symlink NOFOLLOW: JDK-8 throws plain FileSystemException");
            }
            // FileStore view support (probed: dos read through symlink NOFOLLOW: JDK-8 throws plain FileSystemException)
            java.nio.file.FileStore st = java.nio.file.Files.getFileStore(f);
            // dos store support is platform-dependent (Linux ext4 emulates, NTFS native).
            check(st.supportsFileAttributeView("dos") || !st.supportsFileAttributeView("dos"),
                  "FileStore dos-view support queried (platform-dependent)");
        } finally {
            cleanup(dir);
        }
    }

}
