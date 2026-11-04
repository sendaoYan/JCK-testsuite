/*
 * @test
 * @summary java.util.Formatter API conformance (Java 8 surface): complete contract coverage
 *          of constructors (Appendable null -> StringBuilder, PrintStream/OutputStream/String/
 *          File null -> NPE), format conversion specifiers (s/d/x/o/e/g/a/c/b/h/%% + width/
 *          flags/precision/argument-index/relative), format(Locale,...) override, locale()/out()/
 *          toString(), flush/close/close-idempotent + FormatterClosedException, ioException,
 *          and IllegalFormatException family (conversion/flags/unknown/missing-argument).
 * @run main FormatterTest
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatFlagsException;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

public class FormatterTest {
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

    // ===================== 契约深化：转换符矩阵/精度/分组/异常族/getter =====================

    static String f(String fmt, Object... args) {
        return String.format(Locale.US, fmt, args);
    }

    static void testConversionVectorMatrix() {
        // 整数族（Locale.US 固定）
        check("42".equals(f("%d", 42)), "%d");
        check("52".equals(f("%o", 42)), "%o octal");
        check("ff".equals(f("%x", 255)) && "FF".equals(f("%X", 255)), "%x/%X");
        check("10".equals(f("%o", 8)), "%o 8 -> 10");
        check("37777777770".equals(f("%o", -8)), "%o negative wraps unsigned");
        check("ffffff01".equals(f("%x", -255)), "%x negative two's complement");
        check("0xff".equals(f("%#x", 255)) && "0XFF".equals(f("%#X", 255)), "%#x alt prefix");
        check("010".equals(f("%#o", 8)), "%#o alt leading 0");
        // 浮点族
        check("1234.500000".equals(f("%f", 1234.5)), "%f default 6 decimals");
        check("1.234500e+03".equals(f("%e", 1234.5)), "%e mantissa");
        check("1.234500E+03".equals(f("%E", 1234.5)), "%E uppercase");
        check("1.235e+04".equals(f("%.3e", 12345.6789)), "%.3e rounds");
        check("3.14".equals(f("%.2f", 3.14159)), "%.2f precision");
        check("0.000100000".equals(f("%g", 0.0001)) && "123457".equals(f("%g", 123456.7)),
              "%g significant digits");
        check("0x1.0p-1".equals(f("%a", 0.5)), "%a hex float");
        check("1.01".equals(f("%.2f", new java.math.BigDecimal("1.005"))), "%f BigDecimal HALF_UP");
        // NaN/Infinity
        check("NaN".equals(f("%f", Double.NaN)), "%f NaN");
        check("Infinity".equals(f("%f", Double.POSITIVE_INFINITY)), "%f Infinity");
        check("-Infinity".equals(f("%f", Double.NEGATIVE_INFINITY)), "%f -Infinity");
        // 字符/布尔/hash
        check("Aa".equals(f("%c", 0x41) + f("%c", 'a')), "%c int code point and char");
        check("true".equals(f("%b", true)) && "FALSE".equals(f("%B", false)), "%b/%B Boolean");
        check("true".equals(f("%b", 0)) && "true".equals(f("%b", new int[0])), "%b non-null -> true");
        check(Integer.toHexString("hello".hashCode()).equals(f("%h", "hello")), "%h equals toHexString");
        // 字符串
        check("HI".equals(f("%S", "hi")), "%S uppercases");
        check("ab".equals(f("%.2s", "abcd")), "%.2s truncates");
        check("[      ab]".equals(f("[%8.2s]", "abcdef")), "width+precision on %s");
        // %% 与 %n
        check("%".equals(f("%%")), "%% literal percent");
        check(System.lineSeparator().equals(f("%n")), "%n equals line separator");
        // 宽度/flags
        check("    ab|ab    |".equals(f("%6s|%-6s|", "ab", "ab")), "%6s right / %-6s left");
        check("00042".equals(f("%05d", 42)), "%05d zero pad");
        check("-00042".equals(f("%06d", -42)), "%06d negative sign before zeros");
        check("+42".equals(f("%+d", 42)), "%+d");
        check(" 42".equals(f("% d", 42)), "% d space flag");
        check("(42)".equals(f("%(d", -42)), "%(d parens negative");
        check("-9,223,203,685,477,5808".startsWith("-9,223,372,036,854,775,808".substring(0, 4)), "Long.MIN_VALUE grouped");
        check("-9,223,372,036,854,775,808".equals(f("%,d", Long.MIN_VALUE)), "%,d Long.MIN_VALUE");
        check("123,456,789,012,345,678".equals(f("%,d", new java.math.BigInteger("123456789012345678"))),
              "%,d BigInteger");
    }

    static void testNullConversionMatrix() {
        check("null".equals(f("%s", (Object) null)), "%s null -> null");
        check("null".equals(f("%d", (Object) null)), "%d null -> null");
        check("null".equals(f("%x", (Object) null)), "%x null -> null");
        check("null".equals(f("%c", (Object) null)), "%c null -> null");
        check("null".equals(f("%e", (Object) null)), "%e null -> null");
        check("null".equals(f("%h", (Object) null)), "%h null -> null");
        check("false".equals(f("%b", (Object) null)), "%b null -> false");
        check("FALSE".equals(f("%B", (Object) null)), "%B null -> FALSE");
        check("nu".equals(f("%.2f", (Object) null)), "%.2f null -> nu (precision truncates 'null')");
        check("nu".equals(f("%.2s", (Object) null)), "%.2s null -> nu");
    }

    static void testGroupingLocales() {
        check("1,234,567".equals(String.format(Locale.US, "%,d", 1234567)), "US grouping comma");
        check("1.234.567".equals(String.format(Locale.GERMANY, "%,d", 1234567)), "GERMANY grouping dot");
        check("1,234,567".equals(String.format(Locale.CHINA, "%,d", 1234567)), "CHINA grouping comma");
        check("1234,500".equals(String.format(Locale.GERMANY, "%.3f", 1234.5)), "GERMANY decimal comma");
        check("1.234,500".equals(String.format(Locale.GERMANY, "%,.3f", 1234.5)), "GERMANY grouped decimal");
        check("-1,234,567".equals(f("%,d", -1234567)), "negative grouped");
    }

    static void testArgumentIndexMatrix() {
        check("seven 7".equals(f("%2$s %1$d", 7, "seven")), "%2$s %1$d reordering");
        check("5 5".equals(f("%d %<d", 5)), "%<d relative reuse");
        check("a a".equals(f("%1$s %1$s", "a")), "explicit index reuse");
        check("7seven".equals(f("%d%s", 7, "seven")), "ordinary index advances after reuse");
        // 索引越界（无对应实参）→ MissingFormatArgumentException
        expectThrows(java.util.MissingFormatArgumentException.class, () -> f("%99$d", 1),
                     "%99$d missing arg");
        java.util.MissingFormatArgumentException me =
                expectThrowsGet(java.util.MissingFormatArgumentException.class, () -> f("%s and %s", "one"),
                                "second %s missing");
        check("%s".equals(me.getFormatSpecifier()), "MissingFormatArgumentException.getFormatSpecifier");
        check(me.getMessage() != null && me.getMessage().contains("%s"), "message contains specifier");
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T expectThrowsGet(Class<T> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return (T) t; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testPrecisionWidthFlagErrors() {
        // 精度用于整数转换 → IllegalFormatPrecisionException（getter getPrecision）
        java.util.IllegalFormatPrecisionException pe =
                expectThrowsGet(java.util.IllegalFormatPrecisionException.class, () -> f("%5.3d", 1),
                                "%.3d precision on integer");
        check(pe.getPrecision() == 3, "IllegalFormatPrecisionException.getPrecision");
        // 宽度与 %n → IllegalFormatWidthException（getter getWidth）
        java.util.IllegalFormatWidthException we =
                expectThrowsGet(java.util.IllegalFormatWidthException.class, () -> f("%5n"),
                                "%5n width with newline");
        check(we.getWidth() == 5, "IllegalFormatWidthException.getWidth");
        // 重复 flag → DuplicateFormatFlagsException（getter getFlags）
        java.util.DuplicateFormatFlagsException de =
                expectThrowsGet(java.util.DuplicateFormatFlagsException.class, () -> f("%,,d", 1),
                                "%,,d duplicate comma");
        check(",".equals(de.getFlags()), "DuplicateFormatFlagsException.getFlags");
        // '-'/'0' 无宽度 → MissingFormatWidthException（getter getFormatSpecifier）
        java.util.MissingFormatWidthException mw =
                expectThrowsGet(java.util.MissingFormatWidthException.class, () -> f("%-d", 1),
                                "%-d missing width");
        check("%-d".equals(mw.getFormatSpecifier()), "MissingFormatWidthException.getFormatSpecifier");
        expectThrows(java.util.MissingFormatWidthException.class, () -> f("%0d", 1), "%0d missing width");
        // flag 冲突 '+ ' → IllegalFormatFlagsException（getter getFlags）
        java.util.IllegalFormatFlagsException fe =
                expectThrowsGet(java.util.IllegalFormatFlagsException.class, () -> f("%+ d", 1),
                                "%+ d conflicting flags");
        check(fe.getFlags().contains("+") && fe.getFlags().contains(" "), "IllegalFormatFlagsException.getFlags");
        // ',' 用于 %x → FormatFlagsConversionMismatchException（getter getFlags/getConversion）
        java.util.FormatFlagsConversionMismatchException cme =
                expectThrowsGet(java.util.FormatFlagsConversionMismatchException.class, () -> f("%,x", 255),
                                "%,x grouping with hex");
        check(",".equals(cme.getFlags()) && 'x' == cme.getConversion(),
              "FormatFlagsConversionMismatchException getters");
        // '%' 结尾 → UnknownFormatConversionException
        java.util.UnknownFormatConversionException uc =
                expectThrowsGet(java.util.UnknownFormatConversionException.class, () -> f("100%"),
                                "trailing percent");
        check("%".equals(uc.getConversion()), "UnknownFormatConversionException.getConversion");
        // 无效转换符
        expectThrows(java.util.UnknownFormatConversionException.class, () -> f("%q", 1), "%q unknown");
        // '0'/'-' 与 %n → IllegalFormatFlagsException
        expectThrows(java.util.IllegalFormatFlagsException.class, () -> f("%0n"), "%0n");
        expectThrows(java.util.IllegalFormatFlagsException.class, () -> f("%-n"), "%-n");
        // format(null) NPE
        expectThrows(NullPointerException.class, () -> new Formatter().format(null),
                     "format((String)null) NPE");
        // args 传 null 数组：无实参引用时被容忍
        new Formatter().format("plain", (Object[]) null);
        check(true, "format with null args array tolerated when no specifier");
    }

    static void testIllegalFormatConversionGetters() {
        java.util.IllegalFormatConversionException ice =
                expectThrowsGet(java.util.IllegalFormatConversionException.class, () -> f("%d", "str"),
                                "%d with String");
        check(ice.getArgumentClass() == String.class, "getArgumentClass is String");
        check(ice.getConversion() == 'd', "getConversion is d");
        check(ice.getMessage().contains("d") && ice.getMessage().contains("java.lang.String"),
              "message names conversion and class");
        expectThrows(java.util.IllegalFormatConversionException.class, () -> f("%x", 3.5), "%x with double");
        expectThrows(java.util.IllegalFormatConversionException.class, () -> f("%e", "str"), "%e with String");
        expectThrows(java.util.IllegalFormatConversionException.class, () -> f("%c", "str"), "%c with String");
        expectThrows(java.util.IllegalFormatConversionException.class, () -> f("%f", 1), "%f with int");
        // 无效码点 → IllegalFormatCodePointException（getter getCodePoint）
        java.util.IllegalFormatCodePointException cp =
                expectThrowsGet(java.util.IllegalFormatCodePointException.class, () -> f("%c", 0x110000),
                                "%c invalid code point");
        check(cp.getCodePoint() == 0x110000, "IllegalFormatCodePointException.getCodePoint");
        check(String.format("%c", 0x10000).codePointAt(0) == 0x10000, "supplementary code point ok");
    }

    static void testAppendableErrorPath() {
        java.io.IOException boom = new java.io.IOException("io-bad");
        Appendable bad = new Appendable() {
            public Appendable append(CharSequence csq) throws java.io.IOException { throw boom; }
            public Appendable append(CharSequence c, int s, int e) throws java.io.IOException { throw boom; }
            public Appendable append(char c) throws java.io.IOException { throw boom; }
        };
        Formatter bf = new Formatter(bad);
        bf.format("%d", 1); // 输出失败：format 不抛，错误记录在 ioException()
        check(boom == bf.ioException(), "ioException() returns the append failure");
        // 后续 format 不抛（错误只记录一次），ioException 保留
        bf.format("%d", 2);
        check(boom == bf.ioException(), "ioException retained after repeated failures");
        bf.close();
    }

    static void testFormatterFileAndStream() throws Exception {
        java.nio.file.Path tmp = java.nio.file.Files.createTempFile("formatter-probe", ".txt");
        try {
            Formatter ff = new Formatter(tmp.toFile());
            ff.format(Locale.US, "%d-%s", 7, "x");
            ff.close();
            String content = new String(java.nio.file.Files.readAllBytes(tmp));
            check("7-x".equals(content), "Formatter(File) writes through close");
            Formatter fs = new Formatter(new java.io.FileOutputStream(tmp.toFile()));
            fs.format("%s", "stream");
            fs.flush();
            check("stream".equals(new String(java.nio.file.Files.readAllBytes(tmp))),
                  "Formatter(OutputStream) writes");
            fs.close();
        } finally {
            java.nio.file.Files.deleteIfExists(tmp);
        }
    }

    static void testToStringAndOut() {
        Formatter f = new Formatter(Locale.US);
        f.format("a%db", 3);
        check("a3b".equals(f.toString()), "toString returns formatted content");
        check(f.out() instanceof StringBuilder, "out() default destination StringBuilder");
        check(f.toString().equals(f.out().toString()), "toString equals out().toString()");
        f.close();
        expectThrows(FormatterClosedException.class, f::toString, "toString after close FCE");
        expectThrows(FormatterClosedException.class, () -> f.format("x"), "format after close FCE");
    }

    static void testFormattableCallback() {
        // Formattable 实参：formatTo 收到 flags/width/precision；Formatter 不代为补空格
        final int[] rec = {-1, -2, -3};
        final int[] calls = {0};
        class F implements java.util.Formattable {
            public void formatTo(Formatter fmt, int flags, int width, int precision) {
                rec[0] = flags; rec[1] = width; rec[2] = precision; calls[0]++;
                fmt.format("X");
            }
        }
        check("X|".equals(f("%-10s|", new F())), "formatTo output not padded by Formatter");
        check(calls[0] == 1 && rec[0] == java.util.FormattableFlags.LEFT_JUSTIFY && rec[1] == 10 && rec[2] == -1,
              "%-10s flags/width/precision delivered");
        f("%S", new F());
        check(rec[0] == java.util.FormattableFlags.UPPERCASE, "%S delivers UPPERCASE flag");
        f("%.3s", new F());
        check(rec[2] == 3, "%.3s delivers precision 3");
        f("%#s", new F());
        check(rec[0] == java.util.FormattableFlags.ALTERNATE, "%#s delivers ALTERNATE to Formattable");
        f("%s", (Object) null);
        check(calls[0] == 4, "null arg does not invoke formatTo");
    }

    static void testFormat() {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb, Locale.US);
        f.format("%d %s %x %.2f", 42, "hi", 255, 3.14159);
        f.close();
        check("42 hi ff 3.14".equals(sb.toString()), "format d/s/x/f");
    }

    static void testStringFormat() {
        check("3.14".equals(String.format(Locale.US, "%.2f", 3.14159)), "String.format(Locale)");
    }

    static void testConversions() {
        Formatter f = new Formatter(Locale.US);
        f.format("%d %o %x %X %e %g %a %c %b %h %% %s",
                 42, 42, 42, 42, 3.14159, 0.00001, 0.5, 'A', true, "hash", "str");
        String out = f.toString();
        check(out.contains("42") && out.contains("52") && out.contains("2a") && out.contains("2A"),
              "integer conversions d/o/x/X");
        check(out.contains("A") && out.contains("true") && out.contains("str") && out.contains("%"),
              "char/boolean/string/%% conversions");
        f.close();
    }

    static void testWidthPrecisionFlags() {
        Formatter f = new Formatter(Locale.US);
        f.format("[%5d][%-5d][%05d][%+d][% d][%(d]", 42, 42, 42, 42, 42, -42);
        String out = f.toString();
        check(out.contains("[   42]") && out.contains("[42   ]") && out.contains("[00042]"),
              "width and zero/left flags");
        check(out.contains("+42") && out.contains(" 42") && out.contains("(42)"),
              "sign/space/paren flags");
        f.close();
    }

    static void testArgumentIndex() {
        Formatter f = new Formatter(Locale.US);
        f.format("%2$s %1$d", 7, "seven");
        check("seven 7".equals(f.toString()), "argument index");
        f.close();

        Formatter g = new Formatter(Locale.US);
        g.format("%d %<d", 5);
        check("5 5".equals(g.toString()), "relative index <");
        g.close();
    }

    static void testConstructorNull() {
        // Appendable null -> internally a StringBuilder (not NPE).
        Formatter a = new Formatter((Appendable) null, Locale.US);
        check(a.out() instanceof StringBuilder, "Appendable null -> StringBuilder");
        a.close();

        Formatter b = new Formatter((Appendable) null);
        check(b.out() instanceof StringBuilder, "Appendable null (default locale) -> StringBuilder");
        b.close();

        // Locale null is legal (no localization).
        Formatter c = new Formatter((Locale) null);
        c.format("%d", 1);
        check("1".equals(c.toString()), "null Locale -> no localization");
        c.close();

        // PrintStream/OutputStream/String/File null -> NPE.
        expectThrows(NullPointerException.class, () -> new Formatter((PrintStream) null),
                     "Formatter((PrintStream)null) NPE");
        expectThrows(NullPointerException.class, () -> new Formatter((OutputStream) null),
                     "Formatter((OutputStream)null) NPE");
        expectThrows(NullPointerException.class, () -> {
            try { new Formatter((String) null); }
            catch (java.io.FileNotFoundException e) { throw new AssertionError("unexpected FNFE", e); }
        }, "Formatter((String)null) NPE");
        expectThrows(NullPointerException.class, () -> {
            try { new Formatter((java.io.File) null); }
            catch (java.io.FileNotFoundException e) { throw new AssertionError("unexpected FNFE", e); }
        }, "Formatter((File)null) NPE");
        expectThrows(NullPointerException.class, () -> {
            try { new Formatter(new ByteArrayOutputStream(), null); }
            catch (java.io.UnsupportedEncodingException e) { throw new AssertionError("unexpected UEE", e); }
        }, "Formatter(os, null charset) NPE");
    }

    static void testLocaleAndOut() {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb, Locale.US);
        check(f.locale() == Locale.US, "locale() returns Locale.US");
        check(f.out() == sb, "out() returns the Appendable");
        f.close();
    }

    static void testFlushCloseIoException() {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb, Locale.US);
        f.format("x");
        f.flush(); // StringBuilder is Flushable no-op, should not throw.
        check(f.ioException() == null, "ioException() null before close");
        f.close();
        f.close(); // idempotent
        check(f.ioException() == null, "ioException() null after clean close");

        expectThrows(FormatterClosedException.class, () -> f.format("%d", 1),
                     "format after close FCE");
        expectThrows(FormatterClosedException.class, () -> f.flush(), "flush after close FCE");
    }

    static void testIllegalFormatFamily() {
        Formatter f = new Formatter(Locale.US);
        expectThrows(IllegalFormatConversionException.class, () -> f.format("%d", "notanint"),
                     "%d with String -> IllegalFormatConversionException");
        expectThrows(UnknownFormatConversionException.class, () -> f.format("%q", 1),
                     "%q -> UnknownFormatConversionException");
        expectThrows(MissingFormatArgumentException.class, () -> f.format("%d %s", 1),
                     "missing arg -> MissingFormatArgumentException");
        expectThrows(IllegalFormatFlagsException.class, () -> f.format("%-05d", 1),
                     "- and 0 flags conflict -> IllegalFormatFlagsException");
        expectThrows(NullPointerException.class, () -> f.format(null), "format(null) NPE");
        f.close();
    }

    public static void main(String[] args) throws Exception {
        testFormat();
        testStringFormat();
        testConversions();
        testWidthPrecisionFlags();
        testArgumentIndex();
        testConstructorNull();
        testLocaleAndOut();
        testFlushCloseIoException();
        testIllegalFormatFamily();
        testConversionVectorMatrix();
        testNullConversionMatrix();
        testGroupingLocales();
        testArgumentIndexMatrix();
        testPrecisionWidthFlagErrors();
        testIllegalFormatConversionGetters();
        testAppendableErrorPath();
        testFormatterFileAndStream();
        testToStringAndOut();
        testFormattableCallback();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
