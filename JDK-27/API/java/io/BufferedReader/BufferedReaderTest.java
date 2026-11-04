/*
 * @test
 * @summary java.io.BufferedReader conformance: white-box structure (reflection) + black-box
 *          behavior (readLine variants, single-char/block reads, skip, mark/reset, lines()).
 * @run main BufferedReaderTest
 *
 * 黑盒设计案:
 * 1. 契约: 字符缓冲输入流，支持按行读取 readLine、单字符/批量读、skip 与 mark/reset，并提供 lines() 流。
 * 2. 场景: BufferedReader(StringReader) 读多行 -> readLine 逐行 -> EOF 返回 null。
 * 3. 组合: readLine 处理空行与无尾换行；lines() 收集行；read(char[],off,len) 与 skip 混合。
 * 4. 不变量: markSupported()==true；readLine 在末尾返回 null；readLine 去掉行终止符但保留内容。
 * 5. 边界: 空输入首行 readLine()==null。
 */

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

public class BufferedReaderTest {
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
        Class<?> c = Class.forName("java.io.BufferedReader");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Reader.class, "extends Reader");
        hasMethod(c, "readLine");
        hasMethod(c, "lines");
        hasMethod(c, "mark", int.class);
        hasMethod(c, "reset");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        BufferedReader br = new BufferedReader(new StringReader("a\n\nc"));
        check("a".equals(br.readLine()), "readLine 1");
        check("".equals(br.readLine()), "readLine empty line");
        check("c".equals(br.readLine()), "readLine 3");
        check(br.readLine() == null, "readLine EOF");

        BufferedReader last = new BufferedReader(new StringReader("x\ny"));
        check("x".equals(last.readLine()) && "y".equals(last.readLine()) && last.readLine() == null,
              "readLine without trailing newline");

        BufferedReader mix = new BufferedReader(new StringReader("abcdefgh"));
        check(mix.markSupported(), "markSupported");
        char[] buf = new char[4];
        check(mix.read(buf, 1, 2) == 2 && buf[1] == 'a' && buf[2] == 'b', "read(char[],off,len)");
        check(mix.skip(2) == 2, "skip");
        check(mix.read() == 'e', "single read after skip");
        mix.mark(10);
        int c = mix.read();
        mix.reset();
        check(c == mix.read(), "mark/reset replay");

        List<String> lines = new BufferedReader(new StringReader("l1\nl2\nl3"))
                .lines().collect(Collectors.toList());
        check(lines.size() == 3 && lines.get(0).equals("l1") && lines.get(2).equals("l3"), "lines()");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
