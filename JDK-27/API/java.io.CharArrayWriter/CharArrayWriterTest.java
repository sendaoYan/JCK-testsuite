/*
 * @test
 * @summary java.io.CharArrayWriter conformance: white-box structure + black-box
 *          behavior (growth beyond initial capacity, all write/append forms,
 *          writeTo(Writer) delegation, reset, toCharArray/size/toString snapshots,
 *          close/flush no-ops).
 * @run main CharArrayWriterTest
 */

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CharArrayWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.CharArrayWriter");
        check(c.getSuperclass().getName().equals("java.io.Writer"), "extends Writer");
        check(c.getConstructor().getModifiers() == Modifier.PUBLIC, "no-arg ctor public");
        check(c.getConstructor(int.class) != null, "ctor (int initialCapacity)");
        for (String m : new String[]{"write", "writeTo", "append", "reset",
                                     "toCharArray", "size", "toString", "flush", "close"})
            check(has(c, m), "has method " + m);
        check(c.getMethod("append", CharSequence.class).getReturnType() == c,
              "append overrides return CharArrayWriter (this)");
        check(Modifier.isProtected(c.getDeclaredField("buf").getModifiers()), "protected buf");
        check(Modifier.isProtected(c.getDeclaredField("count").getModifiers()), "protected count");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. all write forms accumulate in order
        CharArrayWriter w = new CharArrayWriter();
        w.write('a');
        w.write("bc");
        w.write("def".toCharArray());
        w.write("XYZW", 1, 2);
        w.write("gh".toCharArray(), 0, 2);
        check("abcdefYZgh".equals(w.toString()), "write forms: " + w);
        check(w.size() == 10, "size counts chars");

        // 2. growth: tiny initial capacity expands without losing data
        CharArrayWriter small = new CharArrayWriter(1);
        for (int i = 0; i < 10_000; i++) small.write('x');
        check(small.size() == 10_000, "grew to 10000");
        check(small.toString().length() == 10_000, "content preserved across growth");

        // 3. negative initial capacity rejected
        try {
            new CharArrayWriter(-1);
            check(false, "negative capacity must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // 4. append family returns the same writer and appends
        CharArrayWriter a = new CharArrayWriter();
        check(a.append('A') == a && a.append("BC") == a && a.append("CDE", 1, 3) == a,
              "append returns this");
        check("ABCDE".equals(a.toString()), "append contents: " + a);

        // 5. toCharArray returns a usable snapshot; later writes do not affect it
        CharArrayWriter s = new CharArrayWriter();
        s.write("snap");
        char[] snapshot = s.toCharArray();
        check(snapshot.length == 4 && snapshot[0] == 's', "snapshot contents");
        s.write("more");
        check(snapshot.length == 4, "snapshot not resized by later writes");

        // 6. reset discards accumulated data but keeps capacity semantics
        s.reset();
        check(s.size() == 0 && s.toString().isEmpty(), "reset empties");
        s.write("new");
        check("new".equals(s.toString()), "writable after reset");

        // 7. writeTo copies the buffer into another Writer
        CharArrayWriter src = new CharArrayWriter();
        src.write("copy-me");
        StringWriter dst = new StringWriter();
        src.writeTo(dst);
        check("copy-me".equals(dst.toString()), "writeTo copies");
        check(src.size() == 7, "writeTo leaves the source intact");

        // 8. flush and close are no-ops: writing continues afterwards
        CharArrayWriter n = new CharArrayWriter();
        n.write("1");
        n.flush();
        n.close();
        n.write("2");
        check("12".equals(n.toString()), "close() does not close a CharArrayWriter");

        // 9. zero-length writes change nothing
        n.write("", 0, 0);
        n.write(new char[0], 0, 0);
        check("12".equals(n.toString()), "zero-length writes are no-ops");

        // 10. polymorphic use as Writer/Appendable
        Writer base = new CharArrayWriter();
        base.write("w");
        base.append('!');
        check("w!".equals(base.toString()), "via Writer reference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
