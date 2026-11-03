/*
 * @test
 * @summary java.io.OutputStreamWriter conformance: white-box structure (reflection) + black-box
 *          behavior (UTF-8 encoding of ASCII/multi-byte chars, getEncoding, roundtrip).
 * @run main OutputStreamWriterTest
 *
 * 黑盒设计案:
 * 1. 契约: 字符流 -> 字节流的桥，按指定 Charset 编码字符为字节，报告 getEncoding()。
 * 2. 场景: OutputStreamWriter(OutputStream, UTF-8) 写字符 -> flush -> 底层字节断言。
 * 3. 组合: 写 "Aé中" -> 断言 UTF-8 字节序列 -> InputStreamReader 读回原文一致。
 * 4. 不变量: getEncoding() 返回所用编码历史名(UTF8)；write(char[],off,len)/append 编码一致。
 * 5. 边界: flush 后才可见到全部字节（缓冲）。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OutputStreamWriterTest {
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
        Class<?> c = Class.forName("java.io.OutputStreamWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Writer.class, "extends Writer");
        hasMethod(c, "getEncoding");
        hasMethod(c, "write", int.class);
        hasMethod(c, "flush");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        check(osw.getEncoding() != null, "getEncoding non-null");
        osw.write("Aé中");
        osw.flush();
        byte[] expected = "Aé中".getBytes(StandardCharsets.UTF_8);
        check(Arrays.equals(bos.toByteArray(), expected), "UTF-8 encoding exact bytes");

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        OutputStreamWriter o2 = new OutputStreamWriter(b2, StandardCharsets.UTF_8);
        o2.write(new char[]{'x', 'y', 'z', 'w'}, 1, 2); // "yz"
        o2.append('!');
        o2.flush();
        check("yz!".equals(new String(b2.toByteArray(), StandardCharsets.UTF_8)),
              "write(char[],off,len)+append");

        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        OutputStreamWriter o3 = new OutputStreamWriter(b3, StandardCharsets.UTF_8);
        o3.write("héllo 世界");
        o3.flush();
        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(b3.toByteArray()), StandardCharsets.UTF_8);
        char[] c = new char[100];
        int n = isr.read(c);
        check("héllo 世界".equals(new String(c, 0, n)), "roundtrip via InputStreamReader");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
