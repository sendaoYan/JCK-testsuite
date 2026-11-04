/*
 * @test
 * @summary java.io.InputStreamReader conformance: white-box structure (reflection) + black-box
 *          behavior (UTF-8 decoding of multi-byte/supplementary chars, partial reads).
 * @run main InputStreamReaderTest
 *
 * 黑盒设计案:
 * 1. 契约: 字节流 -> 字符流的桥，按指定 Charset 解码字节为字符，报告 getEncoding()。
 * 2. 场景: OutputStreamWriter(UTF-8) 写 -> 字节 -> 本类(UTF-8) 读回。
 * 3. 组合: 写中文/重音/emoji 混合字符串 -> read(char[],off,len) 分批读 -> 拼接断言原文一致。
 * 4. 不变量: getEncoding() 返回所用编码的历史名(UTF8)；read() 与 read(char[],off,len) 均正确解码。
 * 5. 边界: 读到末尾 read()==-1。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class InputStreamReaderTest {
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
        Class<?> c = Class.forName("java.io.InputStreamReader");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Reader.class, "extends Reader");
        hasMethod(c, "getEncoding");
        hasMethod(c, "read");
        hasMethod(c, "read", char[].class, int.class, int.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        String text = "héllo 世界 😀";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        osw.write(text);
        osw.flush();

        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), StandardCharsets.UTF_8);
        check(isr.getEncoding() != null, "getEncoding non-null");

        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4];
        int n;
        while ((n = isr.read(buf, 0, buf.length)) != -1) {
            sb.append(buf, 0, n);
        }
        check(text.equals(sb.toString()), "UTF-8 multi-byte roundtrip");
        check(isr.read() == -1, "EOF");

        InputStreamReader single =
            new InputStreamReader(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)),
                                  StandardCharsets.UTF_8);
        check(single.read() == 'a' && single.read() == 'b' && single.read() == 'c' && single.read() == -1,
              "single char reads");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
