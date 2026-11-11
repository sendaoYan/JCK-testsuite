/*
 * @test
 * @summary java.nio.file.attribute.DosFileAttributeView conformance: white-box structure + black-box behavior.
 * @run main DosFileAttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

public class DosFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.DosFileAttributeView");
        check(c.isInterface(), "DosFileAttributeView is an interface");
        check(java.nio.file.attribute.BasicFileAttributeView.class.isAssignableFrom(c),
              "extends BasicFileAttributeView");
        for (String n : new String[] {"name", "readAttributes", "setReadOnly", "setHidden",
                "setArchive", "setSystem"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("dv.txt");
            Files.writeString(file, "v");
            DosFileAttributeView view = Files.getFileAttributeView(file,
                    DosFileAttributeView.class);
            if (view == null) {
                System.out.println("NOTE: dos view unsupported here; behavioral part skipped");
                return;
            }
            check(view.name().equals("dos"), "view name is dos");

            // readAttributes through the view
            DosFileAttributes attrs = view.readAttributes();
            check(attrs.isRegularFile() && attrs.size() == 1, "read via dos view");

            // inherited setTimes works through the dos view
            FileTime fixed = FileTime.from(Instant.parse("2020-06-01T12:00:00Z"));
            view.setTimes(fixed, null, null);
            check(view.readAttributes().lastModifiedTime().toMillis() == fixed.toMillis(),
                  "setTimes via dos view");

            // string-form access through the dos scheme
            Object ro = Files.getAttribute(file, "dos:readonly");
            check(ro instanceof Boolean, "dos:readonly readable");

            // missing file: operations fail with NoSuchFileException
            DosFileAttributeView missing = Files.getFileAttributeView(dir.resolve("gone"),
                    DosFileAttributeView.class);
            boolean nsf = false;
            try { if (missing != null) missing.readAttributes(); }
            catch (java.nio.file.NoSuchFileException e) { nsf = true; }
            check(nsf, "readAttributes missing -> NoSuchFileException");
            boolean nsf2 = false;
            try { if (missing != null) missing.setHidden(true); }
            catch (java.nio.file.NoSuchFileException e) { nsf2 = true; }
            check(nsf2, "setHidden missing -> NoSuchFileException");
        } finally {
            Files.deleteIfExists(dir.resolve("dv.txt"));
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
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-dosv");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            DosFileAttributeView v = java.nio.file.Files.getFileAttributeView(f, DosFileAttributeView.class);
            check(v != null, "dos view available on Linux");
            check("dos".equals(v.name()), "view name is dos");
            check(v instanceof BasicFileAttributeView, "extends BasicFileAttributeView");
            check(v instanceof FileAttributeView, "extends FileAttributeView (transitively)");
            check(v.readAttributes() instanceof DosFileAttributes, "readAttributes returns DosFileAttributes");
            check(v.readAttributes() instanceof BasicFileAttributes, "attributes are BasicFileAttributes too");
            // four setter/getter round trips
            v.setHidden(true);
            check(v.readAttributes().isHidden(), "setHidden true");
            v.setHidden(false);
            check(!v.readAttributes().isHidden(), "setHidden false");
            v.setArchive(true);
            check(v.readAttributes().isArchive(), "setArchive true");
            v.setArchive(false);
            check(!v.readAttributes().isArchive(), "setArchive false");
            v.setSystem(true);
            check(v.readAttributes().isSystem(), "setSystem true");
            v.setSystem(false);
            check(!v.readAttributes().isSystem(), "setSystem false");
            v.setReadOnly(true);
            check(v.readAttributes().isReadOnly(), "setReadOnly true");
            v.setReadOnly(false);
            check(!v.readAttributes().isReadOnly(), "setReadOnly false");
            // inherited setTimes works through the dos view
            v.setTimes(FileTime.fromMillis(1700000000000L), null, null);
            check(v.readAttributes().lastModifiedTime().toMillis() == 1700000000000L,
                  "dos view setTimes mtime round-trip");
            // basic view and dos view see the same timestamp
            check(java.nio.file.Files.getFileAttributeView(f, BasicFileAttributeView.class).readAttributes()
                    .lastModifiedTime().equals(v.readAttributes().lastModifiedTime()),
                  "basic and dos views agree on mtime");
            // string-attribute family
            java.nio.file.Files.setAttribute(f, "dos:archive", Boolean.TRUE);
            check(java.nio.file.Files.getAttribute(f, "dos:archive").equals(Boolean.TRUE),
                  "setAttribute dos:archive round-trip");
            java.nio.file.Files.setAttribute(f, "dos:archive", Boolean.FALSE);
            check(java.nio.file.Files.getAttribute(f, "dos:archive").equals(Boolean.FALSE),
                  "dos:archive cleared");
            // DOS read-only enforcement differs by OS: Linux stores the flag as a
            // DOSATTRIB xattr and still permits writes; Windows enforces it at the
            // OS level and rejects writes with AccessDeniedException.
            v.setReadOnly(true);
            boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
            if (windows) {
                boolean denied = false;
                try { java.nio.file.Files.write(f, "yy".getBytes("UTF-8")); }
                catch (java.nio.file.AccessDeniedException e) { denied = true; }
                check(denied, "write under dos-readonly denied on Windows");
            } else {
                java.nio.file.Files.write(f, "yy".getBytes("UTF-8"));
                check(v.readAttributes().size() == 2L, "write under dos-readonly succeeds on Linux ext4");
            }
            v.setReadOnly(false);
            // operations on missing file
            DosFileAttributeView miss = java.nio.file.Files.getFileAttributeView(dir.resolve("nope"),
                    DosFileAttributeView.class);
            checkThrowsIO(java.nio.file.NoSuchFileException.class, () -> miss.setHidden(true),
                    "setHidden on missing file");
            checkThrowsIO(java.nio.file.NoSuchFileException.class,
                    () -> miss.setTimes(FileTime.fromMillis(1L), null, null), "setTimes on missing file");
            // registered view names
            check(java.nio.file.FileSystems.getDefault().supportedFileAttributeViews().contains("dos"),
                  "dos in supportedFileAttributeViews");
            java.nio.file.FileStore st = java.nio.file.Files.getFileStore(f);
            check(st.supportsFileAttributeView("dos") == true, "FileStore dos-view support (probed)");
            check(st.supportsFileAttributeView("basic"), "FileStore basic-view support (contrast)");
        } finally {
            cleanup(dir);
        }
    }

}
