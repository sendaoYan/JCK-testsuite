/*
 * @test
 * @summary java.text.ParsePosition conformance: white-box structure + black-box behavior.
 * @run main ParsePositionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

public class ParsePositionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.ParsePosition");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "concrete, non-final class");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPublic(ctors[0].getModifiers()),
                "single public ParsePosition(int)");
        check(c.getMethod("getIndex").getReturnType() == int.class, "getIndex() -> int");
        check(c.getMethod("getErrorIndex").getReturnType() == int.class, "getErrorIndex() -> int");
        check(c.getMethod("setIndex", int.class).getReturnType() == void.class, "setIndex(int)");
        check(c.getMethod("setErrorIndex", int.class).getReturnType() == void.class, "setErrorIndex(int)");
        check(c.getMethod("equals", Object.class) != null && c.getMethod("hashCode") != null,
                "equals + hashCode present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws ParseException {
        // 1. constructor + index accessors
        ParsePosition pp = new ParsePosition(5);
        check(pp.getIndex() == 5, "ctor stores index");
        check(pp.getErrorIndex() == -1, "errorIndex defaults to -1");
        pp.setIndex(7);
        check(pp.getIndex() == 7, "setIndex");
        pp.setErrorIndex(2);
        check(pp.getErrorIndex() == 2, "setErrorIndex");
        pp.setErrorIndex(-1);
        check(pp.getErrorIndex() == -1, "setErrorIndex(-1) resets");
        pp.setIndex(0);
        check(pp.getIndex() == 0, "setIndex(0) allowed");

        // 2. equals/hashCode consider index and errorIndex
        ParsePosition q = new ParsePosition(7);
        q.setErrorIndex(2);
        check(q.equals(q), "equals(self)");
        ParsePosition same = new ParsePosition(0);
        same.setErrorIndex(-1);
        check(pp.equals(same) && same.equals(pp), "equal state -> equals");
        check(pp.hashCode() == same.hashCode(), "hashCode consistent with equals");
        same.setIndex(1);
        check(!pp.equals(same), "index participates in equals");
        ParsePosition diffErr = new ParsePosition(0);
        diffErr.setErrorIndex(3);
        check(!pp.equals(diffErr), "errorIndex participates in equals");
        check(!pp.equals(null), "equals(null) false");
        check(!pp.equals("pp"), "equals(other type) false");

        // 3. toString
        ParsePosition t = new ParsePosition(7);
        t.setErrorIndex(2);
        check("java.text.ParsePosition[index=7,errorIndex=2]".equals(t.toString()),
                "toString exact format");
        check("java.text.ParsePosition[index=0,errorIndex=-1]"
                .equals(new ParsePosition(0).toString()), "toString default errorIndex");

        // 4. real parse chain: NumberFormat drives index/errorIndex
        NumberFormat nf = DecimalFormat.getNumberInstance(Locale.US);
        ParsePosition full = new ParsePosition(0);
        Number n = nf.parse("1,234.5xyz", full);
        check(n != null && n.doubleValue() == 1234.5, "prefix parse succeeds");
        check(full.getIndex() == 7 && full.getErrorIndex() == -1,
                "index advanced past parsed text, no error");
        ParsePosition from2 = new ParsePosition(2);
        Number tail = nf.parse("ab34.5", from2);
        check(tail != null && tail.doubleValue() == 34.5 && from2.getIndex() == 6,
                "parse resumes at given index");
        ParsePosition err = new ParsePosition(0);
        check(nf.parse("xyz", err) == null, "failed parse returns null");
        check(err.getIndex() == 0 && err.getErrorIndex() == 0,
                "failed parse keeps index and records errorIndex");

        // 5. failed parse feeds ParseException.getErrorOffset through Format.parseObject(String)
        try {
            nf.parseObject("xyz");
            throw new AssertionError("expected ParseException");
        } catch (ParseException pe) {
            check(pe.getErrorOffset() == 0, "errorOffset mirrors ParsePosition.errorIndex");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
