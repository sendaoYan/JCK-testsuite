/*
 * @test
 * @summary java.io.StringWriter conformance: white-box structure (reflection) + black-box
 *          behavior (write/append variants, toString, getBuffer, roundtrip with StringReader).
 * @run main StringWriterTest
 *
 * 黑盒设计案:
 * 1. 契约: 以 StringBuffer 为目标的字符输出流，支持 write/append，toString 返回累积内容。
 * 2. 场景: StringWriter() 写 -> write(String)/write(char)/append -> toString/getBuffer。
 * 3. 组合: 写多行 -> toString -> StringReader 读回逐段比对；write(String,off,len) 子串。
 * 4. 不变量: getBuffer() 与 toString() 内容一致；flush/close 为 no-op。
 * 5. 边界: 空流 toString()==""。
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;

public class StringWriterTest {
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
        Class<?> c = Class.forName("java.io.StringWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Writer.class, "extends Writer");
        hasMethod(c, "write", String.class);
        hasMethod(c, "append", CharSequence.class);
        hasMethod(c, "toString");
        hasMethod(c, "getBuffer");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        StringWriter w = new StringWriter();
        check("".equals(w.toString()), "initial empty");
        w.write("abc");
        w.write('d');
        w.write("efgh", 1, 3); // "fgh"
        check("abcdfgh".equals(w.toString()), "write/toString");
        check(w.toString().equals(w.getBuffer().toString()), "getBuffer matches toString");

        StringWriter w2 = new StringWriter();
        w2.append("xy").append('z');
        check("xyz".equals(w2.toString()), "append");
        w2.flush();
        w2.close();
        check("xyz".equals(w2.toString()), "flush/close no-op");

        StringWriter w3 = new StringWriter();
        w3.write("line1\nline2\nline3");
        StringReader r = new StringReader(w3.toString());
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[5];
        int n;
        while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        check(w3.toString().equals(sb.toString()), "roundtrip with StringReader");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
