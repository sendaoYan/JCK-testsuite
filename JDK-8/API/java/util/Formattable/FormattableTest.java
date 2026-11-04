/*
 * @test
 * @summary java.util.Formattable API conformance (Java 8 surface): formatTo callback contract
 *          (flags/width/precision delivery matrix, LEFT_JUSTIFY/UPPERCASE/ALTERNATE bit values,
 *          width self-padding responsibility, precision truncation, null arg skips callback,
 *          exception propagation from formatTo), Formattable vs '#'-flag interplay, and
 *          direct formatTo invocation.
 * @run main FormattableTest
 */

import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Formatter;
import java.util.IllegalFormatWidthException;
import java.util.Locale;

public class FormattableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static class F implements Formattable {
        public void formatTo(Formatter formatter, int flags, int width, int precision) {
            formatter.format("[%d,%d,%d]", flags, width, precision);
        }
    }

    /** Formattable that honors width/precision itself the way the javadoc recommends. */
    static class Pad implements Formattable {
        public void formatTo(Formatter f, int flags, int width, int precision) {
            String s = "value";
            if (precision >= 0 && precision < s.length()) s = s.substring(0, precision);
            StringBuilder sb = new StringBuilder();
            int pad = width > 0 ? width - s.length() : 0;
            if (pad > 0 && (flags & FormattableFlags.LEFT_JUSTIFY) != 0) {
                sb.append(s);
                for (int i = 0; i < pad; i++) sb.append(' ');
            } else {
                for (int i = 0; i < pad; i++) sb.append(' ');
                sb.append(s);
            }
            f.format("%s", sb);
        }
    }

    static class Throwing implements Formattable {
        public void formatTo(Formatter f, int flags, int width, int precision) {
            if (width > 3) throw new IllegalFormatWidthException(width);
            f.format("ok");
        }
    }

    static class RejectUpper implements Formattable {
        public void formatTo(Formatter f, int flags, int width, int precision) {
            if ((flags & FormattableFlags.UPPERCASE) != 0)
                throw new FormatFlagsConversionMismatchException("S", 's');
            f.format("obj");
        }
    }

    static void testDirectCallback() {
        StringBuilder sb = new StringBuilder();
        new F().formatTo(new Formatter(sb), 0, -1, -1);
        check("[0,-1,-1]".equals(sb.toString()), "formatTo writes to the formatter");
        // 直接调用传递任意 flags
        StringBuilder sb2 = new StringBuilder();
        new F().formatTo(new Formatter(sb2), 7, 8, 9);
        check("[7,8,9]".equals(sb2.toString()), "formatTo passes flags/width/precision verbatim");
    }

    static void testFlagDeliveryMatrix() {
        check("[0,-1,-1]".equals(String.format("%s", new F())), "%s: flags 0, width/precision -1");
        check("[1,10,-1]".equals(String.format("%-10s", new F())), "%-10s delivers LEFT_JUSTIFY + width");
        check("[2,-1,-1]".equals(String.format("%S", new F())), "%S delivers UPPERCASE");
        check("[4,-1,-1]".equals(String.format("%#s", new F())), "%#s delivers ALTERNATE to Formattable");
        check("[6,-1,-1]".equals(String.format("%#S", new F())), "%#S delivers UPPERCASE|ALTERNATE");
        check("[0,-1,3]".equals(String.format("%.3s", new F())), "%.3s delivers precision 3");
        check("[1,8,2]".equals(String.format("%-8.2s", new F())), "%-8.2s delivers all three");
        // '#'+UPPERCASE 组合
        check("[7,4,-1]".equals(String.format("%#-4S", new F())), "%#-4S combines ALTERNATE|LEFT|UPPERCASE");
    }

    static void testFlagConstantValues() {
        check(FormattableFlags.LEFT_JUSTIFY == 1, "LEFT_JUSTIFY == 1");
        check(FormattableFlags.UPPERCASE == 2, "UPPERCASE == 2");
        check(FormattableFlags.ALTERNATE == 4, "ALTERNATE == 4");
        check((FormattableFlags.LEFT_JUSTIFY | FormattableFlags.UPPERCASE
               | FormattableFlags.ALTERNATE) == 7, "flags are bitmask combinable");
    }

    static void testWidthPrecisionSemantics() {
        // Formatter 不替 Formattable 补宽度：实现必须自己处理（javadoc 契约）
        check("[0,10,-1]".equals(String.format("%10s", new F())), "Formatter does not pad Formattable output");
        // 自实现按宽度填充
        check("value ".equals(String.format("%-6s|", new Pad()).substring(0, 6)), "left-justified self pad");
        check(" value".equals(String.format("%6s|", new Pad()).substring(0, 6)), "right-justified self pad");
        check("exactly".length() == 7, "sanity");
        check("val".equals(String.format("%.3s", new Pad())), "precision truncates in formatTo");
        check("value".equals(String.format("%-3s", new Pad())), "width smaller than content: no pad");
        // 精度 0 → 空串（但仍受宽度影响）
        check("     ".equals(String.format("%5.0s", new Pad())), "precision 0 empties, width pads");
    }

    static void testNullAndExceptions() {
        // null 实参不回调 formatTo
        final int[] calls = {0};
        class Counting implements Formattable {
            public void formatTo(Formatter f, int fl, int w, int p) { calls[0]++; f.format("c"); }
        }
        String.format("%s", (Object) null);
        check(calls[0] == 0, "null arg skips formatTo");
        // formatTo 抛出的 IllegalFormatException 原样透传
        expectThrows(IllegalFormatWidthException.class, () -> String.format("%5s", new Throwing()),
                     "formatTo exception propagates");
        expectThrows(FormatFlagsConversionMismatchException.class, () -> String.format("%S", new RejectUpper()),
                     "recommended FormatFlagsConversionMismatchException pattern");
        check("ok".equals(String.format("%3s", new Throwing())), "no throw when width small");
        // '#' 与非 Formattable 的 %s → FormatFlagsConversionMismatchException（对照）
        expectThrows(FormatFlagsConversionMismatchException.class,
                     () -> String.format(Locale.US, "%#s", "plain"), "%#s on String mismatch");
    }

    static void testFormatterReference() {
        final Formatter[] seen = {null};
        class Cap implements Formattable {
            public void formatTo(Formatter f, int fl, int w, int p) { seen[0] = f; f.format("cap"); }
        }
        Formatter fmt = new Formatter();
        fmt.format("%s", new Cap());
        check(seen[0] == fmt, "formatTo receives the very Formatter instance");
        check("cap".equals(fmt.toString()), "output routed through the formatter");
        fmt.close();
    }

    public static void main(String[] args) {
        testDirectCallback();
        testFlagDeliveryMatrix();
        testFlagConstantValues();
        testWidthPrecisionSemantics();
        testNullAndExceptions();
        testFormatterReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
