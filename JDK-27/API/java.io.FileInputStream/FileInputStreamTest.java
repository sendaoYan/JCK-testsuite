/*
 * @test
 * @summary java.io.FileInputStream conformance: white-box structure (reflection) + black-box
 *          behavior (reads, skip, available, readAllBytes/readNBytes/transferTo, channel/FD).
 * @run main FileInputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 从文件读取字节的输入流，支持单字节/批量读、skip、available，并暴露 getFD/getChannel。
 * 2. 场景: FileOutputStream 写文件 -> FileInputStream(File)/try-with-resources 读回。
 * 3. 组合: 写已知字节序列 -> read()/read(byte[],off,len)/skip/available 逐步读 -> readAllBytes 全量比对 -> transferTo 拷贝。
 * 4. 不变量: available 初值为剩余字节数；读到末尾 read()==-1；readAllBytes 返回完整内容。
 * 5. 边界: 打开不存在的文件抛 FileNotFoundException。
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileInputStreamTest {
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
        Class<?> c = Class.forName("java.io.FileInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.InputStream.class, "extends InputStream");
        hasMethod(c, "read");
        hasMethod(c, "readAllBytes");
        hasMethod(c, "readNBytes", int.class);
        hasMethod(c, "transferTo", java.io.OutputStream.class);
        hasMethod(c, "getChannel");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-fis");
        File f = dir.resolve("data.bin").toFile();
        byte[] data = {10, 20, 30, 40, 50, 60, 70};
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(data);
            }

            try (FileInputStream fis = new FileInputStream(f)) {
                check(fis.getFD() != null, "getFD");
                check(fis.getChannel() != null, "getChannel");
                check(fis.available() == data.length, "available");
                check(fis.read() == 10, "read 10");
                byte[] buf = new byte[3];
                check(fis.read(buf, 1, 2) == 2 && buf[1] == 20 && buf[2] == 30, "read(byte[],off,len)");
                check(fis.skip(1) == 1, "skip");
                check(fis.read() == 50, "read 50 after skip");
                byte[] rest = fis.readNBytes(2);
                check(Arrays.equals(rest, new byte[]{60, 70}), "readNBytes");
                check(fis.read() == -1, "EOF");
            }

            try (FileInputStream fis = new FileInputStream(f)) {
                check(Arrays.equals(fis.readAllBytes(), data), "readAllBytes");
            }

            try (FileInputStream fis = new FileInputStream(f);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                check(fis.transferTo(bos) == data.length, "transferTo count");
                check(Arrays.equals(bos.toByteArray(), data), "transferTo content");
            }

            boolean fnf = false;
            try { new FileInputStream(dir.resolve("missing.bin").toFile()); }
            catch (FileNotFoundException e) { fnf = true; }
            check(fnf, "FileNotFoundException for missing file");
        } finally {
            Files.deleteIfExists(f.toPath());
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
