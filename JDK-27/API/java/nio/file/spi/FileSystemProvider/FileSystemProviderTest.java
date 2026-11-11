/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.spi.FileSystemProvider conformance: white-box structure + black-box behavior.
 * @run main FileSystemProviderTest
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

public class FileSystemProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.spi.FileSystemProvider");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getDeclaredConstructors().length == 0
              || java.lang.reflect.Modifier.isProtected(
                     c.getDeclaredConstructors()[0].getModifiers()),
              "protected constructor");
        for (String n : new String[] {"installedProviders", "getScheme", "newFileSystem",
                "getFileSystem", "getPath", "newByteChannel", "newDirectoryStream",
                "createDirectory", "delete", "copy", "move", "isSameFile", "isHidden",
                "getFileStore", "checkAccess", "getFileAttributeView", "readAttributes",
                "setAttribute"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // installed providers contain the default "file" provider
        var providers = FileSystemProvider.installedProviders();
        check(providers != null && !providers.isEmpty(), "providers installed");
        FileSystemProvider file = null;
        for (FileSystemProvider p : providers) {
            if (p.getScheme().equals("file")) file = p;
        }
        check(file != null, "file provider present");
        check(FileSystems.getDefault().provider() == file,
              "default fs wired to the file provider");

        // the zip provider is also installed
        boolean zip = providers.stream().anyMatch(p -> p.getScheme().equals("jar"));
        check(zip, "jar provider installed");

        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("p.txt");
            Files.writeString(f, "provider");

            // newByteChannel through the provider
            try (SeekableByteChannel ch = file.newByteChannel(f,
                    Set.of(StandardOpenOption.READ))) {
                check(ch.size() == 8, "channel size via provider");
            }

            // newDirectoryStream + filter (glob convenience lives in Files)
            try (DirectoryStream<Path> ds = file.newDirectoryStream(dir,
                    p -> p.getFileName().toString().endsWith(".txt"))) {
                java.util.List<Path> list = new java.util.ArrayList<>();
                ds.forEach(list::add);
                check(list.size() == 1 && list.get(0).getFileName().toString().equals("p.txt"),
                      "directory stream filtered via provider");
            }

            // createDirectory / delete via provider
            Path sub = dir.resolve("sub");
            file.createDirectory(sub);
            check(Files.isDirectory(sub), "createDirectory via provider");
            file.delete(sub);
            check(!Files.exists(sub), "delete via provider");

            // copy/move/isSameFile via provider
            Path copy = dir.resolve("copy.txt");
            file.copy(f, copy);
            check(Files.readString(copy).equals("provider"), "copy via provider");
            check(file.isSameFile(f, f), "isSameFile identity");
            check(!file.isSameFile(f, copy), "distinct files differ");
            Path moved = dir.resolve("moved.txt");
            file.move(copy, moved);
            check(Files.exists(moved) && !Files.exists(copy), "move via provider");

            // attribute APIs via provider
            BasicFileAttributes attrs =
                    file.readAttributes(f, BasicFileAttributes.class);
            check(attrs.isRegularFile() && attrs.size() == 8, "readAttributes via provider");
            var view = file.getFileAttributeView(f,
                    java.nio.file.attribute.BasicFileAttributeView.class);
            check(view != null && view.readAttributes().size() == 8, "attribute view");
            file.setAttribute(f, "basic:lastModifiedTime",
                    java.nio.file.attribute.FileTime.from(
                            java.time.Instant.parse("2020-05-05T05:05:05Z")));
            check(Files.getLastModifiedTime(f).toMillis()
                  == java.time.Instant.parse("2020-05-05T05:05:05Z").toEpochMilli(),
                  "setAttribute via provider");

            // checkAccess + hidden
            file.checkAccess(f, AccessMode.READ, AccessMode.WRITE);
            check(true, "checkAccess passes");
            check(!file.isHidden(f), "plain file not hidden");

            // newInputStream/newOutputStream conveniences
            try (InputStream in = file.newInputStream(f)) {
                check(in.read() == 'p', "newInputStream reads");
            }
            try (OutputStream out = file.newOutputStream(dir.resolve("os.txt"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                out.write('o');
            }
            check(Files.readString(dir.resolve("os.txt")).equals("o"), "newOutputStream writes");

            // foreign provider rejection: default provider refuses zip paths
            Path zipf = Files.createTempFile("jck", ".zip");
            try {
                try (var zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(zipf))) {
                    zos.putNextEntry(new java.util.zip.ZipEntry("z.txt"));
                }
                try (FileSystem zfs = FileSystems.newFileSystem(zipf)) {
                    Path zpath = zfs.getPath("/z.txt");
                    boolean mismatch = false;
                    try {
                        file.readAttributes(zpath, BasicFileAttributes.class);
                    } catch (ProviderMismatchException e) { mismatch = true; }
                    check(mismatch, "foreign path -> ProviderMismatchException");
                }
            } finally {
                Files.deleteIfExists(zipf);
            }

            // getFileSystem only answers the default fs identity "file:///";
            // getPath reconstructs any concrete file URI
            check(file.getFileSystem(URI.create("file:///")).isOpen(),
                  "getFileSystem(file:///) default");
            check(file.getPath(f.toUri()).equals(f.toAbsolutePath()),
                  "getPath(file URI) reconstructs the path");
            boolean bad = false;
            try { file.getFileSystem(f.toUri()); }
            catch (IllegalArgumentException e) { bad = true; }
            check(bad, "getFileSystem rejects non-root file URIs");
            // the default provider already has its filesystem: recreating is refused
            boolean already = false;
            try { file.newFileSystem(URI.create("file:///"), Map.of()); }
            catch (java.nio.file.FileSystemAlreadyExistsException e) { already = true; }
            check(already, "default provider newFileSystem -> FileSystemAlreadyExistsException");
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) { } });
            }
        }
    }

    // ---- appended: JDK-27 provider surface additions ----
    static void testJdk27Surface() throws Exception {
        Class<?> c = Class.forName("java.nio.file.spi.FileSystemProvider");
        check(java.util.Arrays.stream(c.getMethods())
                      .anyMatch(m -> m.getName().equals("exists")),
              "JDK-27 adds exists(Path,LinkOption...)");
        check(java.util.Arrays.stream(c.getMethods())
                      .anyMatch(m -> m.getName().equals("readAttributesIfExists")),
              "JDK-27 adds readAttributesIfExists(Path,Class,LinkOption...)");
        check(java.lang.reflect.Modifier.isStatic(
                      c.getMethod("installedProviders").getModifiers()),
              "installedProviders is static");
        // JDK-27 declares only the protected no-arg constructor (JDK-8 additionally
        // carries a private Void security-gate constructor — recorded difference)
        boolean hasProtected = false;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            if (java.lang.reflect.Modifier.isProtected(ct.getModifiers())) hasProtected = true;
        check(hasProtected, "protected constructor for subclasses");
        check(c.getDeclaredConstructors().length == 1,
              "single constructor on JDK-27 (no security gate)");

        // provider registry: JDK-27 ships file + jar + jrt (measured)
        java.util.Set<String> schemes = new java.util.HashSet<>();
        for (FileSystemProvider p : FileSystemProvider.installedProviders())
            schemes.add(p.getScheme());
        check(schemes.contains("jar"), "jar (zipfs) provider installed (measured)");
        check(schemes.contains("jrt"), "jrt provider installed (measured; JDK-8 has none)");

        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("n.txt");
            Files.writeString(f, "x");
            Path broken = dir.resolve("broken");
            Files.createSymbolicLink(broken, dir.resolve("nowhere"));

            var file = FileSystems.getDefault().provider();
            check(file.exists(f), "exists(Path) true for a present file");
            check(!file.exists(dir.resolve("ghost")), "exists(Path) false when missing");
            check(file.exists(broken, LinkOption.NOFOLLOW_LINKS),
                  "exists with NOFOLLOW sees a dangling link");
            check(!file.exists(broken), "exists follows a dangling link to nothing");
            check(file.readAttributesIfExists(f, BasicFileAttributes.class) != null,
                  "readAttributesIfExists returns attrs for a present file");
            check(file.readAttributesIfExists(dir.resolve("ghost"),
                    BasicFileAttributes.class) == null,
                  "readAttributesIfExists returns null when missing");
        } finally {
            try (var st = Files.walk(dir)) {
                java.util.List<Path> l = new java.util.ArrayList<>();
                st.forEach(l::add);
                java.util.Collections.reverse(l);
                for (Path p : l) { try { Files.deleteIfExists(p); } catch (Exception ignore) { } }
            }
        }
    }

    // ---- appended: negative and null matrix (exact exception types, measured) ----
    interface IoAction { void run() throws java.io.IOException; }

    static void expect(Class<? extends Throwable> ex, IoAction r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testNegativeMatrix() throws Exception {
        final FileSystemProvider file = FileSystems.getDefault().provider();
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("neg.txt");
            Files.writeString(f, "provider");
            // URI handling
            expect(IllegalArgumentException.class,
                  () -> file.getPath(URI.create("mailto:x")),
                  "getPath(non-file URI) -> IllegalArgumentException");
            expect(IllegalArgumentException.class,
                  () -> file.getPath(URI.create("file://host/tmp/x")),
                  "getPath(authority URI) -> IllegalArgumentException");
            expect(NullPointerException.class, () -> file.getPath(null),
                  "getPath(null) -> NullPointerException");
            expect(IllegalArgumentException.class,
                  () -> file.newFileSystem(URI.create("mailto:x"), Map.of()),
                  "newFileSystem(non-file URI) -> IllegalArgumentException");
            expect(IllegalArgumentException.class,
                  () -> file.getFileSystem(URI.create("file:///tmp")),
                  "getFileSystem(file:///tmp) -> IllegalArgumentException (root path only)");
            expect(IllegalArgumentException.class,
                  () -> file.getFileSystem(URI.create("https://x/")),
                  "getFileSystem(https URI) -> IllegalArgumentException");
            check(file.getFileSystem(URI.create("file:///")) == FileSystems.getDefault(),
                  "getFileSystem(file:///) is the default filesystem");

            // null paths
            expect(NullPointerException.class, () -> file.delete(null),
                  "delete(null) -> NullPointerException");
            expect(NullPointerException.class, () -> file.copy(null, null),
                  "copy(null,null) -> NullPointerException");
            expect(NullPointerException.class,
                  () -> file.readAttributes(null, BasicFileAttributes.class),
                  "readAttributes(null path) -> NullPointerException");
            expect(NullPointerException.class,
                  () -> file.checkAccess(null, AccessMode.READ),
                  "checkAccess(null) -> NullPointerException");
            expect(NullPointerException.class, () -> file.isSameFile(null, null),
                  "isSameFile(null,null) -> NullPointerException");

            // error states
            Path sub = dir.resolve("sub");
            Files.createDirectory(sub);
            expect(FileAlreadyExistsException.class,
                  () -> file.createDirectory(sub),
                  "createDirectory existing -> FileAlreadyExistsException");
            expect(NoSuchFileException.class,
                  () -> file.createDirectory(dir.resolve("no/way")),
                  "createDirectory missing parent -> NoSuchFileException");
            expect(NoSuchFileException.class,
                  () -> file.delete(dir.resolve("ghost")),
                  "delete missing -> NoSuchFileException");
            Path nx = dir.resolve("nx.txt");
            Files.writeString(nx, "x");
            Files.setPosixFilePermissions(nx,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-r--r--"));
            expect(AccessDeniedException.class,
                  () -> file.checkAccess(nx, AccessMode.EXECUTE),
                  "checkAccess EXECUTE on non-executable -> AccessDeniedException");

            // hidden semantics + file store + string attributes + zip refusal
            Path dot = dir.resolve(".hidden");
            Files.writeString(dot, "h");
            check(file.isHidden(dot), "dot file hidden (Unix convention)");
            check(!file.isHidden(dir), "directory itself not hidden");
            check(!file.getFileStore(f).name().isEmpty(), "file store has a name");
            check(!file.getFileStore(f).type().isEmpty(), "file store has a type");
            Map<String, Object> asMap = file.readAttributes(f, "*");
            check(Long.valueOf(8).equals(asMap.get("size"))
                  && Boolean.TRUE.equals(asMap.get("isRegularFile")),
                  "readAttributes(String) map values");
            expect(UnsupportedOperationException.class,
                  () -> file.newFileSystem(f, Map.of()),
                  "default provider newFileSystem(Path,Map) -> UnsupportedOperationException");

            // full-content reads through provider streams
            try (InputStream in = file.newInputStream(f)) {
                byte[] all = in.readAllBytes();
                check(new String(all).equals("provider"), "newInputStream full content");
            }
        } finally {
            try (var st = Files.walk(dir)) {
                java.util.List<Path> l = new java.util.ArrayList<>();
                st.forEach(l::add);
                java.util.Collections.reverse(l);
                for (Path p : l) { try { Files.deleteIfExists(p); } catch (Exception ignore) { } }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testJdk27Surface();
        testNegativeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
