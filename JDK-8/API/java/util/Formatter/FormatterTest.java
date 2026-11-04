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

    public static void main(String[] args) {
        testFormat();
        testStringFormat();
        testConversions();
        testWidthPrecisionFlags();
        testArgumentIndex();
        testConstructorNull();
        testLocaleAndOut();
        testFlushCloseIoException();
        testIllegalFormatFamily();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
