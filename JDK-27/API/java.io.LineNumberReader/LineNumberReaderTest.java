/*
 * @test
 * @summary java.io.LineNumberReader conformance: white-box structure + black-box
 *          behavior (line counting across \n, \r and \r\n, read/read(char[])/readLine
 *          paths, setLineNumber/getLineNumber, mark/reset restores the line number,
 *          skip counts lines, terminator semantics on readLine, no terminator at EOF).
 * @run main LineNumberReaderTest
 */

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class LineNumberReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static LineNumberReader lnr(String s) {
        return new LineNumberReader(new StringReader(s));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.LineNumberReader");
        check(c.getSuperclass().getName().equals("java.io.BufferedReader"),
              "extends BufferedReader");
        check(c.getConstructor(java.io.Reader.class) != null, "ctor (Reader)");
        check(c.getConstructor(java.io.Reader.class, int.class) != null, "ctor (Reader, int)");
        for (String m : new String[]{"setLineNumber", "getLineNumber", "read",
                                     "readLine", "skip", "mark", "reset"})
            check(has(c, m), "has method " + m);
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. counting with read() over all three terminator forms; \r and \r\n
        //    are both delivered as a single '\n' read
        LineNumberReader r = lnr("a\nb\rc\r\nd");
        check(r.read() == 'a', "first char");
        check(r.getLineNumber() == 0, "line 0 before any terminator");
        check(r.read() == '\n', "\n delivered");
        check(r.getLineNumber() == 1, "\\n increments");
        check(r.read() == 'b', "b on line 1");
        check(r.read() == '\n', "\\r delivered as \n");
        check(r.getLineNumber() == 2, "\\r increments");
        check(r.read() == 'c', "c on line 2");
        check(r.read() == '\n', "\\r\\n collapsed to one \n");
        check(r.getLineNumber() == 3, "\\r\\n counts once");
        check(r.read() == 'd', "d on line 3");
        check(r.getLineNumber() == 3, "reading the char itself does not increment");
        check(r.read() == -1, "EOF");
        check(r.getLineNumber() == 4, "EOF terminates the unterminated last line");

        // 2. readLine: strips terminators, returns null at EOF
        r = lnr("one\r\ntwo\nthree");
        check("one".equals(r.readLine()), "first line");
        check(r.getLineNumber() == 1, "line after first readLine");
        check("two".equals(r.readLine()) && "three".equals(r.readLine()),
              "remaining lines");
        check(r.getLineNumber() == 3, "final line counted");
        check(r.readLine() == null, "null at EOF");

        // 3. bulk read counts lines too
        r = lnr("1\n2\n3\n");
        char[] buf = new char[64];
        r.read(buf);
        check(r.getLineNumber() == 3, "bulk read counted 3 lines");

        // 4. setLineNumber moves the counter arbitrarily (no repositioning)
        r = lnr("x\ny");
        r.setLineNumber(100);
        check(r.getLineNumber() == 100, "setLineNumber");
        r.readLine();
        check(r.getLineNumber() == 101, "counting continues from the set value");
        r.setLineNumber(-5);
        check(r.getLineNumber() == -5, "negative line numbers allowed");

        // 5. mark/reset restores the line number along with the position
        r = lnr("p\nq\nr");
        r.readLine();
        r.mark(100);
        r.readLine();
        check(r.getLineNumber() == 2, "advanced past the mark");
        r.reset();
        check(r.getLineNumber() == 1, "reset restored the line number");
        check("q".equals(r.readLine()), "reset restored the position");

        // 6. skip counts lines it crosses
        r = lnr("ab\ncd\nef");
        r.skip(3);                                       // consumes "ab\n"
        check(r.getLineNumber() == 1, "skip crossed one terminator");
        check(r.read() == 'c', "position after skip");

        // 7. empty input
        r = lnr("");
        check(r.readLine() == null && r.getLineNumber() == 0, "empty input");

        // 8. read() at EOF and close idempotence
        r = lnr("z");
        r.readLine();
        check(r.read() == -1, "EOF via read");
        r.close();
        r.close();
        try {
            r.readLine();
            check(false, "readLine after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "close message: " + ioe.getMessage());
        }

        // 9. long lines exceeding the default buffer still count correctly
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) sb.append('x');
        String longLine = sb.toString();
        r = lnr(longLine + "\n" + longLine + "\n");
        check(longLine.equals(r.readLine()), "long line 1");
        check(longLine.equals(r.readLine()), "long line 2");
        check(r.getLineNumber() == 2, "long lines counted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
