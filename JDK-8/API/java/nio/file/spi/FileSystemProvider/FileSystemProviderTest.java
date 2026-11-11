/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.spi.FileSystemProvider conformance: hierarchy and methods.
 * @run main FileSystemProviderTest
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.spi.FileSystemProvider;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileSystemProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    static void testFileSystemProvider() {
        check(Modifier.isAbstract(FileSystemProvider.class.getModifiers()), "is abstract");

        Set<String> names = new HashSet<String>();
        for (Method m : FileSystemProvider.class.getMethods()) names.add(m.getName());
        String[] required = { "installedProviders", "getScheme", "newFileSystem", "getFileSystem", "getPath", "newInputStream", "newOutputStream", "newFileChannel", "newByteChannel", "newDirectoryStream", "createDirectory", "delete", "copy", "move", "isSameFile", "isHidden", "getFileStore", "checkAccess", "getFileAttributeView", "readAttributes", "setAttribute" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    // ---- appended: abstract-shape detail ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("java.nio.file.spi.FileSystemProvider");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        // JDK-8 declares an extra private (Void) security-gate constructor; the
        // public contract is the protected no-arg constructor for subclasses
        boolean hasProtected = false;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            if (Modifier.isProtected(ct.getModifiers())) hasProtected = true;
        check(hasProtected, "protected constructor for subclasses");
        check(java.util.Arrays.stream(c.getDeclaredConstructors())
                .anyMatch(ct -> ct.getParameterTypes().length == 0
                      && Modifier.isProtected(ct.getModifiers())),
              "protected no-arg constructor");
        for (String n : new String[] {"delete", "copy", "move", "readAttributes"})
            check(java.util.Arrays.stream(c.getDeclaredMethods())
                    .anyMatch(m -> m.getName().equals(n)
                          && Modifier.isAbstract(m.getModifiers())),
                  "abstract method " + n);
        check(Modifier.isStatic(c.getMethod("installedProviders").getModifiers()),
              "installedProviders is static");
        // JDK-27 adds exists(Path,LinkOption...) and readAttributesIfExists(...);
        // both are absent on JDK-8 (version difference recorded in DESIGN.md)
        boolean hasExists = false;
        boolean hasReadIfExists = false;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("exists")) hasExists = true;
            if (m.getName().equals("readAttributesIfExists")) hasReadIfExists = true;
        }
        check(!hasExists, "JDK-8 has no exists(Path,LinkOption...) (JDK-27 does)");
        check(!hasReadIfExists, "JDK-8 has no readAttributesIfExists (JDK-27 does)");
    }

    // ---- appended: installed provider registry ----
    static void testInstalledProviders() {
        java.util.List<FileSystemProvider> providers = FileSystemProvider.installedProviders();
        check(providers != null && !providers.isEmpty(), "providers non-null, non-empty");
        Set<String> schemes = new HashSet<String>();
        FileSystemProvider file = null;
        for (FileSystemProvider p : providers) {
            check(p != null && p.getScheme() != null && !p.getScheme().isEmpty(),
                  "each provider has a scheme");
            schemes.add(p.getScheme());
            if (p.getScheme().equals("file")) file = p;
        }
        check(file != null, "default 'file' provider installed");
        check(schemes.contains("jar"), "jar (zipfs) provider installed (measured)");
        // JDK-27 additionally installs the 'jrt' provider; JDK-8 does not (measured)
        check(!schemes.contains("jrt"), "JDK-8 has no 'jrt' provider (JDK-27 does)");
        check(FileSystems.getDefault().provider() == file,
              "default filesystem wired to the file provider");
    }

    // ---- appended: URI addressing on the file provider ----
    static void testUriHandling(final FileSystemProvider file) throws Exception {
        check("file".equals(file.getScheme()), "scheme is file");
        // reconstructing a concrete file URI (Unix path contract)
        check(file.getPath(URI.create("file:///tmp/x")).toString().equals("/tmp/x"),
              "getPath(file:///tmp/x) -> /tmp/x");
        Path probe = Files.createTempFile("jck-fsp-uri", ".tmp");
        try {
            check(file.getPath(probe.toUri()).equals(probe.toAbsolutePath()),
                  "getPath(toUri()) round-trip");
        } finally {
            Files.deleteIfExists(probe);
        }
        expect(IllegalArgumentException.class,
              () -> file.getPath(URI.create("mailto:x")),
              "getPath(non-file URI) -> IllegalArgumentException");
        expect(IllegalArgumentException.class,
              () -> file.getPath(URI.create("file://host/tmp/x")),
              "getPath(authority URI) -> IllegalArgumentException");
        expect(NullPointerException.class,
              () -> file.getPath(null),
              "getPath(null) -> NullPointerException");
        expect(FileSystemAlreadyExistsException.class,
              () -> file.newFileSystem(URI.create("file:///"),
                      Collections.<String, Object>emptyMap()),
              "newFileSystem(file:///) -> FileSystemAlreadyExistsException");
        expect(IllegalArgumentException.class,
              () -> file.newFileSystem(URI.create("mailto:x"),
                      Collections.<String, Object>emptyMap()),
              "newFileSystem(non-file URI) -> IllegalArgumentException");
        check(file.getFileSystem(URI.create("file:///")) == FileSystems.getDefault(),
              "getFileSystem(file:///) is the default fs");
        check(file.getFileSystem(URI.create("file:///")).isOpen(),
              "default filesystem open");
        expect(IllegalArgumentException.class,
              () -> file.getFileSystem(URI.create("file:///tmp")),
              "getFileSystem(file:///tmp) -> IllegalArgumentException (root path only)");
        expect(IllegalArgumentException.class,
              () -> file.getFileSystem(URI.create("https://x/")),
              "getFileSystem(https URI) -> IllegalArgumentException");
    }

    // ---- appended: whole operation surface on a temp tree ----
    static void testOperationSurface(final FileSystemProvider file) throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("p.txt");
            Files.write(f, "provider".getBytes(StandardCharsets.UTF_8));

            // newByteChannel: content and size through the provider
            try (SeekableByteChannel ch = file.newByteChannel(f,
                    java.util.EnumSet.of(StandardOpenOption.READ))) {
                check(ch.size() == 8, "channel size via provider");
                ByteBuffer buf = ByteBuffer.allocate(8);
                ch.read(buf);
                check(new String(buf.array(), StandardCharsets.UTF_8).equals("provider"),
                      "channel content via provider");
            }

            // newInputStream / newOutputStream conveniences
            try (InputStream in = file.newInputStream(f)) {
                byte[] all = new byte[8];
                int off = 0;
                int n;
                while ((n = in.read(all, off, all.length - off)) > 0) off += n;
                check(new String(all, StandardCharsets.UTF_8).equals("provider"),
                      "newInputStream reads the whole file");
            }
            try (OutputStream out = file.newOutputStream(dir.resolve("os.txt"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                out.write('o');
            }
            check(new String(Files.readAllBytes(dir.resolve("os.txt")),
                    StandardCharsets.UTF_8).equals("o"), "newOutputStream writes");

            // newDirectoryStream with a filter
            try (DirectoryStream<Path> ds = file.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) {
                            return p.getFileName().toString().endsWith(".txt");
                        } })) {
                java.util.List<Path> list = new java.util.ArrayList<Path>();
                for (Path p : ds) list.add(p);
                check(list.size() == 2, "filtered stream finds both .txt files");
            }

            // createDirectory success + failure modes
            Path sub = dir.resolve("sub");
            file.createDirectory(sub);
            check(Files.isDirectory(sub), "createDirectory via provider");
            expect(FileAlreadyExistsException.class,
                  () -> file.createDirectory(sub),
                  "createDirectory existing -> FileAlreadyExistsException");
            expect(NoSuchFileException.class,
                  () -> file.createDirectory(dir.resolve("no/way")),
                  "createDirectory missing parent -> NoSuchFileException");

            // delete success + failure + null
            Path doomed = sub.resolve("d.txt");
            Files.write(doomed, "d".getBytes(StandardCharsets.UTF_8));
            file.delete(doomed);
            check(!Files.exists(doomed), "delete via provider");
            expect(NoSuchFileException.class,
                  () -> file.delete(dir.resolve("ghost")),
                  "delete missing -> NoSuchFileException");
            expect(NullPointerException.class,
                  () -> file.delete(null),
                  "delete(null) -> NullPointerException");

            // copy / move / isSameFile
            Path copy = dir.resolve("c.txt");
            file.copy(f, copy);
            check(new String(Files.readAllBytes(copy), StandardCharsets.UTF_8)
                    .equals("provider"), "copy via provider");
            check(file.isSameFile(f, f), "isSameFile identity");
            check(!file.isSameFile(f, copy), "distinct files differ");
            Path moved = dir.resolve("m.txt");
            file.move(copy, moved);
            check(Files.exists(moved) && !Files.exists(copy), "move via provider");
            expect(NullPointerException.class,
                  () -> file.copy(null, null),
                  "copy(null,null) -> NullPointerException");
            expect(NullPointerException.class,
                  () -> file.isSameFile(null, null),
                  "isSameFile(null,null) -> NullPointerException");

            // attributes: class-based, string-based, view, setAttribute
            BasicFileAttributes attrs = file.readAttributes(f, BasicFileAttributes.class);
            check(attrs.isRegularFile() && attrs.size() == 8, "readAttributes via provider");
            Map<String, Object> asMap = file.readAttributes(f, "*");
            check(Long.valueOf(8).equals(asMap.get("size"))
                  && Boolean.TRUE.equals(asMap.get("isRegularFile")),
                  "readAttributes(String) map values");
            check(asMap.containsKey("lastModifiedTime"),
                  "readAttributes(String) includes timestamps");
            BasicFileAttributeView view = file.getFileAttributeView(f,
                    BasicFileAttributeView.class);
            check(view != null && view.readAttributes().size() == 8, "attribute view read");
            FileTime stamp = FileTime.from(java.time.Instant.parse("2020-05-05T05:05:05Z"));
            file.setAttribute(f, "basic:lastModifiedTime", stamp);
            check(Files.getLastModifiedTime(f).equals(stamp), "setAttribute via provider");
            expect(NullPointerException.class,
                  () -> file.readAttributes(null, BasicFileAttributes.class),
                  "readAttributes(null path) -> NullPointerException");

            // checkAccess matrix
            file.checkAccess(f, AccessMode.READ, AccessMode.WRITE);
            check(true, "checkAccess READ+WRITE passes");
            Path nx = dir.resolve("nx.txt");
            Files.write(nx, "x".getBytes(StandardCharsets.UTF_8));
            Files.setPosixFilePermissions(nx, PosixFilePermissions.fromString("rw-r--r--"));
            expect(AccessDeniedException.class,
                  () -> file.checkAccess(nx, AccessMode.EXECUTE),
                  "checkAccess EXECUTE on non-executable -> AccessDeniedException");
            expect(NoSuchFileException.class,
                  () -> file.checkAccess(dir.resolve("ghost"), AccessMode.READ),
                  "checkAccess missing -> NoSuchFileException");
            expect(NullPointerException.class,
                  () -> file.checkAccess(null, AccessMode.READ),
                  "checkAccess(null) -> NullPointerException");

            // isHidden
            check(!file.isHidden(f), "plain file not hidden");
            Path dot = dir.resolve(".hidden");
            Files.write(dot, "h".getBytes(StandardCharsets.UTF_8));
            check(file.isHidden(dot), "dot file hidden (Unix convention)");
            check(!file.isHidden(dir), "directory itself not hidden");

            // getFileStore
            check(file.getFileStore(f) != null
                  && !file.getFileStore(f).name().isEmpty(), "file store has a name");
            check(!file.getFileStore(f).type().isEmpty(), "file store has a type");

            // the default provider refuses zip-style newFileSystem(Path, Map)
            expect(UnsupportedOperationException.class,
                  () -> file.newFileSystem(f, Collections.<String, Object>emptyMap()),
                  "default provider newFileSystem(Path,Map) -> UnsupportedOperationException");
        } finally {
            try (java.util.stream.Stream<Path> st = Files.walk(dir)) {
                java.util.List<Path> l = new java.util.ArrayList<Path>();
                st.forEach(l::add);
                java.util.Collections.reverse(l);
                for (Path p : l) { try { Files.deleteIfExists(p); } catch (Exception ignore) { } }
            }
        }
    }

    // ---- appended: foreign-provider paths are rejected ----
    static void testProviderMismatch(final FileSystemProvider file) throws Exception {
        Path zip = Files.createTempFile("jck-fsp", ".zip");
        try {
            try (java.util.zip.ZipOutputStream zos =
                    new java.util.zip.ZipOutputStream(Files.newOutputStream(zip))) {
                zos.putNextEntry(new java.util.zip.ZipEntry("z.txt"));
            }
            try (FileSystem zfs = FileSystems.newFileSystem(zip, (ClassLoader) null)) {
                final Path zpath = zfs.getPath("/z.txt");
                expect(ProviderMismatchException.class,
                      () -> file.readAttributes(zpath, BasicFileAttributes.class),
                      "foreign path to file provider -> ProviderMismatchException");
                expect(ProviderMismatchException.class,
                      () -> file.delete(zpath),
                      "foreign path delete -> ProviderMismatchException");
            }
        } finally {
            Files.deleteIfExists(zip);
        }
    }

    public static void main(String[] args) throws Exception {
        testFileSystemProvider();
        testShape();
        testInstalledProviders();
        FileSystemProvider file = null;
        for (FileSystemProvider p : FileSystemProvider.installedProviders())
            if (p.getScheme().equals("file")) file = p;
        check(file != null, "file provider located for behavioral tests");
        testUriHandling(file);
        testOperationSurface(file);
        testProviderMismatch(file);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
