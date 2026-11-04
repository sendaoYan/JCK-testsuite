/*
 * @test
 * @summary java.io.FileOutputStream conformance: white-box structure (reflection) + black-box
 *          behavior (write/append modes, FD/channel, truncate-vs-append).
 * @run main FileOutputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 向文件写字节的输出流；默认截断，append=true 时追加；暴露 getFD/getChannel。
 * 2. 场景: FileOutputStream(File) 写 -> 读回；FileOutputStream(File,true) 追加。
 * 3. 组合: 写 "ab" -> 追加 "cd" -> FileInputStream 读回 "abcd" -> 长度断言。
 * 4. 不变量: 默认构造覆盖已有内容；append 模式保留并追加。
 * 5. 边界: write(byte[],off,len) 只写指定范围。
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileOutputStreamTest {
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
        Class<?> c = Class.forName("java.io.FileOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.OutputStream.class, "extends OutputStream");
        hasMethod(c, "write", int.class);
        hasMethod(c, "getFD");
        hasMethod(c, "getChannel");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-fos");
        File f = dir.resolve("out.bin").toFile();
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(new byte[]{1, 2, 3});
                check(fos.getFD() != null, "getFD");
                check(fos.getChannel() != null, "getChannel");
            }
            check(f.length() == 3, "wrote 3 bytes");

            try (FileOutputStream fos = new FileOutputStream(f, true)) {
                fos.write(new byte[]{4, 5}, 0, 2);
            }
            check(f.length() == 5, "append adds bytes");
            try (FileInputStream fis = new FileInputStream(f)) {
                check(Arrays.equals(fis.readAllBytes(), new byte[]{1, 2, 3, 4, 5}), "append roundtrip");
            }

            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(9);
            }
            check(f.length() == 1, "default mode truncates");

            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(new byte[]{10, 20, 30, 40}, 1, 2);
            }
            check(f.length() == 2, "write(byte[],off,len) length");
            try (FileInputStream fis = new FileInputStream(f)) {
                check(Arrays.equals(fis.readAllBytes(), new byte[]{20, 30}), "write(byte[],off,len) content");
            }
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
