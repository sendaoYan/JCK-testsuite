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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
