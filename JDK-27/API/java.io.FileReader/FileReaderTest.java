/*
 * @test
 * @summary java.io.FileReader conformance: white-box structure (reflection) + black-box behavior
 *          (UTF-8 character reads and roundtrip with FileWriter).
 * @run main FileReaderTest
 *
 * 黑盒设计案:
 * 1. 契约: 读文件的字符流，默认按系统编码；显式 FileReader(File, Charset) 可固定 UTF-8。
 * 2. 场景: FileWriter(File, UTF-8) 写非 ASCII -> FileReader(File, UTF-8) 读回。
 * 3. 组合: 写 "héllo 世界" -> read() 逐字符/read(char[],off,len) 批量 -> 拼接断言一致。
 * 4. 不变量: 显式 Charset 构造保证确定性编码；读到末尾 read()==-1。
 * 5. 边界: 打开不存在的文件抛 FileNotFoundException。
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReaderTest {
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
        Class<?> c = Class.forName("java.io.FileReader");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.InputStreamReader.class, "extends InputStreamReader");
        c.getConstructor(File.class, Charset.class);
        passed++;
        hasMethod(c, "read");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-fr");
        File f = dir.resolve("text.txt").toFile();
        try {
            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                fw.write("héllo 世界");
            }

            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                char[] buf = new char[100];
                int n = fr.read(buf, 0, buf.length);
                check("héllo 世界".equals(new String(buf, 0, n)), "read(char[],off,len) roundtrip");
            }

            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                check(fr.read() == 'h', "read h");
                check(fr.read() == 'é', "read é");
                check(fr.read() == 'l', "read l");
                check(fr.read() != -1, "more chars");
            }

            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                char[] all = new char[200];
                int n = fr.read(all);
                check(n == "héllo 世界".length(), "read count equals length");
                check(fr.read() == -1, "EOF");
            }

            boolean fnf = false;
            try { new FileReader(dir.resolve("nope.txt").toFile(), StandardCharsets.UTF_8); }
            catch (FileNotFoundException e) { fnf = true; }
            check(fnf, "FileNotFoundException");
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
