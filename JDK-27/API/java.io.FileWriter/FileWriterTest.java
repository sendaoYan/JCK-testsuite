/*
 * @test
 * @summary java.io.FileWriter conformance: white-box structure (reflection) + black-box behavior
 *          (write/append modes with explicit UTF-8 and roundtrip with FileReader).
 * @run main FileWriterTest
 *
 * 黑盒设计案:
 * 1. 契约: 写文件的字符流，默认系统编码；FileWriter(File, Charset[, append]) 固定编码与追加模式。
 * 2. 场景: FileWriter(File, UTF-8) 写 -> 读回；FileWriter(File, UTF-8, true) 追加。
 * 3. 组合: 写 "ab" -> 追加 "cd" -> FileReader(UTF-8) 读回 "abcd"；默认构造覆盖。
 * 4. 不变量: append=true 保留原内容；默认 append=false 覆盖。
 * 5. 边界: write(char[],off,len) 只写指定范围。
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWriterTest {
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
        Class<?> c = Class.forName("java.io.FileWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.OutputStreamWriter.class, "extends OutputStreamWriter");
        c.getConstructor(File.class, Charset.class, boolean.class);
        passed++;
        hasMethod(c, "write", String.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-fw");
        File f = dir.resolve("w.txt").toFile();
        try {
            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                fw.write("ab");
            }
            check(f.length() == 2, "wrote 2 chars");

            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8, true)) {
                fw.append("cd");
            }
            check(f.length() == 4, "append adds 2 chars");

            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                char[] buf = new char[10];
                int n = fr.read(buf);
                check("abcd".equals(new String(buf, 0, n)), "append roundtrip");
            }

            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                fw.write("xy");
            }
            check(f.length() == 2, "default mode truncates");

            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                fw.write("é中");
            }
            check(f.length() == "é中".getBytes(StandardCharsets.UTF_8).length, "UTF-8 byte length");
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
