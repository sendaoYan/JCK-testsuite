/*
 * @test
 * @summary java.io.PrintStream conformance: white-box structure (reflection) + black-box behavior
 *          (print/println/printf/format, charset(), checkError, append).
 * @run main PrintStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 格式化字节输出流，print/println/printf 各类型，按 Charset 编码输出，错误记入 checkError。
 * 2. 场景: PrintStream(OutputStream,boolean,Charset) -> print/println/printf/format。
 * 3. 组合: 固定 Locale.US printf -> 字节 -> UTF-8 解码断言；charset() 返回构造时 Charset。
 * 4. 不变量: printf 返回 this；checkError 正常为 false；println 追加平台行分隔符。
 * 5. 边界: append 追加字符序列。
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PrintStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PrintStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterOutputStream.class, "extends FilterOutputStream");
        check(hasInterface(c, "java.lang.Appendable"), "implements Appendable");
        check(hasInterface(c, "java.io.Closeable"), "implements Closeable");
        hasMethod(c, "printf", String.class, Object[].class);
        hasMethod(c, "format", String.class, Object[].class);
        hasMethod(c, "charset");
        hasMethod(c, "checkError");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos, false, StandardCharsets.UTF_8);
        check(StandardCharsets.UTF_8.equals(ps.charset()), "charset()");
        check(ps.printf(Locale.US, "%d %.2f", 42, 3.14159) == ps, "printf returns this");
        ps.println();
        ps.print(true);
        ps.append(" ok");
        ps.flush();
        String out = new String(bos.toByteArray(), StandardCharsets.UTF_8);
        check(out.startsWith("42 3.14"), "printf content");
        check(out.contains("true"), "print boolean");
        check(out.contains("ok"), "append");
        check(!ps.checkError(), "checkError false");

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        PrintStream p2 = new PrintStream(b2, false, StandardCharsets.UTF_8);
        p2.format(Locale.US, "%05d", 42);
        p2.flush();
        check("00042".equals(new String(b2.toByteArray(), StandardCharsets.UTF_8)), "format");

        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        PrintStream p3 = new PrintStream(b3, false, StandardCharsets.UTF_8);
        p3.println("héllo 世界");
        p3.flush();
        check(("héllo 世界" + System.lineSeparator()).equals(new String(b3.toByteArray(), StandardCharsets.UTF_8)),
              "UTF-8 println roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
