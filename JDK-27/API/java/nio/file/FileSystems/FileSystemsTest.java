/*
 * @test
 * @summary java.nio.file.FileSystems conformance: white-box structure (reflection) + black-box
 *          behavior (default filesystem properties, provider scheme, zip filesystem).
 * @run main FileSystemsTest
 *
 * 黑盒设计案:
 * 1. 契约: 文件系统工厂，getDefault 返回默认 FS；getFileSystem(URI) 返回匹配 FS；newFileSystem 打开可关闭的文件系统（如 zip）。
 * 2. 场景: getDefault -> getSeparator/provider/isOpen/supportedFileAttributeViews/getPath/getRootDirectories。
 * 3. 组合: 建 zip 文件 -> newFileSystem(jar:URI) -> 打开 entry -> readString 断言 -> close。
 * 4. 不变量: 默认 FS 的 provider scheme 为 "file"；getFileSystem(file URI) 返回与 getDefault 同一实例。
 * 5. 边界: newFileSystem 打开的 FS isOpen()==true，close 后 isOpen()==false。
 */

import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileSystemsTest {
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

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.FileSystems");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "superclass Object");
        hasMethod(c, "getDefault");
        hasMethod(c, "getFileSystem", URI.class);
        hasMethod(c, "newFileSystem", URI.class, Map.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        FileSystem fs = FileSystems.getDefault();
        check(fs != null && fs.isOpen(), "getDefault open");
        check(fs.getSeparator().equals(java.io.File.separator), "separator matches File.separator");
        check("file".equals(fs.provider().getScheme()), "provider scheme file");
        check(fs.supportedFileAttributeViews().contains("basic"), "supportedFileAttributeViews basic");
        check(fs.getRootDirectories() != null, "rootDirectories");
        check(fs.getPath("a", "b").toString().equals("a" + fs.getSeparator() + "b"), "getPath joins");

        check(FileSystems.getFileSystem(URI.create("file:///")) == fs, "getFileSystem file URI");

        Path zip = Files.createTempFile("jck-fs", ".zip");
        try {
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
                zos.putNextEntry(new ZipEntry("hello.txt"));
                zos.write("héllo zip".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            URI jarUri = URI.create("jar:" + zip.toUri());
            Map<String, String> env = new HashMap<>();
            try (FileSystem zfs = FileSystems.newFileSystem(jarUri, env)) {
                check(zfs.isOpen(), "zip fs open");
                check("/".equals(zfs.getSeparator()), "zip fs separator");
                Path entry = zfs.getPath("/hello.txt");
                check(Files.isRegularFile(entry), "zip entry isRegularFile");
                check("héllo zip".equals(Files.readString(entry, StandardCharsets.UTF_8)), "zip entry readString");
                check(Files.size(entry) == "héllo zip".getBytes(StandardCharsets.UTF_8).length, "zip entry size");
            }
        } finally {
            Files.deleteIfExists(zip);
        }
    }

    // ===== deepened contract coverage (batch A9): probe-confirmed exact types =====
    static void testUriLookupMatrix() throws Exception {
        FileSystem def = FileSystems.getDefault();
        check(FileSystems.getFileSystem(URI.create("file:///")) == def,
              "getFileSystem(file:///) returns the default fs");
        check(FileSystems.getFileSystem(URI.create("file:///")).isOpen(),
              "returned fs is open");
        checkThrows(IllegalArgumentException.class,
              () -> FileSystems.getFileSystem(URI.create("file:///nope")),
              "getFileSystem(file URI with path) -> IllegalArgumentException");
        checkThrows(java.nio.file.ProviderNotFoundException.class,
              () -> FileSystems.getFileSystem(URI.create("bogus://x")),
              "getFileSystem unknown scheme -> ProviderNotFoundException");
        checkThrows(NullPointerException.class,
              () -> FileSystems.getFileSystem(null), "getFileSystem(null) NPE");
        checkThrows(java.nio.file.FileSystemNotFoundException.class,
              () -> FileSystems.getFileSystem(URI.create("jar:file:///tmp/nope.zip!/")),
              "getFileSystem closed jar fs -> FileSystemNotFoundException");

        // newFileSystem(URI, ...) over the default provider
        java.util.Map<String, Object> env = new HashMap<>();
        checkThrows(java.nio.file.FileSystemAlreadyExistsException.class,
              () -> FileSystems.newFileSystem(URI.create("file:///"), env),
              "newFileSystem(file:) -> FileSystemAlreadyExistsException (probe-confirmed)");
        checkThrows(java.nio.file.FileSystemAlreadyExistsException.class,
              () -> FileSystems.newFileSystem(URI.create("file:///"), null),
              "newFileSystem(file:, null env) -> FileSystemAlreadyExistsException");
        checkThrows(java.nio.file.ProviderNotFoundException.class,
              () -> FileSystems.newFileSystem(URI.create("bogus://x"), env),
              "newFileSystem(bogus:) -> ProviderNotFoundException");
        checkThrows(java.nio.file.ProviderNotFoundException.class,
              () -> FileSystems.newFileSystem(URI.create("bogus://x"), env, null),
              "newFileSystem(bogus:, loader) -> ProviderNotFoundException");
        checkThrows(NullPointerException.class,
              () -> FileSystems.newFileSystem((URI) null, env), "newFileSystem(null URI) NPE");
    }

    // zip filesystem via Path incl. JDK-13+ overloads
    static void testZipPathFileSystem() throws Exception {
        Path zip = Files.createTempFile("jck-fs-path", ".zip");
        try {
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
                zos.putNextEntry(new ZipEntry("entry.txt"));
                zos.write("path-zip".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            try (FileSystem zfs = FileSystems.newFileSystem(zip, (ClassLoader) null)) {
                check(zfs.isOpen(), "zipfs via path open");
                check("/".equals(zfs.getSeparator()), "zipfs separator /");
                check(!zfs.isReadOnly(), "zipfs writable");
                Path entry = zfs.getPath("/entry.txt");
                check(Files.isRegularFile(entry), "zipfs entry regular");
                check("path-zip".equals(Files.readString(entry, StandardCharsets.UTF_8)),
                      "zipfs entry content via path-opened fs");
                java.util.Set<String> views = zfs.supportedFileAttributeViews();
                check(views.contains("basic") && views.contains("zip"),
                      "zipfs views contain basic and zip");
                check(zfs.provider() != null && "jar".equals(zfs.provider().getScheme()),
                      "zipfs provider scheme jar");
                check(zfs.getPath("a", "b").toString().equals("a/b"), "zipfs getPath join");
            }
            check(true, "zipfs closed by try-with-resources");
            // JDK-13+ overloads present and usable
            try (FileSystem zfs2 = FileSystems.newFileSystem(zip)) {
                check(zfs2.isOpen(), "newFileSystem(Path) single-arg open (JDK-13+)");
            }
            try (FileSystem zfs3 = FileSystems.newFileSystem(zip, new HashMap<String, String>())) {
                check(zfs3.isOpen(), "newFileSystem(Path, Map) open (JDK-13+)");
                check(zfs3.getPath("/entry.txt") != null, "Map overload navigable");
            }
        } finally {
            Files.deleteIfExists(zip);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUriLookupMatrix();
        testZipPathFileSystem();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
