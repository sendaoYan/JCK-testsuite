/*
 * @test
 * @summary java.io.StringReader conformance: white-box structure (reflection) + black-box
 *          behavior (read variants, skip, mark/reset, ready, close no-op).
 * @run main StringReaderTest
 *
 * 黑盒设计案:
 * 1. 契约: 以字符串为源的字符输入流，支持单字符/批量读、skip、mark/reset、ready。
 * 2. 场景: StringReader(String) 读 -> read()/read(char[],off,len) -> EOF -1。
 * 3. 组合: skip 跳过 -> mark/reset 重放 -> 未 mark 的 reset 回到起点 -> close 后仍可读（no-op）。
 * 4. 不变量: markSupported()==true；ready() 有数据时为 true。
 * 5. 边界: 读到末尾 read()==-1。
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;

public class StringReaderTest {
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
        Class<?> c = Class.forName("java.io.StringReader");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Reader.class, "extends Reader");
        hasMethod(c, "read");
        hasMethod(c, "skip", long.class);
        hasMethod(c, "mark", int.class);
        hasMethod(c, "reset");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        StringReader r = new StringReader("hello");
        check(r.ready(), "ready");
        check(r.read() == 'h' && r.read() == 'e', "read()");
        char[] buf = new char[3];
        check(r.read(buf) == 3 && buf[0] == 'l' && buf[1] == 'l' && buf[2] == 'o', "read(char[])");
        check(r.read() == -1, "EOF");

        StringReader s = new StringReader("abcdef");
        check(s.markSupported(), "markSupported");
        check(s.skip(2) == 2, "skip");
        check(s.read() == 'c', "read after skip");
        s.mark(0);
        int c = s.read();
        s.reset();
        check(c == s.read(), "mark/reset replay");

        StringReader u = new StringReader("xyz");
        check(u.read() == 'x', "read before unmarked reset");
        u.reset();
        check(u.read() == 'x', "unmarked reset to start");

        StringReader cl = new StringReader("ab");
        cl.close();
        boolean closed = false;
        try { cl.read(); } catch (java.io.IOException e) { closed = true; }
        check(closed, "read after close throws IOException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
