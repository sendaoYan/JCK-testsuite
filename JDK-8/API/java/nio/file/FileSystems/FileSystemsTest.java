/*
 * @test
 * @summary java.nio.file.FileSystems conformance: white-box structure (reflection) + black-box
 *          behavior (default filesystem properties, provider scheme, zip filesystem; JDK-8).
 * @run main FileSystemsTest
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
                check("héllo zip".equals(new String(Files.readAllBytes(entry), StandardCharsets.UTF_8)),
                      "zip entry content");
                check(Files.size(entry) == "héllo zip".getBytes(StandardCharsets.UTF_8).length,
                      "zip entry size");
            }
        } finally {
            Files.deleteIfExists(zip);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
