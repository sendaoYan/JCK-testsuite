/*
 * @test
 * @summary java.io.PrintWriter conformance: white-box structure (reflection) + black-box behavior
 *          (print/println/printf/format, checkError, UTF-8 output).
 * @run main PrintWriterTest
 *
 * 黑盒设计案:
 * 1. 契约: 格式化字符输出流，print/println/printf 各类型，从不抛 IOException（错误记入 checkError）。
 * 2. 场景: PrintWriter(StringWriter)/PrintWriter(OutputStream,boolean,Charset) -> print/println/printf/format。
 * 3. 组合: 固定 Locale.US printf 定点小数 -> toString 断言；UTF-8 输出到流 -> InputStreamReader 读回。
 * 4. 不变量: printf 返回 this 支持链式；checkError 正常写入为 false。
 * 5. 边界: println 追加平台行分隔符。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PrintWriterTest {
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
        Class<?> c = Class.forName("java.io.PrintWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Writer.class, "extends Writer");
        hasMethod(c, "printf", String.class, Object[].class);
        hasMethod(c, "format", String.class, Object[].class);
        hasMethod(c, "println", String.class);
        hasMethod(c, "checkError");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        check(pw.printf(Locale.US, "%d %.2f %s", 42, 3.14159, "x") == pw, "printf returns this");
        pw.println();
        pw.print(true);
        pw.print(' ');
        pw.print(7L);
        pw.flush();
        String out = sw.toString();
        check(out.startsWith("42 3.14 x"), "printf content");
        check(out.contains("true"), "print boolean");
        check(out.contains("7"), "print long");
        check(!pw.checkError(), "checkError false");

        StringWriter sw2 = new StringWriter();
        PrintWriter pw2 = new PrintWriter(sw2);
        pw2.format(Locale.US, "%05d", 42);
        pw2.flush();
        check("00042".equals(sw2.toString()), "format");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw3 = new PrintWriter(bos, false, StandardCharsets.UTF_8);
        pw3.println("héllo 世界");
        pw3.flush();
        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), StandardCharsets.UTF_8);
        char[] buf = new char[100];
        int n = isr.read(buf);
        check(("héllo 世界" + System.lineSeparator()).equals(new String(buf, 0, n)),
              "UTF-8 println roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
