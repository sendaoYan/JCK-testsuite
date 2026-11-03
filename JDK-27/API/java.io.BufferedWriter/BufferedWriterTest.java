/*
 * @test
 * @summary java.io.BufferedWriter conformance: white-box structure (reflection) + black-box
 *          behavior (write variants, newLine, flush/close, buffered roundtrip).
 * @run main BufferedWriterTest
 *
 * 黑盒设计案:
 * 1. 契约: 字符缓冲输出流，支持写字符/字符串/子串、newLine 写平台行分隔符，flush/close 落盘。
 * 2. 场景: BufferedWriter(StringWriter) 写 -> newLine -> flush -> toString 断言。
 * 3. 组合: 小缓冲写多行 -> close -> BufferedReader 逐行读回 -> 逐行比对。
 * 4. 不变量: newLine() 写入 System.lineSeparator()；close() 隐含 flush。
 * 5. 边界: write(String,off,len)/write(char[],off,len) 只写指定范围。
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;

public class BufferedWriterTest {
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
        Class<?> c = Class.forName("java.io.BufferedWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Writer.class, "extends Writer");
        hasMethod(c, "newLine");
        hasMethod(c, "write", String.class, int.class, int.class);
        hasMethod(c, "flush");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        bw.write("hello");
        bw.newLine();
        bw.write("world");
        bw.flush();
        String expected = "hello" + System.lineSeparator() + "world";
        check(expected.equals(sw.toString()), "write/newLine/flush");

        StringWriter sw2 = new StringWriter();
        BufferedWriter bw2 = new BufferedWriter(sw2);
        bw2.write("abcdef", 2, 3);                     // "cde"
        bw2.write(new char[]{'x', 'y', 'z'}, 0, 2);     // "xy"
        bw2.flush();
        check("cdexy".equals(sw2.toString()), "write(String,off,len)+char[]");

        StringWriter sw3 = new StringWriter();
        BufferedWriter bw3 = new BufferedWriter(sw3, 8);
        bw3.write("line1");
        bw3.newLine();
        bw3.write("line2");
        bw3.newLine();
        bw3.write("line3");
        bw3.close();
        BufferedReader br = new BufferedReader(new StringReader(sw3.toString()));
        check("line1".equals(br.readLine()), "roundtrip line1");
        check("line2".equals(br.readLine()), "roundtrip line2");
        check("line3".equals(br.readLine()), "roundtrip line3");
        check(br.readLine() == null, "roundtrip EOF");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
